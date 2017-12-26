package com.strive.cache.ehcache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;

public class EhBlockingCache extends AbstractEhcacheCache {

    public EhBlockingCache(final String id) {
        super(id);

        if (!CACHE_MANAGER.cacheExists(id)) {
            CACHE_MANAGER.addCache(id);

            Ehcache ehcache = CACHE_MANAGER.getEhcache(id);
            BlockingCache blockingCache = new BlockingCache(ehcache);
            CACHE_MANAGER.replaceCacheWithDecoratedCache(ehcache, blockingCache);
        }
    }

    @Override
    public Object removeObject(Object key) {
        cache.put(new Element(key, null));
        return null;
    }
}