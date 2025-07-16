package io.github.xermaor.milvus.plus.core.conditions;

import io.github.xermaor.milvus.plus.cache.ConversionCache;
import io.milvus.v2.client.MilvusClientV2;

/**
 * 通用构建器接口
 * @param <W> 构建器类型
 * @param <T> 实体类型
 */
public interface Wrapper<W, T> {
    void init(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType);

    W wrapper();
}