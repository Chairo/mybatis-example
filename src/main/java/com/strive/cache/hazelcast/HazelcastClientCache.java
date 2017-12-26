package com.strive.cache.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

/**
 * 使用Hazelcast客户端缓存{@link HazelcastClient}，如果想让运行mybatis的JVM作为Hazelcast缓存集群的一个客户端，则使用该类
 * <p>
 * 这意味着，HazelcastClientCache不是集群的成员
 * </p>
 */
public class HazelcastClientCache extends AbstractHazelcastCache {

    private static final HazelcastInstance CACHE = HazelcastClient.newHazelcastClient();

    public HazelcastClientCache(String id) {
        super(id, CACHE.getMap(id));
    }
}
