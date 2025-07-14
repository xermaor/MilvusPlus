package com.github.xermao.milvus.plus.core.conditions;

import com.google.gson.JsonObject;
import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import org.apache.commons.lang3.StringUtils;
import com.github.xermao.milvus.plus.cache.CollectionToPrimaryCache;
import com.github.xermao.milvus.plus.cache.ConversionCache;
import com.github.xermao.milvus.plus.cache.PropertyCache;
import com.github.xermao.milvus.plus.core.FieldFunction;
import com.github.xermao.milvus.plus.model.vo.MilvusResp;
import com.github.xermao.milvus.plus.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 构建器内部类，用于构建update请求
 */
public class LambdaUpdateWrapper<T> extends AbstractChainWrapper<T, LambdaUpdateWrapper<T>> implements Wrapper<LambdaUpdateWrapper<T>, T> {

    private final static Logger log = LoggerFactory.getLogger(LambdaUpdateWrapper.class);

    private ConversionCache conversionCache;
    private Class<T> entityType;
    private String collectionName;
    private String partitionName;
    private MilvusClientV2 client;

    public LambdaUpdateWrapper(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        this.collectionName = collectionName;
        this.client = client;
        this.conversionCache = conversionCache;
        this.entityType = entityType;
    }

    public LambdaUpdateWrapper() {

    }

    public LambdaUpdateWrapper<T> partition(String partitionName) {
        this.partitionName = partitionName;
        return this;
    }

    public LambdaUpdateWrapper<T> partition(FieldFunction<T, ?> partitionName) {
        this.partitionName = partitionName.getFieldName(partitionName);
        return this;
    }

    @Override
    protected LambdaUpdateWrapper<T> createNewInstance() {
        LambdaUpdateWrapper<T> wrapper = new LambdaUpdateWrapper<>();
        wrapper.setEntityType(entityType);
        return wrapper;
    }

    /**
     * 构建完整的删除请求
     *
     * @return 搜索请求对象
     */
    private QueryResp buildReq() {
        String filterStr = this.build();
        if (filterStr != null && !filterStr.isEmpty()) {
            QueryReq.QueryReqBuilder<?, ?> builder = QueryReq.builder()
                    .collectionName(collectionName).filter(filterStr);
            return client.query(builder.build());
        } else {
            return null;
        }
    }

    /**
     * 执行更新
     *
     * @return 更新响应对象
     */
    public MilvusResp<UpsertResp> update(T entity) throws MilvusException {
        // 获取主键字段
        String primaryKeyField = CollectionToPrimaryCache.collectionToPrimary.get(collectionName);
        if (StringUtils.isNotEmpty(primaryKeyField)) {
            throw new MilvusException("not find primary key", 400);
        }
        // 将实体转换为属性映射
        Map<String, Object> propertiesMap = getPropertiesMap(entity);
        PropertyCache propertyCache = conversionCache.propertyCache();
        // 初始化主键标识和主键值
        boolean hasPrimaryKey = false;
        Object primaryKeyValue = null;
        // 准备更新的数据列表
        List<JsonObject> updateDataList = new ArrayList<>();
        // 构建单个更新对象
        JsonObject updateObject = new JsonObject();
        for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            String tableNameColumn = propertyCache.functionToPropertyMap.get(field);
            // 检查是否为主键字段
            if (primaryKeyField.equals(tableNameColumn)) {
                hasPrimaryKey = true;
                primaryKeyValue = value;
            }
            // 校验是否为空
            if (StringUtils.isNotEmpty(tableNameColumn)) {
                // 添加到更新对象
                GsonUtil.put(updateObject, tableNameColumn, value);
            }
        }
        // 检查是否需要构建查询条件
        boolean needBuildQuery = !hasPrimaryKey;
        if (hasPrimaryKey) {
            for (Map.Entry<String, String> property : propertyCache.functionToPropertyMap.entrySet()) {
                Boolean nullable = propertyCache.nullableToPropertyMap.get(property.getKey());
                if (updateObject.get(property.getValue()) == null && !nullable) {
                    needBuildQuery = true;
                    eq(primaryKeyField, primaryKeyValue);
                    break;
                }
            }
        }

        // 如果需要构建查询条件，则执行查询并准备更新数据
        if (needBuildQuery) {
            QueryResp queryResp = buildReq();
            if (queryResp != null) {
                for (QueryResp.QueryResult result : queryResp.getQueryResults()) {
                    Map<String, Object> existingEntity = result.getEntity();
                    JsonObject existingData = new JsonObject();
                    for (Map.Entry<String, Object> existingEntry : existingEntity.entrySet()) {
                        String existingField = existingEntry.getKey();
                        Object existingValue = existingEntry.getValue();
                        Object updateValue = updateObject.get(existingField);
                        GsonUtil.put(existingData, existingField, updateValue != null ? updateValue : existingValue);
                    }

                    updateDataList.add(existingData);
                }
            }
        } else {
            updateDataList.add(updateObject);
        }

        // 检查是否有数据需要更新
        if (updateDataList.isEmpty()) {
            return new MilvusResp<>(true, null);
        }
        // 执行更新操作
        return update(updateDataList);
    }

    private MilvusResp<UpsertResp> update(List<JsonObject> jsonObjects) {
        return executeWithRetry(
                () -> {
                    log.info("update data--->{}", GsonUtil.toJson(jsonObjects));
                    UpsertReq.UpsertReqBuilder<?, ?> builder = UpsertReq.builder()
                            .collectionName(collectionName)
                            .data(jsonObjects);
                    if (StringUtils.isNotEmpty(partitionName)) {
                        builder.partitionName(partitionName);
                    }
                    UpsertReq upsertReq = builder
                            .build();
                    UpsertResp upsert = client.upsert(upsertReq);
                    return new MilvusResp<>(true, upsert);
                },
                "collection not loaded",
                maxRetries,
                entityType,
                client
        );
    }

    @SafeVarargs
    public final MilvusResp<UpsertResp> updateById(T... entity) throws MilvusException {
        return updateById(List.of(entity));
    }

    public MilvusResp<UpsertResp> updateById(Collection<T> collection) throws MilvusException {
        PropertyCache propertyCache = conversionCache.propertyCache();
        String pk = CollectionToPrimaryCache.collectionToPrimary.get(collectionName);
        List<JsonObject> jsonObjects = new ArrayList<>();
        for (T item : collection) {
            Map<String, Object> propertiesMap = getPropertiesMap(item);
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                // 根据PropertyCache转换属性名
                String tk = propertyCache.functionToPropertyMap.get(key);
                if (StringUtils.isNotEmpty(tk)) {
                    GsonUtil.put(jsonObject, tk, value);
                }
            }
            // 检查是否包含主键
            if (!jsonObject.has(pk)) {
                throw new MilvusException("not find primary key", 400);
            }
            jsonObjects.add(jsonObject);
        }
        // 准备更新的数据列表
        List<JsonObject> updateDataList = new ArrayList<>();
        for (JsonObject updateObject : jsonObjects) {
            boolean isBuild = false;
            for (Map.Entry<String, String> property : propertyCache.functionToPropertyMap.entrySet()) {
                Boolean nullable = propertyCache.nullableToPropertyMap.get(property.getKey());
                if (updateObject.get(property.getValue()) == null && !nullable) {
                    //缺少数据需要
                    isBuild = true;
                }
            }
            if (isBuild) {
                QueryReq.QueryReqBuilder<?, ?> builder = QueryReq.builder()
                        .collectionName(collectionName).filter(pk + " == " + updateObject.get(pk));
                QueryResp queryResp = client.query(builder.build());
                if (queryResp != null) {
                    for (QueryResp.QueryResult result : queryResp.getQueryResults()) {
                        Map<String, Object> existingEntity = result.getEntity();
                        JsonObject existingData = new JsonObject();
                        for (Map.Entry<String, Object> existingEntry : existingEntity.entrySet()) {
                            String existingField = existingEntry.getKey();
                            Object existingValue = existingEntry.getValue();
                            Object updateValue = updateObject.get(existingField);
                            GsonUtil.put(existingData, existingField, updateValue != null ? updateValue : existingValue);
                        }
                        updateDataList.add(existingData);
                    }
                }
            } else {
                updateDataList.add(updateObject);
            }
        }
        return update(updateDataList);
    }

    @Override
    public void init(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        this.client = client;
        this.collectionName = collectionName;
        this.entityType = entityType;
        this.conversionCache = conversionCache;
    }

    @Override
    public LambdaUpdateWrapper<T> wrapper() {
        return this;
    }

    public void setEntityType(Class<T> entityType) {
        this.entityType = entityType;
    }
}