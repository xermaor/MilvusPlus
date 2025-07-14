package com.github.xermao.milvus.plus.model.vo;

/**
 * @author xermao
 **/

public record MilvusResp<T>(
        boolean success,
        T data
) {}
