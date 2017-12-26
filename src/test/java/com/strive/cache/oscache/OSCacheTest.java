package com.strive.cache.oscache;

import org.apache.ibatis.cache.Cache;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public final class OSCacheTest {

    private static final String DEFAULT_ID = "OSCache";

    private static Cache newCache() {
        return new OSCache(DEFAULT_ID);
    }

    @Test
    public void shouldDemonstrateHowAllObjectsAreKept() {
        Cache cache = newCache();
        for (int i = 0; i < 100000; i++) {
            cache.putObject(i, i);
            assertEquals(i, cache.getObject(i));
        }
        assertEquals(100000, cache.getSize());
    }

    @Test
    public void shouldDemonstrateCopiesAreEqual() {
        Cache cache = newCache();
        for (int i = 0; i < 1000; i++) {
            cache.putObject(i, i);
            assertEquals(i, cache.getObject(i));
        }
    }

    @Test
    public void shouldRemoveItemOnDemand() {
        Cache cache = newCache();
        cache.putObject(0, 0);
        assertNotNull(cache.getObject(0));

        cache.removeObject(0);
        assertNull(cache.getObject(0));
    }

    @Test
    public void shouldFlushAllItemsOnDemand() {
        Cache cache = newCache();
        for (int i = 0; i < 5; i++) {
            cache.putObject(i, i);
        }

        assertNotNull(cache.getObject(0));
        assertNotNull(cache.getObject(4));

        cache.clear();
        assertNull(cache.getObject(0));
        assertNull(cache.getObject(4));
    }
}
