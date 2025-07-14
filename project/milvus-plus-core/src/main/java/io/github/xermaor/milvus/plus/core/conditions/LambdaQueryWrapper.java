package io.github.xermaor.milvus.plus.core.conditions;

import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.EmbeddedText;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.request.ranker.BaseRanker;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import io.github.xermaor.milvus.plus.cache.ConversionCache;
import io.github.xermaor.milvus.plus.converter.SearchRespConverter;
import io.github.xermaor.milvus.plus.core.FieldFunction;
import io.github.xermaor.milvus.plus.model.vo.MilvusResp;
import io.github.xermaor.milvus.plus.model.vo.MilvusResult;
import io.github.xermaor.milvus.plus.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索构建器内部类，用于构建搜索请求
 */
public class LambdaQueryWrapper<T> extends AbstractChainWrapper<T, LambdaQueryWrapper<T>> implements Wrapper<LambdaQueryWrapper<T>, T> {

    private final static Logger log = LoggerFactory.getLogger(LambdaQueryWrapper.class);

    private ConversionCache conversionCache;
    private List<String> outputFields;
    private Class<T> entityType;
    private String collectionName;
    private String collectionAlias;
    private final List<String> partitionNames = new ArrayList<>();

    private String annsField;
    private int topK;
    private final List<BaseVector> vectors = new ArrayList<>();
    private long offset;
    private long limit;
    private int roundDecimal = -1;
    private long guaranteeTimestamp;
    private ConsistencyLevel consistencyLevel;
    private Boolean ignoreGrowing;
    private MilvusClientV2 client;
    private final Map<String, Object> searchParams = new HashMap<>(16);

    private final List<LambdaQueryWrapper<T>> hybridWrapper = new ArrayList<>();

    private BaseRanker ranker;

    private long gracefulTime;
    private String groupByFieldName;

    private Integer groupSize;
    private Boolean strictGroupSize;

    public LambdaQueryWrapper() {

    }

    public LambdaQueryWrapper<T> hybrid(LambdaQueryWrapper<T> wrapper) {
        this.hybridWrapper.add(wrapper);
        return this;
    }

    /**
     * 添加集合别名
     *
     * @param collectionAlias 别名
     * @return this
     */
    public LambdaQueryWrapper<T> alias(String collectionAlias) {
        this.collectionAlias = collectionAlias;
        return this;
    }

    public LambdaQueryWrapper<T> partition(String... partitionName) {
        this.partitionNames.addAll(Arrays.asList(partitionName));
        return this;
    }

    public LambdaQueryWrapper<T> consistencyLevel(ConsistencyLevel level) {
        this.consistencyLevel = level;
        return this;
    }

    public LambdaQueryWrapper<T> partition(FieldFunction<T, ?>... partitionName) {
        return partition(List.of(partitionName));
    }

    public LambdaQueryWrapper<T> partition(Collection<FieldFunction<T, ?>> partitionName) {
        if (CollectionUtils.isEmpty(partitionName)) {
            throw new RuntimeException("partition collection is empty");
        }
        partitionName.forEach(f -> this.partitionNames.add(f.getFieldName(f)));
        return this;
    }

    public LambdaQueryWrapper<T> searchParams(Map<String, Object> searchParams) {
        this.searchParams.putAll(searchParams);
        return this;
    }

    public LambdaQueryWrapper<T> radius(Object radius) {
        this.searchParams.put("radius", radius);
        return this;
    }

    public LambdaQueryWrapper<T> rangeFilter(Object rangeFilter) {
        this.searchParams.put("range_filter", rangeFilter);
        return this;
    }

    public LambdaQueryWrapper<T> metricType(Object metricType) {
        this.searchParams.put("metric_type", metricType);
        return this;
    }

    public LambdaQueryWrapper<T> roundDecimal(int roundDecimal) {
        this.roundDecimal = roundDecimal;
        return this;
    }

    @Override
    protected LambdaQueryWrapper<T> createNewInstance() {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.setEntityType(entityType);
        return wrapper;
    }

    public LambdaQueryWrapper<T> annsField(String annsField) {
        this.annsField = annsField;
        return this;
    }

    public LambdaQueryWrapper<T> annsField(FieldFunction<T, ?> annsField) {
        this.annsField = annsField.getFieldName(annsField);
        return this;
    }

    public LambdaQueryWrapper<T> ranker(BaseRanker ranker) {
        this.ranker = ranker;
        return this;
    }

    public LambdaQueryWrapper<T> vector(List<Float> vector) {
        BaseVector baseVector = new FloatVec(vector);
        vectors.add(baseVector);
        return this;
    }

    public LambdaQueryWrapper<T> vector(String annsField, List<Float> vector) {
        this.annsField = annsField;
        BaseVector baseVector = new FloatVec(vector);
        vectors.add(baseVector);
        return this;
    }

    public LambdaQueryWrapper<T> vector(FieldFunction<T, ?> annsField, List<Float> vector) {
        this.annsField = annsField.getFieldName(annsField);
        BaseVector baseVector = new FloatVec(vector);
        vectors.add(baseVector);
        return this;
    }

    public LambdaQueryWrapper<T> textVector(FieldFunction<T, ?> annsField, String vector) {
        this.annsField = annsField.getFieldName(annsField) + "_sparse";
        BaseVector baseVector = new EmbeddedText(vector);
        vectors.add(baseVector);
        return this;
    }

    public LambdaQueryWrapper<T> textVector(String annsField, String vector) {
        this.annsField = annsField + "_sparse";
        BaseVector baseVector = new EmbeddedText(vector);
        vectors.add(baseVector);
        return this;
    }

    public LambdaQueryWrapper<T> vector(BaseVector vector) {
        vectors.add(vector);
        return this;
    }

    public LambdaQueryWrapper<T> vector(String annsField, BaseVector vector) {
        this.annsField = annsField;
        vectors.add(vector);
        return this;
    }

    public LambdaQueryWrapper<T> vector(FieldFunction<T, ?> annsField, BaseVector vector) {
        this.annsField = annsField.getFieldName(annsField);
        vectors.add(vector);
        return this;
    }

    public LambdaQueryWrapper<T> limit(Long limit) {
        this.limit = limit;
        return this;
    }

    public LambdaQueryWrapper<T> offset(Long offset) {
        this.offset = offset;
        return this;
    }

    public LambdaQueryWrapper<T> topK(Integer topK) {
        this.topK = topK;
        return this;
    }

    /**
     * @param fieldName 按指定字段对搜索结果进行分组
     */
    public LambdaQueryWrapper<T> groupBy(String fieldName) {
        this.groupByFieldName = fieldName;
        return this;
    }

    public LambdaQueryWrapper<T> groupBy(FieldFunction<T, ?> fieldName) {
        this.groupByFieldName = fieldName.getFieldName(fieldName);
        return this;
    }

    // 设置保证时间戳
    public LambdaQueryWrapper<T> guaranteeTimestamp(long guaranteeTimestamp) {
        this.guaranteeTimestamp = guaranteeTimestamp;
        return this;
    }

    // 设置优雅的时间（毫秒）
    public LambdaQueryWrapper<T> gracefulTime(long gracefulTime) {
        this.gracefulTime = gracefulTime;
        return this;
    }

    // 设置是否忽略增长的段
    public LambdaQueryWrapper<T> ignoreGrowing(boolean ignoreGrowing) {
        this.ignoreGrowing = ignoreGrowing;
        return this;
    }

    // 设置分组搜索中每组内返回的实体目标数量
    public LambdaQueryWrapper<T> groupSize(Integer groupSize) {
        this.groupSize = groupSize;
        return this;
    }

    // 设置是否严格执行groupSize
    public LambdaQueryWrapper<T> strictGroupSize(Boolean strictGroupSize) {
        this.strictGroupSize = strictGroupSize;
        return this;
    }

    /**
     * 构建完整的搜索请求
     * @return 搜索请求对象
     */
    private SearchReq buildSearch() {
        SearchReq.SearchReqBuilder<?, ?> builder = SearchReq.builder()
                .collectionName(StringUtils.isNotBlank(collectionAlias) ? collectionAlias : collectionName);
        if (annsField != null && !annsField.isEmpty()) {
            builder.annsField(annsField);
        }
        if (consistencyLevel != null) {
            builder.consistencyLevel(consistencyLevel);
        }
        if (!vectors.isEmpty()) {
            builder.data(vectors);
        }
        String filterStr = build();
        if (filterStr != null && !filterStr.isEmpty()) {
            builder.filter(filterStr);
        }
        if (topK > 0) {
            builder.topK(topK);
        }
        if (limit > 0) {
            builder.limit(limit);
        }
        if (offset > 0) {
            builder.offset(offset);
        }
        if (!CollectionUtils.isEmpty(partitionNames)) {
            builder.partitionNames(partitionNames);
        }
        if (outputFields != null && !outputFields.isEmpty()) {
            builder.outputFields(outputFields);
        } else {
            Collection<String> values = conversionCache.propertyCache().functionToPropertyMap.values();
            builder.outputFields(new ArrayList<>(values));
        }
        if (!searchParams.isEmpty()) {
            builder.searchParams(searchParams);
        }
        if (roundDecimal != -1) {
            builder.roundDecimal(roundDecimal);
        }
        if (guaranteeTimestamp > 0L) {
            builder.guaranteeTimestamp(guaranteeTimestamp);
        }
        if (gracefulTime > 0L) {
            builder.gracefulTime(gracefulTime);
        }
        if (ignoreGrowing != null) {
            builder.ignoreGrowing(ignoreGrowing);
        }
        if (groupByFieldName != null && !groupByFieldName.isEmpty()) {
            builder.groupByFieldName(groupByFieldName);
        }
        if (groupSize != null && groupSize > 0) {
            builder.groupSize(groupSize);
        }
        if (strictGroupSize != null) {
            builder.strictGroupSize(strictGroupSize);
        }
        // Set other parameters as needed
        return builder.build();
    }

    private QueryReq buildQuery() {
        QueryReq.QueryReqBuilder<?, ?> builder = QueryReq.builder()
                .collectionName(StringUtils.isNotBlank(collectionAlias) ? collectionAlias : collectionName);
        String filterStr = build();
        if (StringUtils.isNotBlank(filterStr)) {
            builder.filter(filterStr);
        }
        if (topK > 0) {
            builder.limit(topK);
        }
        if (limit > 0L) {
            builder.limit(limit);
        }
        if (offset > 0) {
            builder.offset(offset);
        }
        if (consistencyLevel != null) {
            builder.consistencyLevel(consistencyLevel);
        }
        if (CollectionUtils.isNotEmpty(partitionNames)) {
            builder.partitionNames(partitionNames);
        }
        if (outputFields != null && !outputFields.isEmpty()) {
            builder.outputFields(outputFields);
        } else {
            Collection<String> values = conversionCache.propertyCache().functionToPropertyMap.values();
            builder.outputFields(new ArrayList<>(values));
        }
        return builder.build();
    }

    private HybridSearchReq buildHybrid() {
        //混合查询
        List<AnnSearchReq> searchRequests = hybridWrapper.stream().filter(v -> StringUtils.isNotEmpty(v.annsField) && !v.vectors.isEmpty()).map(
                v -> {
                    AnnSearchReq.AnnSearchReqBuilder<?, ?> annBuilder = AnnSearchReq.builder()
                            .vectorFieldName(v.annsField)
                            .vectors(v.vectors);
                    if (v.topK > 0) {
                        annBuilder.topK(v.topK);
                    }
                    String expr = v.build();
                    if (StringUtils.isNotEmpty(expr)) {
                        annBuilder.expr(expr);
                    }
                    Map<String, Object> params = v.searchParams;
                    if (!params.isEmpty()) {
                        annBuilder.params(GsonUtil.toJson(params));
                    }
                    return annBuilder.build();
                }
        ).collect(Collectors.toList());
        HybridSearchReq.HybridSearchReqBuilder<?, ?> reqBuilder = HybridSearchReq.builder()
                .collectionName(collectionName)
                .searchRequests(searchRequests);
        if (ranker != null) {
            reqBuilder.ranker(ranker);
        }
        if (topK > 0) {
            reqBuilder.topK(topK);
        }
        if (consistencyLevel != null) {
            reqBuilder.consistencyLevel(consistencyLevel);
        }
        if (outputFields != null && !outputFields.isEmpty()) {
            reqBuilder.outFields(outputFields);
        } else {
            Collection<String> values = conversionCache.propertyCache().functionToPropertyMap.values();
            reqBuilder.outFields(new ArrayList<>(values));
        }
        if (!CollectionUtils.isEmpty(partitionNames)) {
            reqBuilder.partitionNames(partitionNames);
        }
        if (roundDecimal != -1) {
            reqBuilder.roundDecimal(roundDecimal);
        }
        return reqBuilder.build();
    }

    /**
     * 执行搜索
     *
     * @return 搜索响应对象
     */
    public MilvusResp<List<MilvusResult<T>>> query() throws MilvusException {
        return executeWithRetry(
                () -> {
                    if (CollectionUtils.isNotEmpty(hybridWrapper)) {
                        HybridSearchReq hybridSearchReq = buildHybrid();
                        log.info("Build HybridSearch Param--> {}", GsonUtil.toJson(hybridSearchReq));
                        SearchResp searchResp = client.hybridSearch(hybridSearchReq);
                        return SearchRespConverter.convertSearchRespToMilvusResp(searchResp, entityType);
                    }
                    if (!vectors.isEmpty()) {
                        SearchReq searchReq = buildSearch();
                        log.info("Build Search Param--> {}", GsonUtil.toJson(searchReq));
                        SearchResp searchResp = client.search(searchReq);
                        return SearchRespConverter.convertSearchRespToMilvusResp(searchResp, entityType);
                    } else {
                        QueryReq queryReq = buildQuery();
                        log.info("Build Query param--> {}", GsonUtil.toJson(queryReq));
                        QueryResp queryResp = client.query(queryReq);
                        return SearchRespConverter.convertGetRespToMilvusResp(queryResp, entityType);
                    }
                },
                "collection not loaded",
                maxRetries,
                entityType,
                client
        );
    }


    public MilvusResp<List<MilvusResult<T>>> query(FieldFunction<T, ?>... outputFields) throws MilvusException {
        List<String> otf = new ArrayList<>();
        for (FieldFunction<T, ?> outputField : outputFields) {
            otf.add(outputField.getFieldName(outputField));
        }
        this.outputFields = otf;
        return query();
    }

    public MilvusResp<Long> count() throws MilvusException {
        this.outputFields = new ArrayList<>();
        this.outputFields.add("count(*)");
        return executeWithRetry(
                () -> {
                    QueryReq queryReq = buildQuery();
                    log.info("Build Query param--> {}", GsonUtil.toJson(queryReq));
                    QueryResp queryResp = client.query(queryReq);
                    return SearchRespConverter.convertGetRespToCount(queryResp);
                },
                "collection not loaded",
                maxRetries,
                entityType,
                client
        );

    }

    public MilvusResp<List<MilvusResult<T>>> query(String... outputFields) throws MilvusException {
        this.outputFields = Arrays.stream(outputFields).collect(Collectors.toList());
        return query();
    }

    public MilvusResp<List<MilvusResult<T>>> getById(Serializable... ids) {
        GetReq.GetReqBuilder<?, ?> builder = GetReq.builder()
                .collectionName(collectionName)
                .ids(Arrays.asList(ids));
        if (!CollectionUtils.isEmpty(partitionNames)) {
            builder.partitionName(partitionNames.get(0));
        }
        GetReq getReq = builder.build();
        GetResp getResp = client.get(getReq);

        return SearchRespConverter.convertGetRespToMilvusResp(getResp, entityType);
    }

    @Override
    public void init(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        this.client = client;
        this.collectionName = collectionName;
        this.entityType = entityType;
        this.conversionCache = conversionCache;
    }

    @Override
    public LambdaQueryWrapper<T> wrapper() {
        return this;
    }

    public void setEntityType(Class<T> entityType) {
        this.entityType = entityType;
    }
}