package io.github.xermaor.milvus.plus.core.conditions;

import com.google.gson.JsonObject;
import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.response.InsertResp;
import org.apache.commons.lang3.StringUtils;
import io.github.xermaor.milvus.plus.cache.CollectionToPrimaryCache;
import io.github.xermaor.milvus.plus.cache.ConversionCache;
import io.github.xermaor.milvus.plus.cache.MilvusCache;
import io.github.xermaor.milvus.plus.cache.PropertyCache;
import io.github.xermaor.milvus.plus.core.FieldFunction;
import io.github.xermaor.milvus.plus.model.vo.MilvusResp;
import io.github.xermaor.milvus.plus.util.GsonUtil;
import io.github.xermaor.milvus.plus.util.IdWorkerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 构建器内部类，用于构建insert请求
 */
public class LambdaInsertWrapper<T> extends AbstractChainWrapper<T, LambdaInsertWrapper<T>> implements Wrapper<LambdaInsertWrapper<T>, T> {

    private final static Logger log = LoggerFactory.getLogger(LambdaInsertWrapper.class);

    private Class<T> entityType;
    private String collectionName;
    private String partitionName;
    private MilvusClientV2 client;
    private final JsonObject entity = new JsonObject();

    /**
     * 向当前对象中添加字段名与值的映射，适用于插入操作的构建。
     *
     * @param fieldName 字段的函数引用，表示目标字段
     * @param value     字段对应的值
     * @return 返回当前的 LambdaInsertWrapper 对象，支持链式调用
     */
    public LambdaInsertWrapper<T> put(FieldFunction<T, ?> fieldName, Object value) {
        GsonUtil.put(this.entity, fieldName.getFieldName(fieldName), value);
        return this;
    }

    /**
     * 添加字段名与值的映射，用于插入操作的构建。
     *
     * @param fieldName 字段名，表示目标字段
     * @param value     字段对应的值
     * @return 返回当前的 LambdaInsertWrapper 对象，支持链式调用
     */
    public LambdaInsertWrapper<T> put(String fieldName, Object value) {
        GsonUtil.put(this.entity, fieldName, value);
        return this;
    }

    /**
     * 设置分区名称。
     *
     * @param partitionName 分区名称，用于指定数据插入的目标分区
     * @return 返回当前的 LambdaInsertWrapper 对象，支持链式调用
     */
    public LambdaInsertWrapper<T> partition(String partitionName) {
        this.partitionName = partitionName;
        return this;
    }

    /**
     * 设置分区名称，用于指定数据插入的目标分区。
     *
     * @param partitionName 分区名称对应的字段函数引用，用于提取字段名称
     * @return 返回当前的 LambdaInsertWrapper 对象，支持链式调用
     */
    public LambdaInsertWrapper<T> partition(FieldFunction<T, ?> partitionName) {
        this.partitionName = partitionName.getFieldName(partitionName);
        return this;
    }

    /**
     * 构建完整的insert请求
     * @return 搜索请求对象
     */
    public MilvusResp<InsertResp> insert() {
        if (!entity.isJsonNull()) {
            return insert(Collections.singletonList(entity));
        }
        throw new MilvusException("not insert data", 400);
    }


    private MilvusResp<InsertResp> insert(List<JsonObject> jsonObjects) {
        return executeWithRetry(
                () -> {
                    log.info("insert data size--->{}", jsonObjects.size());
                    InsertReq.InsertReqBuilder<?, ?> builder = InsertReq.builder()
                            .collectionName(collectionName)
                            .data(jsonObjects);
                    if (StringUtils.isNotEmpty(partitionName)) {
                        builder.partitionName(partitionName);
                    }
                    InsertReq insertReq = builder
                            .build();
                    InsertResp insert = client.insert(insertReq);
                    return new MilvusResp<>(true, insert);
                },
                "collection not loaded",
                maxRetries,
                entityType,
                client
        );
    }

    @SafeVarargs
    public final MilvusResp<InsertResp> insert(T... entity) throws MilvusException {
        return insert(List.of(entity));
    }

    /**
     * 插入实体对象集合到 Milvus 数据库。
     *
     * @param collection 实体对象的迭代器，用于批量插入数据。实体对象为用户定义的类型 T，每个对象将被转换为对应的数据库记录。
     * @return 返回一个封装插入结果的 MilvusResp 对象，其中包含插入的响应信息，如成功与否及相关数据。
     * @throws MilvusException 如果插入操作过程中发生错误，将抛出此异常。
     */
    public MilvusResp<InsertResp> insert(Collection<T> collection) throws MilvusException {
        ConversionCache conversionCache = MilvusCache.milvusCache.get(entityType.getName());
        PropertyCache propertyCache = conversionCache.propertyCache();
        String pk = CollectionToPrimaryCache.collectionToPrimary.get(collectionName);
        List<JsonObject> jsonObjects = new ArrayList<>();
        for (T item : collection) {
            Map<String, Object> propertiesMap = getPropertiesMap(item);
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String tk = propertyCache.functionToPropertyMap.get(key);
                if (StringUtils.isNotEmpty(tk)) {
                    GsonUtil.put(jsonObject, tk, value);
                }
            }
            if (conversionCache.autoID()) {
                GsonUtil.put(jsonObject, pk, IdWorkerUtils.nextId());
            }
            jsonObjects.add(jsonObject);
        }
        return insert(jsonObjects);
    }

    @Override
    public void init(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        this.client = client;
        this.collectionName = collectionName;
        this.entityType = entityType;
    }

    @Override
    public LambdaInsertWrapper<T> wrapper() {
        return this;
    }

    @Override
    protected LambdaInsertWrapper<T> createNewInstance() {
        LambdaInsertWrapper<T> wrapper = new LambdaInsertWrapper<>();
        wrapper.setEntityType(entityType);
        return wrapper;
    }

    public void setEntityType(Class<T> entityType) {
        this.entityType = entityType;
    }
}