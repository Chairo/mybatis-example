package com.strive.cache.ehcache;

public class EhcacheCache extends AbstractEhcacheCache {

    public EhcacheCache(String id) {
        super(id);

        if (!CACHE_MANAGER.cacheExists(id)) {
            CACHE_MANAGER.addCache(id);
        }
        this.cache = CACHE_MANAGER.getEhcache(id);
    }
}
