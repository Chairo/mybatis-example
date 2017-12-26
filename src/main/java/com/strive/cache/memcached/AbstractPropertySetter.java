package com.strive.cache.memcached;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 将配置中的键属性字符串转换为适当的Java对象
 */
abstract class AbstractPropertySetter<T> {

    private static Map<String, Method> WRITERS = new HashMap<>();

    static {
        try {
            BeanInfo memcachedConfigInfo = Introspector.getBeanInfo(MemcachedConfiguration.class);
            for (PropertyDescriptor descriptor : memcachedConfigInfo.getPropertyDescriptors()) {
                WRITERS.put(descriptor.getName(), descriptor.getWriteMethod());
            }
        } catch (IntrospectionException e) {
            // handle quietly
        }
    }

    /**
     * 配置属性key
     */
    private final String propertyKey;

    /**
     * {@link MemcachedConfiguration}中的属性名称
     */
    private final String propertyName;

    /**
     * {@link MemcachedConfiguration}中的属性的setter方法
     */
    private final Method propertyWriterMethod;

    /**
     * 在转换过程中出现问题时使用的默认值
     */
    private final T defaultValue;

    /**
     * 构建一个新的属性设置器
     *
     * @param propertyKey  配置属性key
     * @param propertyName {@link MemcachedConfiguration}中的属性名称
     * @param defaultValue 属性默认值
     */
    public AbstractPropertySetter(final String propertyKey, final String propertyName, final T defaultValue) {
        this.propertyKey = propertyKey;
        this.propertyName = propertyName;

        this.propertyWriterMethod = WRITERS.get(propertyName);
        if (this.propertyWriterMethod == null) {
            throw new RuntimeException(
                    "Class '" + MemcachedConfiguration.class.getName() + "' doesn't contain a property '" + propertyName + "'");
        }

        this.defaultValue = defaultValue;
    }

    /**
     * @param config                 属性配置信息
     * @param memcachedConfiguration {@link MemcachedConfiguration}
     */
    public final void set(Properties config, MemcachedConfiguration memcachedConfiguration) {
        String propertyValue = config.getProperty(propertyKey);
        T value;

        try {
            value = this.convert(propertyValue);
            if (value == null) {
                value = defaultValue;
            }
        } catch (Exception e) {
            value = defaultValue;
        }

        try {
            propertyWriterMethod.invoke(memcachedConfiguration, value);
        } catch (Exception e) {
            throw new RuntimeException("Impossible to set property '" + propertyName + "' with value '" + value
                    + "', extracted from ('" + propertyKey + "'=" + propertyValue + ")", e);
        }
    }

    /**
     * 将字符串表示转换为适当的Java对象
     *
     * @param value 这个值必须被转换
     * @return 转换后的值
     * @throws Exception
     */
    protected abstract T convert(String value) throws Exception;
}
