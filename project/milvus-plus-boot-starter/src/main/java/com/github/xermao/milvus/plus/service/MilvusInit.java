package com.github.xermao.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;
import com.github.xermao.milvus.plus.config.MilvusPropertiesConfiguration;
import com.github.xermao.milvus.plus.logger.LogLevelController;
import com.github.xermao.milvus.plus.model.MilvusProperties;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class MilvusInit extends AbstractMilvusClientBuilder implements InitializingBean, DisposableBean {

    private final MilvusPropertiesConfiguration milvusPropertiesConfiguration;

    private MilvusClientV2 client;

    public MilvusInit(MilvusPropertiesConfiguration milvusPropertiesConfiguration) {
        this.milvusPropertiesConfiguration = milvusPropertiesConfiguration;
    }

    @Override
    public void afterPropertiesSet() {
        initialize();
    }

    @Override
    public void destroy() throws Exception {
        // super.close();
    }

    public void initialize() {
        maybePrintBanner();
        LogLevelController.setLoggingEnabledForPackage("com.github.xermao.milvus.plus",
                milvusPropertiesConfiguration.isOpenLog(),
                milvusPropertiesConfiguration.getLogLevel());
        MilvusProperties milvusProperties = new MilvusProperties(
                milvusPropertiesConfiguration.isEnable(), milvusPropertiesConfiguration.getUri(),
                milvusPropertiesConfiguration.getDbName(), milvusPropertiesConfiguration.getUsername(),
                milvusPropertiesConfiguration.getPassword(), milvusPropertiesConfiguration.getToken(),
                milvusPropertiesConfiguration.getPackages()
        );
        BeanUtils.copyProperties(milvusPropertiesConfiguration, milvusProperties);
        properties = milvusProperties;
        super.initialize();
        client = getClient();
    }

    @Bean
    public MilvusClientV2 milvusClientV2() {
        return client;
    }

    public void maybePrintBanner() {
        if (milvusPropertiesConfiguration.isBanner()) {
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