package io.github.xermaor.milvus.plus.core.conditions;

import io.milvus.v2.client.MilvusClientV2;
import io.github.xermaor.milvus.plus.cache.ConversionCache;
import io.github.xermaor.milvus.plus.cache.MilvusCache;
import io.github.xermaor.milvus.plus.converter.MilvusConverter;
import io.github.xermaor.milvus.plus.model.vo.MilvusResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public abstract class AbstractChainWrapper<T, W extends ConditionBuilder<T, W>> extends ConditionBuilder<T, W> {

    private final static Logger log = LoggerFactory.getLogger(AbstractChainWrapper.class);

    // 定义最大重试次数的常量
    public static final int maxRetries = 2;

    protected <R> MilvusResp<R> executeWithRetry(Supplier<MilvusResp<R>> action, String errorMessage, int maxRetries, Class<T> entityType, MilvusClientV2 client) {
        int attempt = 1;
        while (true) {
            try {
                return action.get(); // 尝试执行操作
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains(errorMessage) && attempt < maxRetries) {
                    log.warn("Attempt {}: {} - attempting to retry.", attempt, errorMessage);
                    handleCollectionNotLoaded(entityType, client);
                    attempt++;
                } else {
                    throw new RuntimeException(e); // 如果不是预期的错误或者重试次数达到上限，则抛出异常
                }
            }
        }
    }

    protected void handleCollectionNotLoaded(Class<?> entityType, MilvusClientV2 client) {
        ConversionCache cache = MilvusCache.milvusCache.get(entityType.getName());
        MilvusConverter.loadStatus(cache.milvusEntity(), client);
    }

}
