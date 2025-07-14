package io.github.xermaor.milvus.plus.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xermao
 **/
public class MilvusCache {
    public static final Map<String, ConversionCache> milvusCache = new ConcurrentHashMap<>(); //类名-->缓存
}
