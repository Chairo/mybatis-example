package com.strive.cache.oscache;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import org.apache.ibatis.cache.Cache;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class OSCache implements Cache {

    private static final GeneralCacheAdministrator CACHE_ADMINISTRATOR = new GeneralCacheAdministrator();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * 缓存ID
     */
    private final String id;

    public OSCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }

    @Override
    public void clear() {
        CACHE_ADMINISTRATOR.flushGroup(id);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Object getObject(Object key) {
        String keyString = key.toString();
        Object ret = null;

        try {
            ret = CACHE_ADMINISTRATOR.getFromCache(keyString);
        } catch (NeedsRefreshException e) {
            CACHE_ADMINISTRATOR.cancelUpdate(keyString);
        }
        return ret;
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return this.readWriteLock;
    }

    @Override
    public int getSize() {
        return CACHE_ADMINISTRATOR.getCache().getSize();
    }

    @Override
    public void putObject(Object key, Object value) {
        CACHE_ADMINISTRATOR.putInCache(key.toString(), value, new String[]{this.id});
    }

    @Override
    public Object removeObject(Object key) {
        String keyString = key.toString();
        Object ret = null;

        try {
            ret = CACHE_ADMINISTRATOR.getFromCache(keyString);
        } catch (NeedsRefreshException e) {
            CACHE_ADMINISTRATOR.cancelUpdate(keyString);
        }

        if (ret != null) {
            CACHE_ADMINISTRATOR.flushEntry(keyString);
        }
        return ret;
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
        return this.id.equals(otherCache.getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "OSCache {" + this.id + "}";
    }
}
