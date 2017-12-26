package com.strive.cache.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.ibatis.cache.Cache;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Ehcache缓存适配器
 */
public abstract class AbstractEhcacheCache implements Cache {

    /**
     * 缓存管理器引用
     */
    protected static CacheManager CACHE_MANAGER = CacheManager.create();
    protected final String id;
    protected Ehcache cache;

    public AbstractEhcacheCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }

        this.id = id;
    }

    @Override
    public void clear() {
        cache.removeAll();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getObject(Object key) {
        Element cachedElement = cache.get(key);
        if (cachedElement == null) {
            return null;
        }
        return cachedElement.getObjectValue();
    }

    @Override
    public int getSize() {
        return cache.getSize();
    }

    @Override
    public void putObject(Object key, Object value) {
        cache.put(new Element(key, value));
    }

    @Override
    public Object removeObject(Object key) {
        Object obj = this.getObject(key);
        cache.remove(key);
        return obj;
    }

    public void unlock(Object key) {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Cache)) {
            return false;
        }

        Cache otherCache = (Cache) obj;
        return id.equals(otherCache.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    @Override
    public String toString() {
        return "EHCache {" + id + "}";
    }

    public void setTimeToIdleSeconds(long timeToIdleSeconds) {
        cache.getCacheConfiguration().setTimeToIdleSeconds(timeToIdleSeconds);
    }

    public void setTimeToLiveSeconds(long timeToLiveSeconds) {
        cache.getCacheConfiguration().setTimeToLiveSeconds(timeToLiveSeconds);
    }

    public void setMaxEntriesLocalHeap(long maxEntriesLocalHeap) {
        cache.getCacheConfiguration().setMaxEntriesLocalHeap(maxEntriesLocalHeap);
    }

    public void setMaxEntriesLocalDisk(long maxEntriesLocalDisk) {
        cache.getCacheConfiguration().setMaxEntriesLocalDisk(maxEntriesLocalDisk);
    }

    public void setMemoryStoreEvictionPolicy(String memoryStoreEvictionPolicy) {
        cache.getCacheConfiguration().setMemoryStoreEvictionPolicy(memoryStoreEvictionPolicy);
    }
}
