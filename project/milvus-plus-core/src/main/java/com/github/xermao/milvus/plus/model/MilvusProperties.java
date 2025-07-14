package com.github.xermao.milvus.plus.model;

import java.util.List;

/**
 * @author xermao
 **/

public record MilvusProperties(
        boolean enable,
        String uri,
        String dbName,
        String username,
        String password,
        String token,
        List<String> packages
) {}