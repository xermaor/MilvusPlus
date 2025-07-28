package io.github.xermaor.milvus.plus.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.RetryConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xermao
 **/
@ConfigurationProperties(prefix = "milvus")
public class MilvusConfigurationProperties {
    @NestedConfigurationProperty
    private final ConnectConfiguration connectConfig;
    private final Boolean enable;
    private final List<String> packages;
    private final Boolean openLog;
    private final String logLevel;
    private final Boolean banner;
    @NestedConfigurationProperty
    private final RetryConfiguration retryConfig;

    @ConstructorBinding
    public MilvusConfigurationProperties(
            ConnectConfiguration connectConfig, Boolean enable,
            List<String> packages, Boolean openLog, String logLevel,
            Boolean banner, RetryConfiguration retryConfig) {
        this.connectConfig = connectConfig != null ? connectConfig : new ConnectConfiguration(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        this.enable = enable != null ? enable : false;
        this.packages = packages != null ? packages : List.of();
        this.openLog = openLog != null ? openLog : false;
        this.logLevel = logLevel != null ? logLevel : "INFO";
        this.banner = banner != null ? banner : true;
        this.retryConfig = retryConfig != null ? retryConfig : new RetryConfiguration(
                null, null, null,
                null, null, null
        );
    }

    public ConnectConfiguration getConnectConfig() {
        return connectConfig;
    }

    public Boolean getEnable() {
        return enable;
    }

    public List<String> getPackages() {
        return packages;
    }

    public Boolean getOpenLog() {
        return openLog;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public Boolean getBanner() {
        return banner;
    }

    public RetryConfiguration getRetryConfig() {
        return retryConfig;
    }

    @Override
    public String toString() {
        return "MilvusConfigurationProperties{" +
                "connectConfig=" + connectConfig +
                ", enable=" + enable +
                ", packages=" + packages +
                ", openLog=" + openLog +
                ", logLevel='" + logLevel + '\'' +
                ", banner=" + banner +
                ", retryConfig=" + retryConfig +
                '}';
    }

    /**
     * @param rpcDeadlineMs  Disabling deadline */
    public record ConnectConfiguration(String uri, String token, String username, String password, String dbName,
                                       Long connectTimeoutMs, Long keepAliveTimeMs, Long keepAliveTimeoutMs,
                                       Boolean keepAliveWithoutCalls, Long rpcDeadlineMs, String clientKeyPath,
                                       String clientPemPath, String caPemPath, String serverPemPath, String serverName,
                                       String proxyAddress, Boolean secure, Long idleTimeoutMs) {
        @ConstructorBinding
        public ConnectConfiguration(
                String uri, String token, String username, String password,
                String dbName, Long connectTimeoutMs, Long keepAliveTimeMs,
                Long keepAliveTimeoutMs, Boolean keepAliveWithoutCalls,
                Long rpcDeadlineMs, String clientKeyPath, String clientPemPath,
                String caPemPath, String serverPemPath, String serverName,
                String proxyAddress, Boolean secure, Long idleTimeoutMs
        ) {
            this.uri = uri;
            this.token = token;
            this.username = username;
            this.password = password;
            this.dbName = dbName;
            this.clientKeyPath = clientKeyPath;
            this.clientPemPath = clientPemPath;
            this.caPemPath = caPemPath;
            this.serverPemPath = serverPemPath;
            this.serverName = serverName;
            this.proxyAddress = proxyAddress;
            this.connectTimeoutMs = connectTimeoutMs != null ? connectTimeoutMs : 10000;
            this.keepAliveTimeMs = keepAliveTimeMs != null ? keepAliveTimeMs : 55000;
            this.keepAliveTimeoutMs = keepAliveTimeoutMs != null ? keepAliveTimeoutMs : 20000;
            this.keepAliveWithoutCalls = keepAliveWithoutCalls != null ? keepAliveWithoutCalls : false;
            this.rpcDeadlineMs = rpcDeadlineMs != null ? rpcDeadlineMs : 0;
            this.secure = secure != null ? secure : false;
            this.idleTimeoutMs = idleTimeoutMs != null ? idleTimeoutMs : TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS);
        }

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

        @NotNull
        @Override
        public String toString() {
            return "ConnectConfiguration{" +
                    "uri='" + uri + '\'' +
                    ", token='" + token + '\'' +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", dbName='" + dbName + '\'' +
                    ", connectTimeoutMs=" + connectTimeoutMs +
                    ", keepAliveTimeMs=" + keepAliveTimeMs +
                    ", keepAliveTimeoutMs=" + keepAliveTimeoutMs +
                    ", keepAliveWithoutCalls=" + keepAliveWithoutCalls +
                    ", rpcDeadlineMs=" + rpcDeadlineMs +
                    ", clientKeyPath='" + clientKeyPath + '\'' +
                    ", clientPemPath='" + clientPemPath + '\'' +
                    ", caPemPath='" + caPemPath + '\'' +
                    ", serverPemPath='" + serverPemPath + '\'' +
                    ", serverName='" + serverName + '\'' +
                    ", proxyAddress='" + proxyAddress + '\'' +
                    ", secure=" + secure +
                    ", idleTimeoutMs=" + idleTimeoutMs +
                    '}';
        }
    }


    public record RetryConfiguration(Integer maxRetryTimes, Long initialBackOffMs, Long maxBackOffMs,
                                     Integer backOffMultiplier, Boolean retryOnRateLimit, Long maxRetryTimeoutMs) {
        @ConstructorBinding
        public RetryConfiguration(
                Integer maxRetryTimes, Long initialBackOffMs,
                Long maxBackOffMs, Integer backOffMultiplier,
                Boolean retryOnRateLimit, Long maxRetryTimeoutMs
        ) {
            this.maxRetryTimes = maxRetryTimes != null ? maxRetryTimes : 75;
            this.initialBackOffMs = initialBackOffMs != null ? initialBackOffMs : 10;
            this.maxBackOffMs = maxBackOffMs != null ? maxBackOffMs : 3000;
            this.backOffMultiplier = backOffMultiplier != null ? backOffMultiplier : 3;
            this.retryOnRateLimit = retryOnRateLimit != null ? retryOnRateLimit : true;
            this.maxRetryTimeoutMs = maxRetryTimeoutMs != null ? maxRetryTimeoutMs : 0;
        }

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

        @Override
        public String toString() {
            return "RetryConfiguration{" +
                    "maxRetryTimes=" + maxRetryTimes +
                    ", initialBackOffMs=" + initialBackOffMs +
                    ", maxBackOffMs=" + maxBackOffMs +
                    ", backOffMultiplier=" + backOffMultiplier +
                    ", retryOnRateLimit=" + retryOnRateLimit +
                    ", maxRetryTimeoutMs=" + maxRetryTimeoutMs +
                    '}';
        }
    }
}