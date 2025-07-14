package io.github.xermaor.milvus.plus.model.vo;

/**
 * @author xermao
 **/

public record MilvusResp<T>(
        boolean success,
        T data
) {}
