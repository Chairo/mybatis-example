package com.strive.cache.ehcache;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class EhcacheTest {

    private static final String DEFAULT_ID = "EHCACHE";

    private AbstractEhcacheCache cache;

    @Before
    public void newCache() {
        cache = new EhcacheCache(DEFAULT_ID);
    }

    @Test
    public void shouldDemonstrateHowAllObjectsAreKept() {
        for (int i = 0; i < 100000; i++) {
            cache.putObject(i, i);
            assertEquals(i, cache.getObject(i));
        }
        assertEquals(100000, cache.getSize());
    }

    @Test
    public void shouldDemonstrateCopiesAreEqual() {
        for (int i = 0; i < 1000; i++) {
            cache.putObject(i, i);
            assertEquals(i, cache.getObject(i));
        }
    }

    @Test
    public void shouldRemoveItemOnDemand() {
        cache.putObject(0, 0);
        assertNotNull(cache.getObject(0));

        cache.removeObject(0);
        assertNull(cache.getObject(0));
    }

    @Test
    public void shouldFlushAllItemsOnDemand() {
        for (int i = 0; i < 5; i++) {
            cache.putObject(i, i);
        }

        assertNotNull(cache.getObject(0));
        assertNotNull(cache.getObject(4));

        cache.clear();
        assertNull(cache.getObject(0));
        assertNull(cache.getObject(4));
    }

    @Test
    public void shouldChangeTimeToLive() throws Exception {
        cache.putObject("test", "test");
        Thread.sleep(1200);
        assertEquals("test", cache.getObject("test"));

        cache.setTimeToLiveSeconds(1);
        Thread.sleep(1200);
        assertNull(cache.getObject("test"));

        this.resetCache();
    }

    @Test
    public void shouldChangeTimeToIdle() throws Exception {
        cache.putObject("test", "test");
        Thread.sleep(1200);
        assertEquals("test", cache.getObject("test"));

        cache.setTimeToIdleSeconds(1);
        Thread.sleep(1200);
        assertNull(cache.getObject("test"));

        this.resetCache();
    }

    @Test
    public void shouldTestEvictionPolicy() throws Exception {
        cache.clear();
        cache.setMemoryStoreEvictionPolicy("FIFO");
        cache.setMaxEntriesLocalHeap(1);
        cache.setMaxEntriesLocalDisk(1);
        cache.putObject("eviction", "eviction");
        cache.putObject("eviction2", "eviction2");
        cache.putObject("eviction3", "eviction3");
        Thread.sleep(1200);
        assertEquals(1, cache.getSize());

        this.resetCache();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateCache() {
        cache = new EhcacheCache(null);
    }

    @Test
    public void shouldVerifyCacheId() {
        assertEquals("EHCACHE", cache.getId());
    }

    @Test
    public void shouldVerifyToString() {
        assertEquals("EHCache {EHCACHE}", cache.toString());
    }

    @Test
    public void equalsAndHashCodeSymmetricTest() {
        AbstractEhcacheCache x = new EhcacheCache("EHCACHE");
        AbstractEhcacheCache y = new EhcacheCache("EHCACHE");
        assertTrue(x.equals(y));
        assertTrue(y.equals(x));
        assertEquals(x.hashCode(), y.hashCode());

        // 虚拟测试以覆盖边界情况
        assertFalse(x.equals(new String()));
        assertFalse(x.equals(null));
        assertTrue(x.equals(x));
    }

    private void resetCache() {
        cache.setTimeToLiveSeconds(120);
        cache.setTimeToIdleSeconds(120);
        cache.setMemoryStoreEvictionPolicy("LRU");
    }
}
