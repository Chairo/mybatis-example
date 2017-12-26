package com.strive.cache.hazelcast;

import org.apache.ibatis.cache.Cache;
import org.junit.BeforeClass;

public final class HazelCastClientCacheTest extends BaseHazelcastTestCase {

    @BeforeClass
    public static void setupClass() {
        new HazelcastCache(DEFAULT_ID);
    }

    @Override
    protected Cache newCache() {
        return new HazelcastClientCache(DEFAULT_ID);
    }
}
