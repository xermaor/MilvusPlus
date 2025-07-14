package com.github.xermao.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;
import com.github.xermao.milvus.plus.logger.LogLevelController;
import com.github.xermao.milvus.plus.model.MilvusProperties;
import com.github.xermao.milvus.plus.service.AbstractMilvusClientBuilder;
import com.github.xermao.milvus.plus.entity.MilvusPropertiesConfiguration;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.bean.LifecycleBean;

@Configuration
public class MilvusInit extends AbstractMilvusClientBuilder implements LifecycleBean {

    //see https://solon.noear.org/article/324
    @Bean
    public MilvusClientV2 init(MilvusPropertiesConfiguration milvusPropertiesConfiguration) {
        maybePrintBanner(milvusPropertiesConfiguration);
        LogLevelController.setLoggingEnabledForPackage("com.github.xermao.milvus.plus",
                milvusPropertiesConfiguration.isOpenLog(),
                milvusPropertiesConfiguration.getLogLevel());
        this.properties=(new MilvusProperties(
                milvusPropertiesConfiguration.isEnable(), milvusPropertiesConfiguration.getUri(),
                milvusPropertiesConfiguration.getDbName(), milvusPropertiesConfiguration.getUsername(),
                milvusPropertiesConfiguration.getPassword(), milvusPropertiesConfiguration.getToken(),
                milvusPropertiesConfiguration.getPackages()
        ));
        super.initialize();
        return getClient();
    }


    public void start() throws Throwable {

    }

    public void stop() throws Throwable {
        //  super.close();
    }

    public void maybePrintBanner(MilvusPropertiesConfiguration propertiesConfiguration) {
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