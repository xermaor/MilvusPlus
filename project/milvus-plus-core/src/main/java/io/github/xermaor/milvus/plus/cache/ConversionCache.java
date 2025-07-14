package io.github.xermaor.milvus.plus.cache;

import io.github.xermaor.milvus.plus.model.MilvusEntity;

/**
 * @author xermao
 **/

public record ConversionCache(
        String collectionName,
        PropertyCache propertyCache,
        MilvusEntity milvusEntity,
        boolean autoID
) {}
