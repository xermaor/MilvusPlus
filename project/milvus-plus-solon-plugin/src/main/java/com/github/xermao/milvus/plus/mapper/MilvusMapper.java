package com.github.xermao.milvus.plus.mapper;

import io.milvus.v2.client.MilvusClientV2;
import com.github.xermao.milvus.plus.core.mapper.BaseMilvusMapper;
import org.noear.solon.Solon;

public class MilvusMapper<T> extends BaseMilvusMapper<T> {

    private MilvusClientV2 client;

    @Override
    public MilvusClientV2 getClient() {
        if (client == null) {
            client = Solon.context().getBean(MilvusClientV2.class);
        }
        return client;
    }
}
