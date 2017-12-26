package com.strive.cache.memcached;

/**
 * Identity String setter.
 */
final class StringPropertySetter extends AbstractPropertySetter<String> {

    /**
     * Instantiates an identity String setter.
     *
     * @param propertyKey  the OSCache Config property key.
     * @param propertyName the {@link MemcachedConfiguration} property name.
     * @param defaultValue the property default value.
     */
    public StringPropertySetter(final String propertyKey, final String propertyName, final String defaultValue) {
        super(propertyKey, propertyName, defaultValue);
    }

    @Override
    protected String convert(String property) throws Exception {
        return property;
    }
}
