package io.github.xermaor.milvus.plus.exception;

public class MilvusPlusException extends RuntimeException {
    public MilvusPlusException(String message) {
        super(message);
    }

    public MilvusPlusException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public MilvusPlusException(String message, Throwable cause) {
        super(message, cause);
    }
}
