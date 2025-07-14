package com.github.xermao.milvus.plus.cache;

import com.github.xermao.milvus.plus.model.MilvusEntity;

/**
 * @author xermao
 **/

public record ConversionCache(
        String collectionName,
        PropertyCache propertyCache,
        MilvusEntity milvusEntity,
        boolean autoID
) {}
