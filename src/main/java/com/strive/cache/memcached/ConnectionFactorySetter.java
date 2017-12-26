package com.strive.cache.memcached;

import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.DefaultConnectionFactory;

/**
 * Setter from String to ConnectionFactory representation.
 */
final class ConnectionFactorySetter extends AbstractPropertySetter<ConnectionFactory> {

    /**
     * Instantiates a String to ConnectionFactory setter.
     */
    public ConnectionFactorySetter() {
        super("org.mybatis.caches.memcached.connectionfactory", "connectionFactory", new DefaultConnectionFactory());
    }

    @Override
    protected ConnectionFactory convert(String property) throws Exception {
        Class<?> clazz = Class.forName(property);
        if (!ConnectionFactory.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                    "Class '" + clazz.getName() + "' is not a valid '" + ConnectionFactory.class.getName() + "' implementation");
        }
        return (ConnectionFactory) clazz.newInstance();
    }
}
