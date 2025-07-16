package io.github.xermaor.milvus.plus.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.RetryConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xermao
 **/
@Data
@ConfigurationProperties(prefix = "milvus")
public class MilvusConfigurationProperties {
    @NestedConfigurationProperty
    private final ConnectConfiguration connectConfig = new ConnectConfiguration();
    private boolean enable;
    private List<String> packages;
    private boolean openLog;
    private String logLevel;
    private boolean banner;
    @NestedConfigurationProperty
    private RetryConfiguration retryConfig = new RetryConfiguration();

    @Data
    public static class ConnectConfiguration {
        private String uri;
        private String token;
        private String username;
        private String password;
        private String dbName;
        private long connectTimeoutMs = 10000;
        private long keepAliveTimeMs = 55000;
        private long keepAliveTimeoutMs = 20000;
        private boolean keepAliveWithoutCalls = false;
        private long rpcDeadlineMs = 0; // Disabling deadline

        private String clientKeyPath;
        private String clientPemPath;
        private String caPemPath;
        private String serverPemPath;
        private String serverName;
        private String proxyAddress;
        private Boolean secure = false;
        private long idleTimeoutMs = TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS);

        public ConnectConfig toConnectConfig() {
            return ConnectConfig.builder()
                    .uri(this.uri)
                    .token(this.token)
                    .username(this.username)
                    .password(this.password)
                    .dbName(this.dbName)
                    .connectTimeoutMs(this.connectTimeoutMs)
                    .keepAliveTimeMs(this.keepAliveTimeMs)
                    .keepAliveTimeoutMs(this.keepAliveTimeoutMs)
                    .keepAliveWithoutCalls(this.keepAliveWithoutCalls)
                    .rpcDeadlineMs(this.rpcDeadlineMs)
                    .clientKeyPath(this.clientKeyPath)
                    .clientPemPath(this.clientPemPath)
                    .caPemPath(this.caPemPath)
                    .serverPemPath(serverPemPath)
                    .serverName(this.serverName)
                    .proxyAddress(this.proxyAddress)
                    .secure(this.secure)
                    .idleTimeoutMs(this.idleTimeoutMs)
                    .build();
        }
    }

    @Data
    public static class RetryConfiguration {
        private int maxRetryTimes = 75;
        private long initialBackOffMs = 10;
        private long maxBackOffMs = 3000;
        private int backOffMultiplier = 3;
        private boolean retryOnRateLimit = true;
        private long maxRetryTimeoutMs = 0;

        public RetryConfig toRetryConfig() {
            return RetryConfig.builder()
                    .maxRetryTimes(this.maxRetryTimes)
                    .initialBackOffMs(this.initialBackOffMs)
                    .maxBackOffMs(this.maxBackOffMs)
                    .backOffMultiplier(this.backOffMultiplier)
                    .retryOnRateLimit(this.retryOnRateLimit)
                    .maxRetryTimeoutMs(this.maxRetryTimeoutMs)
                    .build();
        }
    }
}