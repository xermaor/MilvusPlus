package io.github.xermaor.milvus.plus.model;

public enum AnalyzerType {
    STANDARD("standard"),
    ENGLISH("english"),
    CHINESE("chinese");

    private final String type;

    AnalyzerType(String type) {
        this.type = type;
    }

    public String type() {
        return this.type;
    }
}
