package com.strive.cache.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * 如果希望将运行mybatis的JVM作为hazelcast缓存集群的一部分，则使用该类
 */
public final class HazelcastCache extends AbstractHazelcastCache {

    private static final HazelcastInstance CACHE = Hazelcast.newHazelcastInstance();

    public HazelcastCache(String id) {
        super(id, CACHE.getMap(id));
    }
}
