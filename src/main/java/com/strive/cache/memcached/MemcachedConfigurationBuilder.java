package com.strive.cache.memcached;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

final class MemcachedConfigurationBuilder {

    private static final MemcachedConfigurationBuilder INSTANCE = new MemcachedConfigurationBuilder();

    private static final String SYSTEM_PROPERTY_MEMCACHED_PROPERTIES_FILENAME = "memcached.properties.filename";

    private static final String MEMCACHED_RESOURCE = "memcached.properties";

    private final String memcachedPropertiesFilename;

    private final List<AbstractPropertySetter<?>> settersRegistry = new ArrayList<>();

    private MemcachedConfigurationBuilder() {
        memcachedPropertiesFilename = System.getProperty(SYSTEM_PROPERTY_MEMCACHED_PROPERTIES_FILENAME, MEMCACHED_RESOURCE);

        settersRegistry.add(new StringPropertySetter("org.mybatis.caches.memcached.keyprefix", "keyPrefix", "_mybatis_"));

        settersRegistry
                .add(new IntegerPropertySetter("org.mybatis.caches.memcached.expiration", "expiration", 60 * 60 * 24 * 30));
        settersRegistry.add(new IntegerPropertySetter("org.mybatis.caches.memcached.timeout", "timeout", 5));
        settersRegistry.add(new TimeUnitSetter());

        settersRegistry.add(new BooleanPropertySetter("org.mybatis.caches.memcached.asyncget", "usingAsyncGet", false));
        settersRegistry
                .add(new BooleanPropertySetter("org.mybatis.caches.memcached.compression", "compressionEnabled", false));

        settersRegistry.add(new InetSocketAddressListPropertySetter());
        settersRegistry.add(new ConnectionFactorySetter());
    }

    /**
     * Return this class instance.
     *
     * @return this class instance.
     */
    public static MemcachedConfigurationBuilder getInstance() {
        return INSTANCE;
    }

    /**
     * Parses the Config and builds a new {@link MemcachedConfiguration}.
     *
     * @return the converted {@link MemcachedConfiguration}.
     */
    public MemcachedConfiguration parseConfiguration() {
        return parseConfiguration(getClass().getClassLoader());
    }

    /**
     * Parses the Config and builds a new {@link MemcachedConfiguration}.
     *
     * @param classLoader {@link ClassLoader} used to load the {@code memcached.properties} file in classpath.
     * @return the converted {@link MemcachedConfiguration}.
     */
    public MemcachedConfiguration parseConfiguration(ClassLoader classLoader) {
        Properties config = new Properties();

        // load the properties specified from /memcached.properties, if present
        InputStream input = classLoader.getResourceAsStream(memcachedPropertiesFilename);
        if (input != null) {
            try {
                config.load(input);
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading classpath property '" + memcachedPropertiesFilename
                        + "', see nested exceptions", e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    // close quietly
                }
            }
        }

        MemcachedConfiguration memcachedConfiguration = new MemcachedConfiguration();

        for (AbstractPropertySetter<?> setter : settersRegistry) {
            setter.set(config, memcachedConfiguration);
        }

        return memcachedConfiguration;
    }

}
