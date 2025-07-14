package com.github.xermao.milvus.plus;

import com.github.xermao.milvus.plus.entity.MilvusPropertiesConfiguration;
import com.github.xermao.milvus.plus.service.MilvusInit;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

public class XPlugin implements Plugin {

    public void start(AppContext context) throws Throwable {
        context.beanMake(MilvusPropertiesConfiguration.class);
        context.beanMake(MilvusInit.class);
    }
}
