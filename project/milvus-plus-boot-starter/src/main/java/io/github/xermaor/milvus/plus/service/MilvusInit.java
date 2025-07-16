package io.github.xermaor.milvus.plus.service;

import io.github.xermaor.milvus.plus.config.MilvusConfigurationProperties;
import io.github.xermaor.milvus.plus.logger.LogLevelController;
import io.milvus.v2.client.MilvusClientV2;

public class MilvusInit extends AbstractMilvusClientBuilder {

    private final MilvusConfigurationProperties properties;

    public MilvusInit(MilvusConfigurationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void initialize() {
        maybePrintBanner();
        LogLevelController.setLoggingEnabledForPackage("io.github.xermaor.milvus.plus",
                properties.isOpenLog(),
                properties.getLogLevel()
        );
        this.packages = properties.getPackages().toArray(new String[0]);
        this.initClient();
        super.initialize();
    }

    private void initClient() {
        this.client = new MilvusClientV2(properties.getConnectConfig().toConnectConfig());
        this.client.retryConfig(properties.getRetryConfig().toRetryConfig());
    }

    public void maybePrintBanner() {
        if (properties.isBanner()) {
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