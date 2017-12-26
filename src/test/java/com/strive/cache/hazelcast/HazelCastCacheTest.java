package com.strive.cache.hazelcast;

import org.apache.ibatis.cache.Cache;

public class HazelCastCacheTest extends BaseHazelcastTestCase {

    @Override
    protected Cache newCache() {
        return new HazelcastCache(DEFAULT_ID);
    }
}
