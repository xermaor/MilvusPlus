package io.github.xermaor.milvus.plus.converter;

import com.google.gson.JsonObject;
import io.github.xermaor.milvus.plus.cache.ConversionCache;
import io.github.xermaor.milvus.plus.cache.MilvusCache;
import io.github.xermaor.milvus.plus.cache.PropertyCache;
import io.github.xermaor.milvus.plus.model.vo.MilvusResp;
import io.github.xermaor.milvus.plus.model.vo.MilvusResult;
import io.github.xermaor.milvus.plus.util.GsonUtil;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author xermao
 **/
public class SearchRespConverter {

    /**
     * 将SearchResp对象转换为自定义的MilvusResp对象，其中SearchResp是Milvus搜索响应的内部结构，
     * 而MilvusResp是对外提供的统一响应格式。该方法主要涉及将搜索结果中的每个实体从Map形式转换为指定的Java实体类T。
     *
     * @param searchResp Milvus搜索操作的原始响应对象，包含搜索结果的详细信息。
     * @param entityType 指定的Java实体类类型，用于将搜索结果的每个实体转换为该类型。
     * @return 转换后的MilvusResp对象，其中包含了列表形式的搜索结果以及操作是否成功的标志。
     */
    public static <T> MilvusResp<List<MilvusResult<T>>> convertSearchRespToMilvusResp(SearchResp searchResp, Class<T> entityType) {
        PropertyCache propertyCache = getCacheComponents(entityType);
        List<MilvusResult<T>> results = Optional.ofNullable(searchResp.getSearchResults())
                .orElseGet(ArrayList::new)
                .parallelStream()
                .flatMap(List::stream)
                .map(searchResult -> {
                    T entity = convertEntityMap(searchResult.getEntity(), entityType, propertyCache);
                    return new MilvusResult<>(entity, searchResult.getScore(), searchResult.getId(), null);
                })
                .collect(Collectors.toList());
        return new MilvusResp<>(true, results);
    }

    /**
     * 将Get响应转换为Milvus响应的通用方法。
     * @param getResp Get操作的响应对象，可以是QueryResp或GetResp类型。
     * @param entityType 实体类型，用于泛型结果的类型转换。
     * @return 返回一个包含Milvus结果列表的MilvusResp对象。
     */
    public static <T> MilvusResp<List<MilvusResult<T>>> convertGetRespToMilvusResp(QueryResp getResp, Class<T> entityType) {
        List<QueryResp.QueryResult> queryResults = getResp.getQueryResults();
        return convertQuery(queryResults, entityType);
    }

    public static MilvusResp<Long> convertGetRespToCount(QueryResp getResp) {
        List<QueryResp.QueryResult> queryResults = getResp.getQueryResults();
        return convertQueryCount(queryResults);
    }

    /**
     * 将Get响应转换为Milvus响应的通用方法。
     * @param getResp Get操作的响应对象，可以是QueryResp或GetResp类型。
     * @param entityType 实体类型，用于泛型结果的类型转换。
     * @return 返回一个包含Milvus结果列表的MilvusResp对象。
     */
    public static <T> MilvusResp<List<MilvusResult<T>>> convertGetRespToMilvusResp(GetResp getResp, Class<T> entityType) {
        List<QueryResp.QueryResult> getResults = getResp.getResults;
        return convertQuery(getResults, entityType);
    }

    /**
     * 将查询结果转换为指定类型的实体列表。
     *
     * @param queryResults 查询结果列表，来自Milvus数据库的查询响应。
     * @param entityType 需要转换成的实体类型，指定了转换的目标。
     * @return MilvusResp对象，包含转换后的实体列表。每个实体都包装在一个MilvusResult对象中，同时设置成功状态为true。
     */
    private static <T> MilvusResp<List<MilvusResult<T>>> convertQuery(List<QueryResp.QueryResult> queryResults, Class<T> entityType) {
        PropertyCache propertyCache = getCacheComponents(entityType);
        List<MilvusResult<T>> results = queryResults.parallelStream()
                .map(queryResult -> {
                    T entity = convertEntityMap(queryResult.getEntity(), entityType, propertyCache);
                    return new MilvusResult<>(entity, 0.0f, null, null);
                })
                .toList();
        return new MilvusResp<>(true, results);
    }

    private static MilvusResp<Long> convertQueryCount(List<QueryResp.QueryResult> queryResults) {
        Long total = 0L;
        for (QueryResp.QueryResult queryResult : queryResults) {
            Map<String, Object> entityMap = queryResult.getEntity();
            total = (Long) entityMap.get("count(*)");
        }
        return new MilvusResp<>(true, total);
    }

    /**
     * 获取缓存组件
     *
     * @param entityType 实体类型
     * @return 属性缓存对象
     */
    private static PropertyCache getCacheComponents(Class<?> entityType) {
        ConversionCache conversionCache = MilvusCache.milvusCache.get(entityType.getName());
        return conversionCache.propertyCache();
    }

    /**
     * 转换实体映射
     *
     * @param originalEntityMap 原始实体映射
     * @param entityType 实体类型
     * @param propertyCache 属性缓存
     * @return 转换后的实体对象
     */
    private static <T> T convertEntityMap(Map<String, Object> originalEntityMap, Class<T> entityType, PropertyCache propertyCache) {
        JsonObject convertedEntityObject = new JsonObject();

        for (Map.Entry<String, Object> entry : originalEntityMap.entrySet()) {
            String key = propertyCache.findKeyByValue(entry.getKey());
            if (key != null) {
                GsonUtil.put(convertedEntityObject, key, entry.getValue());
            }
        }

        return GsonUtil.convertToType(convertedEntityObject, entityType);
    }
}