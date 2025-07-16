package io.github.xermaor.milvus.plus;

import io.github.xermaor.milvus.plus.entity.MilvusConfigurationProperties;
import io.github.xermaor.milvus.plus.service.MilvusInit;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

public class XPlugin implements Plugin {

    public void start(AppContext context) throws Throwable {
        context.beanMake(MilvusConfigurationProperties.class);
        context.beanMake(MilvusInit.class);
    }
}
