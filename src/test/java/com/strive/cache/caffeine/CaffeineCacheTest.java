package com.strive.cache.caffeine;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CaffeineCacheTest {

    private static final String DEFAULT_ID = "Caffeine";

    private CaffeineCache cache;

    @Before
    public void setup() {
        this.cache = new CaffeineCache(DEFAULT_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateCache() {
        this.cache = new CaffeineCache(null);
    }

    @Test
    public void shouldVerifyCacheId() {
        assertThat(DEFAULT_ID).isEqualTo(this.cache.getId());
    }

    @Test
    public void shouldPersistObject() {
        this.cache.putObject(1, "foo");
        assertThat(this.cache.getObject(1)).isEqualTo("foo");
    }

    @Test
    public void shouldRemoveObject() {
        this.cache.putObject(1, "foo");
        assertThat(this.cache.getObject(1)).isEqualTo("foo");

        this.cache.removeObject(1);
        assertThat(this.cache.getObject(1)).isNull();
    }

    @Test
    public void shouldRemoveAllObjects() {
        this.cache.putObject(1, "foo");
        this.cache.putObject(2, "bar");

        assertThat(this.cache.getObject(1)).isEqualTo("foo");
        assertThat(this.cache.getObject(2)).isEqualTo("bar");

        this.cache.clear();

        assertThat(this.cache.getObject(1)).isNull();
        assertThat(this.cache.getObject(2)).isNull();
    }

    @Test
    public void shouldVerifySize() {
        this.cache.putObject(1, "foo");
        this.cache.putObject(2, "bar");

        assertThat(this.cache.getSize()).isEqualTo(2);
    }
}
