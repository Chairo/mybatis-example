package com.strive.cache.memcached;

/**
 * Setter from String to Integer representation.
 */
final class IntegerPropertySetter extends AbstractPropertySetter<Integer> {

    /**
     * Instantiates a String to Integer setter.
     *
     * @param propertyKey  the Config property key.
     * @param propertyName the {@link MemcachedConfiguration} property name.
     * @param defaultValue the property default value.
     */
    public IntegerPropertySetter(final String propertyKey, final String propertyName, final Integer defaultValue) {
        super(propertyKey, propertyName, defaultValue);
    }

    @Override
    protected Integer convert(String property) throws Exception {
        return Integer.valueOf(property);
    }
}
