package com.strive.cache.oscache;

import org.apache.ibatis.cache.decorators.LoggingCache;

public final class LoggingOSCache extends LoggingCache {

    public LoggingOSCache(final String id) {
        super(new OSCache(id));
    }
}
