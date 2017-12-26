package com.strive.cache.memcached;

/**
 * Setter from String to Boolean representation.
 */
final class BooleanPropertySetter extends AbstractPropertySetter<Boolean> {

    /**
     * Instantiates a String to Boolean setter.
     *
     * @param propertyKey  the Memcached Config property key.
     * @param propertyName the {@link MemcachedConfiguration} property name.
     * @param defaultValue the property default value.
     */
    public BooleanPropertySetter(final String propertyKey, final String propertyName, final Boolean defaultValue) {
        super(propertyKey, propertyName, defaultValue);
    }

    @Override
    protected Boolean convert(String property) throws Exception {
        return Boolean.valueOf(property);
    }
}
