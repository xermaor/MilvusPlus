package io.github.xermaor.milvus.plus.service;

import io.github.xermaor.milvus.plus.entity.MilvusConfigurationProperties;
import io.github.xermaor.milvus.plus.logger.LogLevelController;
import io.milvus.v2.client.MilvusClientV2;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.bean.LifecycleBean;

@Configuration
public class MilvusInit extends AbstractMilvusClientBuilder implements LifecycleBean {

    private MilvusConfigurationProperties properties;

    //see https://solon.noear.org/article/324
    @Bean
    public MilvusClientV2 init(MilvusConfigurationProperties milvusConfigurationProperties) {
        properties = milvusConfigurationProperties;
        maybePrintBanner(milvusConfigurationProperties);
        LogLevelController.setLoggingEnabledForPackage("io.github.xermaor.milvus.plus",
                milvusConfigurationProperties.isOpenLog(),
                milvusConfigurationProperties.getLogLevel());
        this.packages = milvusConfigurationProperties.getPackages().toArray(new String[0]);
        this.initClient();
        super.initialize();
        return getClient();
    }

    private void initClient() {
        this.client = new MilvusClientV2(properties.getConnectConfig());
        this.client.retryConfig(properties.getRetryConfig());
    }

    public void start() throws Throwable {

    }

    public void stop() throws Throwable {
        //  super.close();
    }

    public void maybePrintBanner(MilvusConfigurationProperties propertiesConfiguration) {
        if (propertiesConfiguration.isBanner()) {
            printBanner();
        }
    }

    public void printBanner() {
        String banner = """
                  __  __ _ _                    ____  _          \s
                 |  \\/  (_) |_   ___   _ ___   |  _ \\| |_   _ ___\s
                 | |\\/| | | \\ \\ / / | | / __|  | |_) | | | | / __|
                 | |  | | | |\\ V /| |_| \\__ \\  |  __/| | |_| \\__ \\
                 |_|  |_|_|_| \\_/  \\__,_|___/  |_|   |_|\\__,_|___/
                
                """;

        System.out.println(banner);
    }
}