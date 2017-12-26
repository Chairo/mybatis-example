package com.strive.cache.memcached;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * 如何运行这个测试?
 * <p>
 * 安装memcached:
 * <ul>
 * <li>ubuntu系统: <code>sudo apt-get install memcached</code></li>
 * <li>mac os x系统: <code>sudo port install memcached</code></li>
 * </ul>
 * <p>
 * 执行<code>mvn test</code>
 */
public final class MemcachedTest {

    private static final String DEFAULT_ID = "MEMCACHED";

    private MemcachedCache cache;

    @Before
    public void newCache() {
        cache = new MemcachedCache(DEFAULT_ID);
    }

    @Test
    public void shouldDemonstrateCopiesAreEqual() {
        for (int i = 0; i < 100; i++) {
            cache.putObject(i, i);
            assertEquals(i, cache.getObject(i));
        }
    }

    @Test
    public void shouldRemoveItemOnDemand() {
        cache.putObject(0, 0);
        assertNotNull(cache.getObject(0));

        cache.removeObject(0);
        Object o = cache.getObject(0);
        assertNull(o);
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
    public void shouldAcceptAKeyBiggerThan250() {
        char[] keyChar = new char[1024];
        Arrays.fill(keyChar, 'X');
        String key = new String(keyChar);
        String value = "value";
        cache.putObject(key, value);
        assertEquals(value, cache.getObject(key));
    }

    @Test
    public void groupShouldContainAllKeys() {
        long threadTestCount = 20;
        long valuesPerThread = 100;
        String mapperName = "GroupTest";

        MemcachedCache newCache = new MemcachedCache(mapperName);
        newCache.clear();

        long i = 0;
        List<Thread> threads = new ArrayList<>();

        while (i < threadTestCount) {
            Thread thread = new GroupTestThread(newCache, valuesPerThread);
            thread.start();
            threads.add(thread);
            i++;
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }

        @SuppressWarnings("unchecked")
        Set<String> keys = (Set<String>) cache.getObject(newCache.getId());
        assertNotNull(keys);

        long count = 0;

        for (@SuppressWarnings("unused")
                String key : keys) {
            count++;
        }

        assertEquals(count, valuesPerThread * threadTestCount);
    }
}
