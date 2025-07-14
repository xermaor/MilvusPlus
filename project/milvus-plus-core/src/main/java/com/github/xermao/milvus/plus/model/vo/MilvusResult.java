package com.github.xermao.milvus.plus.model.vo;

/**
 * @author xermao
 **/

public record MilvusResult<T>(
        T entity,
        Float distance,
        Object id,
        Long total
) {}
