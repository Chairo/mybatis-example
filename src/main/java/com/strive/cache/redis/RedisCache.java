package com.strive.cache.redis;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Redis缓存适配器
 */
public final class RedisCache implements Cache {

    private final ReadWriteLock readWriteLock = new DummyReadWriteLock();
    private String id;
    private static JedisPool pool;

    public RedisCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }

        this.id = id;
        RedisConfig redisConfig = RedisConfigurationBuilder.getInstance().parseConfiguration();
        pool = new JedisPool(redisConfig, redisConfig.getHost(), redisConfig.getPort(),
                redisConfig.getConnectionTimeout(), redisConfig.getPassword(),
                redisConfig.getDatabase());
    }

    private Object execute(RedisCallback callback) {
        try (Jedis jedis = pool.getResource()) {
            return callback.doWithRedis(jedis);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getSize() {
        return (Integer) this.execute(jedis -> {
            Map<byte[], byte[]> result = jedis.hgetAll(id.getBytes());
            return result.size();
        });
    }

    @Override
    public void putObject(final Object key, final Object value) {
        this.execute(jedis -> {
            jedis.hset(id.getBytes(), key.toString().getBytes(), SerializeUtil.serialize(value));
            return null;
        });
    }

    @Override
    public Object getObject(final Object key) {
        return this.execute(jedis -> SerializeUtil.unserialize(jedis.hget(id.getBytes(), key.toString().getBytes())));
    }

    @Override
    public Object removeObject(final Object key) {
        return this.execute(jedis -> jedis.hdel(id, key.toString()));
    }

    @Override
    public void clear() {
        this.execute(jedis -> {
            jedis.del(id);
            return null;
        });
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public String toString() {
        return "Redis {" + id + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (getId() == null) {
            throw new CacheException("Cache instances require an ID.");
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof Cache)) {
            return false;
        }

        Cache otherCache = (Cache) o;
        return getId().equals(otherCache.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) {
            throw new CacheException("Cache instances require an ID.");
        }

        return getId().hashCode();
    }
}
