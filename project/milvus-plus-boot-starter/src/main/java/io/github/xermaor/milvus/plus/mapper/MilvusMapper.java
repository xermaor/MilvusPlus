package io.github.xermaor.milvus.plus.mapper;

import io.github.xermaor.milvus.plus.config.MilvusPlusAutoConfiguration;
import io.github.xermaor.milvus.plus.core.mapper.BaseMilvusMapper;
import io.milvus.v2.client.MilvusClientV2;


public class MilvusMapper<T> extends BaseMilvusMapper<T> {
    @Override
    public MilvusClientV2 getClient() {
        return MilvusPlusAutoConfiguration.milvusInit.getClient();
    }
}
