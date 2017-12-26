package com.strive.cache.memcached;

import org.apache.ibatis.cache.decorators.LoggingCache;

/**
 * {@code LoggingCache} adapter for Memcached.
 */
public final class LoggingMemcachedCache extends LoggingCache {

    public LoggingMemcachedCache(final String id) {
        super(new MemcachedCache(id));
    }
}
