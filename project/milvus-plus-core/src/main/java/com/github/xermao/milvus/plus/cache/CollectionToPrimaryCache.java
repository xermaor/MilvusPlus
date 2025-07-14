package com.github.xermao.milvus.plus.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xermao
 **/
public class CollectionToPrimaryCache {

    public static final Map<String, String> collectionToPrimary = new ConcurrentHashMap<>(); //集合名称->主键名称

}
