package io.github.xermaor.milvus.plus.config;

import io.github.xermaor.milvus.plus.service.MilvusInit;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(MilvusConfigurationProperties.class)
public class MilvusPlusAutoConfiguration {

    public static MilvusInit milvusInit;

    @Bean
    @ConditionalOnMissingBean(MilvusClientV2.class)
    @ConditionalOnProperty(prefix = "milvus", name = "enable", havingValue = "true")
    public MilvusClientV2 milvusClientV2(MilvusConfigurationProperties properties) {
        milvusInit = new MilvusInit(properties);
        milvusInit.initialize();
        return milvusInit.getClient();
    }
}
