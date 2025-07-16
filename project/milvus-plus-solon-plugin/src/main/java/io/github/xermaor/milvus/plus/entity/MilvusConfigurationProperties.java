package io.github.xermaor.milvus.plus.entity;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.RetryConfig;
import lombok.Data;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import java.util.List;

/**
 * @author xermao
 **/
@Data
@Inject("${milvus}") //see https://solon.noear.org/article/326
@Configuration
public class MilvusConfigurationProperties {
    private boolean enable;
    private ConnectConfig connectConfig;
    private List<String> packages;
    private boolean openLog;
    private String logLevel;
    private boolean banner = true;
    private RetryConfig retryConfig = RetryConfig.builder().build();
}