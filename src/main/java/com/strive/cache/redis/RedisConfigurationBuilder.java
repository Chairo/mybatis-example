package com.strive.cache.redis;

import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public final class RedisConfigurationBuilder {

    private static final RedisConfigurationBuilder INSTANCE = new RedisConfigurationBuilder();
    private static final String SYSTEM_PROPERTY_REDIS_PROPERTIES_FILENAME = "redis.properties.filename";
    private static final String REDIS_RESOURCE = "redis.properties";
    private final String redisPropertiesFilename;

    private RedisConfigurationBuilder() {
        redisPropertiesFilename = System.getProperty(SYSTEM_PROPERTY_REDIS_PROPERTIES_FILENAME, REDIS_RESOURCE);
    }

    /**
     * 返回RedisConfigurationBuilder实例
     *
     * @return
     */
    public static RedisConfigurationBuilder getInstance() {
        return INSTANCE;
    }

    /**
     * 解析配置并且构建一个新的{@link RedisConfig}
     *
     * @return
     */
    public RedisConfig parseConfiguration() {
        return this.parseConfiguration(this.getClass().getClassLoader());
    }

    /**
     * 解析配置并且构建一个新的{@link RedisConfig}
     *
     * @param classLoader
     * @return
     */
    public RedisConfig parseConfiguration(ClassLoader classLoader) {
        Properties config = new Properties();
        InputStream input = classLoader.getResourceAsStream(redisPropertiesFilename);
        if (input != null) {
            try {
                config.load(input);
            } catch (IOException e) {
                throw new RuntimeException(
                        "An error occurred while reading classpath property '"
                                + redisPropertiesFilename
                                + "', see nested exceptions", e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {

                }
            }
        }

        RedisConfig jedisConfig = new RedisConfig();
        this.setConfigProperties(config, jedisConfig);
        return jedisConfig;
    }

    private void setConfigProperties(Properties properties, RedisConfig jedisConfig) {
        if (properties != null) {
            MetaObject metaCache = SystemMetaObject.forObject(jedisConfig);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String name = (String) entry.getKey();
                String value = (String) entry.getValue();

                if (metaCache.hasSetter(name)) {
                    Class<?> type = metaCache.getSetterType(name);
                    if (String.class == type) {
                        metaCache.setValue(name, value);
                    } else if (int.class == type || Integer.class == type) {
                        metaCache.setValue(name, Integer.valueOf(value));
                    } else if (long.class == type || Long.class == type) {
                        metaCache.setValue(name, Long.valueOf(value));
                    } else if (short.class == type || Short.class == type) {
                        metaCache.setValue(name, Short.valueOf(value));
                    } else if (byte.class == type || Byte.class == type) {
                        metaCache.setValue(name, Byte.valueOf(value));
                    } else if (float.class == type || Float.class == type) {
                        metaCache.setValue(name, Float.valueOf(value));
                    } else if (boolean.class == type || Boolean.class == type) {
                        metaCache.setValue(name, Boolean.valueOf(value));
                    } else if (double.class == type || Double.class == type) {
                        metaCache.setValue(name, Double.valueOf(value));
                    } else {
                        throw new CacheException("Unsupported property type: '" + name + "' of type " + type);
                    }
                }
            }
        }
    }
}
