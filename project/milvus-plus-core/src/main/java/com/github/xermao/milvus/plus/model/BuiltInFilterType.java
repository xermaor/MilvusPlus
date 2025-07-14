package com.github.xermao.milvus.plus.model;

/**
 * 枚举表示内置的过滤器类型。
 */
public enum BuiltInFilterType {
    LOWERCASE, ASCII_FOLDING, ALPHA_NUM_ONLY, CN_ALPHA_NUM_ONLY, CN_CHAR_ONLY, STOP, LENGTH, STEMMER
}