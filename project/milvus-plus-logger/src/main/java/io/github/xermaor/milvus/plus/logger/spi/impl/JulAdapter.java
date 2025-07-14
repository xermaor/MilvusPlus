package io.github.xermaor.milvus.plus.logger.spi.impl;

import io.github.xermaor.milvus.plus.logger.spi.LogFrameworkAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JulAdapter implements LogFrameworkAdapter {
    @Override
    public int getPriority() {
        return 3; // 最低优先级
    }

    @Override
    public boolean isSupported() {
        return true; // JUL 始终可用
    }

    @Override
    public void setLogLevel(String packageName, String level) {
        Logger logger = Logger.getLogger(packageName);
        logger.setLevel(Level.parse(level));
    }
}
