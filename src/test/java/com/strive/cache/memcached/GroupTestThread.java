package com.strive.cache.memcached;

import java.util.Random;
import java.util.UUID;

/**
 * 测试竞态条件行为的线程
 */
public class GroupTestThread extends Thread {

    private long itemsToCreate;
    private MemcachedCache cache;

    public GroupTestThread(MemcachedCache cache, long itemsToCreate) {
        this.setCache(cache);
        this.setItemsToCreate(itemsToCreate);
    }

    public long getItemsToCreate() {
        return itemsToCreate;
    }

    public void setItemsToCreate(long itemsToCreate) {
        this.itemsToCreate = itemsToCreate;
    }

    public MemcachedCache getCache() {
        return cache;
    }

    public void setCache(MemcachedCache cache) {
        this.cache = cache;
    }

    @Override
    public void run() {
        Random random = new Random();

        for (int i = 0; i < itemsToCreate; i++) {
            cache.putObject(UUID.randomUUID().toString(), "TEST");

            // 在每次插入之间等待1-10毫秒
            try {
                Thread.sleep(random.nextInt(10) + 1);
            } catch (InterruptedException e) {
            }
        }
    }
}
