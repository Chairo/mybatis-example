package com.strive.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.ibatis.cache.Cache;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Caffeine缓存适配器
 */
public final class CaffeineCache implements Cache {

    private com.github.benmanes.caffeine.cache.Cache<Object, Object> cache;

    private String id;

    public CaffeineCache(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }

        this.cache = Caffeine.newBuilder().build();
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void putObject(Object key, Object value) {
        this.cache.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return this.cache.getIfPresent(key);
    }

    @Override
    public Object removeObject(Object key) {
        return this.cache.asMap().remove(key);
    }

    @Override
    public void clear() {
        this.cache.invalidateAll();
    }

    @Override
    public int getSize() {
        return (int) this.cache.estimatedSize();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }
}
