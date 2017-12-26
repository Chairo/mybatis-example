package com.strive.cache.memcached;

import java.util.concurrent.TimeUnit;

/**
 * Setter from String to TimeUnit representation.
 */
final class TimeUnitSetter extends AbstractPropertySetter<TimeUnit> {

    /**
     * Instantiates a String to TimeUnit setter.
     */
    public TimeUnitSetter() {
        super("org.mybatis.caches.memcached.timeoutunit", "timeUnit", TimeUnit.SECONDS);
    }

    @Override
    protected TimeUnit convert(String property) throws Exception {
        return TimeUnit.valueOf(property.toUpperCase());
    }
}
