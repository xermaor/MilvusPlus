package com.github.xermao.milvus.plus.converter;

import com.github.xermao.milvus.plus.annotation.*;
import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.partition.request.CreatePartitionReq;
import io.milvus.v2.service.partition.request.HasPartitionReq;
import io.milvus.v2.service.partition.request.LoadPartitionsReq;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.xermao.milvus.plus.builder.CollectionSchemaBuilder;
import com.github.xermao.milvus.plus.cache.CollectionToPrimaryCache;
import com.github.xermao.milvus.plus.cache.ConversionCache;
import com.github.xermao.milvus.plus.cache.MilvusCache;
import com.github.xermao.milvus.plus.cache.PropertyCache;
import com.github.xermao.milvus.plus.model.MilvusEntity;
import com.github.xermao.milvus.plus.util.AnalyzerParamsUtils;
import com.github.xermao.milvus.plus.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Milvus转换器，用于将Java实体类转换为Milvus实体对象
 *
 * @author xermao
 */
public class MilvusConverter {

    private final static Logger log = LoggerFactory.getLogger(MilvusConverter.class);

    private static final String SPARSE_SUFFIX = "_sparse";
    private static final String SPARSE_INDEX_SUFFIX = "_sparse_index";
    private static final String BM25_SUFFIX = "_bm25_emb";
    private static final String GET_PREFIX = "get";
    private static final String IS_PREFIX = "is";

    /**
     * 将Java实体类转换为MilvusEntity对象
     *
     * @param entityClass 需要转换的Java实体类的Class对象
     * @return 转换后的MilvusEntity对象
     * @throws IllegalArgumentException 如果实体类没有@MilvusCollection注解
     */
    public static MilvusEntity convert(Class<?> entityClass) {
        // 检查缓存
        ConversionCache cache = MilvusCache.milvusCache.get(entityClass.getName());
        if (Objects.nonNull(cache)) {
            return cache.milvusEntity();
        }

        // 验证实体类注解
        MilvusCollection collectionAnnotation = validateAndGetCollectionAnnotation(entityClass);

        // 构建转换上下文
        ConversionContext context = new ConversionContext(entityClass, collectionAnnotation);

        // 执行转换
        MilvusEntity milvusEntity = performConversion(context);

        // 缓存结果
        cacheConversionResult(entityClass, milvusEntity, context);

        return milvusEntity;
    }

    /**
     * 验证并获取集合注解
     */
    private static MilvusCollection validateAndGetCollectionAnnotation(Class<?> entityClass) {
        MilvusCollection annotation = entityClass.getAnnotation(MilvusCollection.class);
        if (Objects.isNull(annotation)) {
            throw new IllegalArgumentException("Entity must be annotated with @MilvusCollection");
        }
        return annotation;
    }

    /**
     * 执行转换过程
     */
    private static MilvusEntity performConversion(ConversionContext context) {
        // 处理分区信息
        List<String> partitionNames = processPartitionAnnotation(context.entityClass);

        // 处理字段信息
        FieldProcessingResult fieldResult = processFields(context);

        // 构建MilvusEntity
        return new MilvusEntity(
                context.collectionName,
                context.description,
                context.alias,
                fieldResult.indexParams(),
                fieldResult.milvusFields(),
                partitionNames,
                context.consistencyLevel,
                context.enableDynamicField,
                fieldResult.functions()
        );
    }

    /**
     * 处理分区注解
     */
    private static List<String> processPartitionAnnotation(Class<?> entityClass) {
        MilvusPartition partitionAnnotation = entityClass.getAnnotation(MilvusPartition.class);
        if (Objects.isNull(partitionAnnotation)) {
            return Collections.emptyList();
        }
        return List.of(partitionAnnotation.name());
    }

    /**
     * 处理字段信息
     */
    private static FieldProcessingResult processFields(ConversionContext context) {
        List<Field> fields = getAllFieldsFromClass(context.entityClass);
        List<AddFieldReq> milvusFields = new ArrayList<>();
        List<IndexParam> indexParams = new ArrayList<>();
        List<CreateCollectionReq.Function> functions = new ArrayList<>();
        PropertyCache propertyCache = new PropertyCache();

        for (Field field : fields) {
            MilvusField fieldAnnotation = field.getAnnotation(MilvusField.class);
            if (Objects.isNull(fieldAnnotation)) {
                continue;
            }

            String fieldName = getFieldName(field, fieldAnnotation);

            // 缓存属性映射
            cachePropertyMapping(field, fieldName, fieldAnnotation, propertyCache);

            // 处理主键字段
            handlePrimaryKey(fieldAnnotation, fieldName, context.collectionName);

            // 构建字段请求
            AddFieldReq fieldReq = buildFieldRequest(field, fieldName, fieldAnnotation, context);
            milvusFields.add(fieldReq);

            // 处理文本分析器
            if (fieldAnnotation.enableAnalyzer() && fieldAnnotation.dataType() == DataType.VarChar) {
                processTextAnalyzer(fieldName, fieldAnnotation, milvusFields, indexParams, functions);
            }

            // 处理索引
            createIndexParam(field, fieldName).ifPresent(indexParams::add);
        }

        context.propertyCache = propertyCache;
        return new FieldProcessingResult(milvusFields, indexParams, functions);
    }

    /**
     * 获取字段名称
     */
    private static String getFieldName(Field field, MilvusField fieldAnnotation) {
        return fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name();
    }

    /**
     * 缓存属性映射
     */
    private static void cachePropertyMapping(Field field, String fieldName, MilvusField fieldAnnotation, PropertyCache propertyCache) {
        propertyCache.putFunctionToProperty(field.getName(), fieldName);
        propertyCache.nullableToPropertyMap.put(field.getName(), fieldAnnotation.nullable());
        propertyCache.methodToPropertyMap.put(getGetMethodName(field), fieldName);
    }

    /**
     * 处理主键字段
     */
    private static void handlePrimaryKey(MilvusField fieldAnnotation, String fieldName, String collectionName) {
        if (fieldAnnotation.isPrimaryKey()) {
            CollectionToPrimaryCache.collectionToPrimary.put(collectionName, fieldName);
        }
    }

    /**
     * 构建字段请求
     */
    private static AddFieldReq buildFieldRequest(Field field, String fieldName, MilvusField fieldAnnotation, ConversionContext context) {
        AddFieldReq.AddFieldReqBuilder<?, ?> builder = AddFieldReq.builder()
                .fieldName(fieldName)
                .dataType(fieldAnnotation.dataType())
                .isPrimaryKey(fieldAnnotation.isPrimaryKey())
                .isPartitionKey(fieldAnnotation.isPartitionKey())
                .elementType(fieldAnnotation.elementType())
                .enableAnalyzer(fieldAnnotation.enableAnalyzer())
                .enableMatch(fieldAnnotation.enableMatch())
                .isNullable(fieldAnnotation.nullable())
                .autoID(false);

        // 更新自动ID状态
        context.autoID = context.autoID || fieldAnnotation.autoID();

        // 设置描述
        Optional.of(fieldAnnotation.description())
                .filter(StringUtils::isNotEmpty)
                .ifPresent(builder::description);

        // 处理向量字段维度
        handleVectorDimension(field, fieldAnnotation, builder);

        // 处理数组最大长度
        Optional.of(fieldAnnotation.maxLength())
                .filter(maxLength -> maxLength > 0)
                .ifPresent(builder::maxLength);

        // 处理哈希表最大容量
        Optional.of(fieldAnnotation.maxCapacity())
                .filter(maxCapacity -> maxCapacity > 0)
                .ifPresent(builder::maxCapacity);

        return builder.build();
    }

    /**
     * 处理向量字段维度
     */
    private static void handleVectorDimension(Field field, MilvusField fieldAnnotation, AddFieldReq.AddFieldReqBuilder<?, ?> builder) {
        Optional.of(fieldAnnotation.dimension())
                .filter(dimension -> dimension > 0)
                .ifPresent(dimension -> {
                    builder.dimension(dimension);
                    if (!isListFloat(field)) {
                        throw new IllegalArgumentException("Vector field type mismatch");
                    }
                });
    }

    /**
     * 处理文本分析器
     */
    private static void processTextAnalyzer(String fieldName, MilvusField fieldAnnotation,
                                            List<AddFieldReq> milvusFields, List<IndexParam> indexParams,
                                            List<CreateCollectionReq.Function> functions) {
        Map<String, Object> analyzerParams = AnalyzerParamsUtils.convertToMap(fieldAnnotation.analyzerParams());
        log.info("Analyzer params: {}", GsonUtil.toJson(analyzerParams));

        // 构建稀疏向量字段
        String sparseFieldName = fieldName + SPARSE_SUFFIX;
        AddFieldReq sparseField = AddFieldReq.builder()
                .fieldName(sparseFieldName)
                .dataType(DataType.SparseFloatVector)
                .build();
        milvusFields.add(sparseField);

        // 构建稀疏向量索引
        IndexParam sparseIndex = IndexParam.builder()
                .indexName(fieldName + SPARSE_INDEX_SUFFIX)
                .fieldName(sparseFieldName)
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.BM25)
                .build();
        indexParams.add(sparseIndex);

        // 构建BM25函数
        CreateCollectionReq.Function bm25Function = CreateCollectionReq.Function.builder()
                .name(fieldName + BM25_SUFFIX)
                .functionType(FunctionType.BM25)
                .inputFieldNames(List.of(fieldName))
                .outputFieldNames(List.of(sparseFieldName))
                .build();
        functions.add(bm25Function);
    }

    /**
     * 缓存转换结果
     */
    private static void cacheConversionResult(Class<?> entityClass, MilvusEntity milvusEntity, ConversionContext context) {
        ConversionCache conversionCache = new ConversionCache(
                context.collectionName,
                context.propertyCache,
                milvusEntity,
                context.autoID
        );
        MilvusCache.milvusCache.put(entityClass.getName(), conversionCache);
    }

    /**
     * 递归获取类及其所有父类的所有字段
     *
     * @param clazz 要检查的类
     * @return 包含所有字段的列表
     */
    public static List<Field> getAllFieldsFromClass(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Stream.of(clazz.getDeclaredFields())
                    .peek(field -> field.setAccessible(true))
                    .toList());
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 根据字段信息和字段名称创建索引参数对象
     *
     * @param field     字段对象
     * @param fieldName 字段名称
     * @return 索引参数对象的Optional
     */
    private static Optional<IndexParam> createIndexParam(Field field, String fieldName) {
        MilvusIndex fieldAnnotation = field.getAnnotation(MilvusIndex.class);
        if (fieldAnnotation == null) {
            return Optional.empty();
        }

        Map<String, Object> extraParams = Optional.ofNullable(fieldAnnotation.extraParams())
                .stream()
                .flatMap(Arrays::stream)
                .collect(Collectors.toMap(
                        ExtraParam::key,
                        ExtraParam::value,
                        (existing, replacement) -> replacement
                ));

        IndexParam indexParam = IndexParam.builder()
                .indexName(fieldAnnotation.indexName().isEmpty() ? fieldName : fieldAnnotation.indexName())
                .fieldName(fieldName)
                .indexType(fieldAnnotation.indexType())
                .metricType(fieldAnnotation.metricType())
                .extraParams(extraParams)
                .build();

        return Optional.of(indexParam);
    }

    /**
     * 生成getter方法名
     */
    public static String getGetMethodName(Field field) {
        if (field == null) {
            throw new IllegalArgumentException("Field must not be null");
        }

        String prefix = (field.getType() == boolean.class || field.getType() == Boolean.class)
                ? IS_PREFIX : GET_PREFIX;
        String fieldName = capitalizeFirstLetter(field.getName());
        return prefix + fieldName;
    }

    /**
     * 首字母大写
     */
    private static String capitalizeFirstLetter(String original) {
        if (original == null || original.isEmpty()) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    /**
     * 创建集合
     */
    public static void create(MilvusEntity milvusEntity, MilvusClientV2 client) {
        validateIndexParams(milvusEntity.indexParams());

        CollectionSchemaBuilder schemaBuilder = new CollectionSchemaBuilder(
                milvusEntity.enableDynamicField(),
                milvusEntity.collectionName(),
                client
        );

        // 构建Schema
        buildCollectionSchema(milvusEntity, schemaBuilder);

        // 创建分区
        createPartitions(milvusEntity, client);
    }

    /**
     * 验证索引参数
     */
    private static void validateIndexParams(List<IndexParam> indexParams) {
        if (CollectionUtils.isEmpty(indexParams)) {
            throw new IllegalArgumentException("Index does not exist, please define the index");
        }
    }

    /**
     * 构建集合Schema
     */
    private static void buildCollectionSchema(MilvusEntity milvusEntity, CollectionSchemaBuilder schemaBuilder) {
        schemaBuilder.setDescription(milvusEntity.description());
        schemaBuilder.addField(milvusEntity.milvusFields());
        schemaBuilder.addConsistencyLevel(milvusEntity.consistencyLevel());
        schemaBuilder.addFun(milvusEntity.functions());

        log.info("Creating collection schema...");
        schemaBuilder.createSchema();

        log.info("Creating collection functions...");
        schemaBuilder.createIndex(milvusEntity.indexParams());

        log.info("Creating collection index...");
    }

    /**
     * 创建分区
     */
    private static void createPartitions(MilvusEntity milvusEntity, MilvusClientV2 client) {
        List<String> partitionNames = milvusEntity.partitionName();
        if (CollectionUtils.isEmpty(partitionNames)) {
            return;
        }

        for (String partitionName : partitionNames) {
            CreatePartitionReq request = CreatePartitionReq.builder()
                    .collectionName(milvusEntity.collectionName())
                    .partitionName(partitionName)
                    .build();
            client.createPartition(request);
            log.info("Created partition: {}", partitionName);
        }
    }

    /**
     * 加载状态处理
     */
    public static void loadStatus(MilvusEntity milvusEntity, MilvusClientV2 client) {
        // 处理集合加载状态
        handleCollectionLoadStatus(milvusEntity, client);

        // 处理分区加载状态
        handlePartitionLoadStatus(milvusEntity, client);
    }

    /**
     * 处理集合加载状态
     */
    private static void handleCollectionLoadStatus(MilvusEntity milvusEntity, MilvusClientV2 client) {
        GetLoadStateReq getLoadStateReq = GetLoadStateReq.builder()
                .collectionName(milvusEntity.collectionName())
                .build();

        Boolean isLoaded = client.getLoadState(getLoadStateReq);
        log.info("Collection load state: {}", isLoaded);

        if (!isLoaded) {
            LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
                    .collectionName(milvusEntity.collectionName())
                    .build();
            client.loadCollection(loadCollectionReq);
            log.info("Loaded collection: {}", milvusEntity.collectionName());
        }
    }

    /**
     * 处理分区加载状态
     */
    private static void handlePartitionLoadStatus(MilvusEntity milvusEntity, MilvusClientV2 client) {
        List<String> partitionNames = milvusEntity.partitionName();
        if (CollectionUtils.isEmpty(partitionNames)) {
            return;
        }

        // 检查并创建分区
        for (String partitionName : partitionNames) {
            ensurePartitionExists(milvusEntity.collectionName(), partitionName, client);
        }

        // 加载分区
        LoadPartitionsReq loadPartitionsReq = LoadPartitionsReq.builder()
                .collectionName(milvusEntity.collectionName())
                .partitionNames(partitionNames)
                .build();
        client.loadPartitions(loadPartitionsReq);
        log.info("Loaded partitions: {}", partitionNames);
    }

    /**
     * 确保分区存在
     */
    private static void ensurePartitionExists(String collectionName, String partitionName, MilvusClientV2 client) {
        HasPartitionReq hasPartitionReq = HasPartitionReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .build();

        Boolean hasPartition = client.hasPartition(hasPartitionReq);
        log.info("Partition exists: {} -> {}", partitionName, hasPartition);

        if (!hasPartition) {
            CreatePartitionReq createPartitionReq = CreatePartitionReq.builder()
                    .collectionName(collectionName)
                    .partitionName(partitionName)
                    .build();
            client.createPartition(createPartitionReq);
            log.info("Created partition: {}", partitionName);
        }
    }

    /**
     * 判断字段是否是 List<Float> 类型
     *
     * @param field 要检查的字段
     * @return 如果字段是 List<Float> 类型返回 true，否则返回 false
     */
    public static boolean isListFloat(Field field) {
        if (field == null) {
            return false;
        }

        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return false;
        }

        Type rawType = parameterizedType.getRawType();
        if (!(rawType instanceof Class<?> rawClass) || !List.class.isAssignableFrom(rawClass)) {
            return false;
        }

        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        return typeArguments.length == 1 && typeArguments[0] == Float.class;
    }

    /**
     * 转换上下文类
     */
    private static class ConversionContext {
        private final Class<?> entityClass;
        private final String collectionName;
        private final String description;
        private final List<String> alias;
        private final ConsistencyLevel consistencyLevel;
        private final boolean enableDynamicField;
        private PropertyCache propertyCache;
        private boolean autoID = false;

        public ConversionContext(Class<?> entityClass, MilvusCollection collectionAnnotation) {
            this.entityClass = entityClass;
            this.collectionName = collectionAnnotation.name();
            this.description = collectionAnnotation.description();
            this.alias = Arrays.asList(collectionAnnotation.alias());
            this.consistencyLevel = collectionAnnotation.level();
            this.enableDynamicField = collectionAnnotation.enableDynamicField();
        }
    }

    /**
     * 字段处理结果类
     */
    private record FieldProcessingResult(
            List<AddFieldReq> milvusFields,
            List<IndexParam> indexParams,
            List<CreateCollectionReq.Function> functions) {
    }
}