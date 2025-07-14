package io.github.xermaor.milvus.plus.model;

import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;

import java.util.List;

/**
 * @author xermao
 **/
public record MilvusEntity(
        String collectionName,
        String description,
        List<String> alias,
        List<IndexParam> indexParams,
        List<AddFieldReq> milvusFields,
        List<String> partitionName,
        ConsistencyLevel consistencyLevel,
        Boolean enableDynamicField,
        List<CreateCollectionReq.Function> functions
) {
}
