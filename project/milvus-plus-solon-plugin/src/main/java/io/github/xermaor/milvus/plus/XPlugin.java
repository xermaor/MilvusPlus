package io.github.xermaor.milvus.plus;

import io.github.xermaor.milvus.plus.entity.MilvusPropertiesConfiguration;
import io.github.xermaor.milvus.plus.service.MilvusInit;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

public class XPlugin implements Plugin {

    public void start(AppContext context) throws Throwable {
        context.beanMake(MilvusPropertiesConfiguration.class);
        context.beanMake(MilvusInit.class);
    }
}
