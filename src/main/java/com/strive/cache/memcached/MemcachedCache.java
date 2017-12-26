package com.strive.cache.memcached;

import org.apache.ibatis.cache.Cache;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * 基于Memcached的缓存实现类
 */
public final class MemcachedCache implements Cache {

    private static final MemcachedClientWrapper MEMCACHED_CLIENT = new MemcachedClientWrapper();

    private final ReadWriteLock readWriteLock = new DummyReadWriteLock();

    /**
     * 缓存ID
     */
    private final String id;

    public MemcachedCache(final String id) {
        this.id = id;
    }

    @Override
    public void clear() {
        MEMCACHED_CLIENT.removeGroup(this.id);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Object getObject(Object key) {
        return MEMCACHED_CLIENT.getObject(key);
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return this.readWriteLock;
    }

    @Override
    public int getSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void putObject(Object key, Object value) {
        MEMCACHED_CLIENT.putObject(key, value, this.id);
    }

    @Override
    public Object removeObject(Object key) {
        return MEMCACHED_CLIENT.removeObject(key);
    }
}
