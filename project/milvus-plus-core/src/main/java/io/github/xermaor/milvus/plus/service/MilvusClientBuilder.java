package io.github.xermaor.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;

public interface MilvusClientBuilder {
    /**
     * 初始化
     */
    void initialize();

    /**
     * 获取milvus客户端
     *
     * @return MilvusClientV2
     */
    MilvusClientV2 getClient();
}