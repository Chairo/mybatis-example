package com.strive.cache.hazelcast;

import com.hazelcast.core.IMap;
import org.apache.ibatis.cache.Cache;

import java.util.concurrent.locks.ReadWriteLock;

public abstract class AbstractHazelcastCache implements Cache {

    protected final ReadWriteLock readWriteLock = new DummyReadWriteLock();

    /**
     * 缓存id
     */
    protected final String id;

    protected final IMap<Object, Object> cacheMap;

    protected AbstractHazelcastCache(String id, IMap<Object, Object> imap) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an id");
        }

        if (imap == null) {
            throw new IllegalArgumentException("Cache instances require a cacheMap");
        }

        this.id = id;
        this.cacheMap = imap;
    }

    @Override
    public void clear() {
        this.cacheMap.clear();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Object getObject(Object key) {
        return this.cacheMap.get(key);
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return this.readWriteLock;
    }

    @Override
    public int getSize() {
        return this.cacheMap.size();
    }

    @Override
    public void putObject(Object key, Object value) {
        if (value != null) {
            this.cacheMap.set(key, value);
        } else {
            if (this.cacheMap.containsKey(key)) {
                this.cacheMap.remove(key);
            }
        }
    }

    @Override
    public Object removeObject(Object key) {
        return this.cacheMap.remove(key);
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
        return "Hazelcast {" + this.id + "}";
    }
}
