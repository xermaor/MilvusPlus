package com.github.xermao.milvus.plus.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xermao
 **/
public class PropertyCache {

    public final Map<String, String> functionToPropertyMap = new HashMap<>(); //属性名称->集合属性名称
    public final Map<String, Boolean> nullableToPropertyMap = new HashMap<>(); //属性名称->是否允许为空
    public final Map<String, String> methodToPropertyMap = new HashMap<>(); //属性get方法名称->集合属性名称
    private final Map<String, String> propertyToFunctionMap = new HashMap<>(); // 集合属性名称->属性名称

    public void putFunctionToProperty(String function, String property) {
        functionToPropertyMap.put(function, property);
        propertyToFunctionMap.put(property, function);
    }

    // 根据值查找第一个匹配的键
    public String findKeyByValue(String value) {
        return propertyToFunctionMap.get(value);
    }
}
