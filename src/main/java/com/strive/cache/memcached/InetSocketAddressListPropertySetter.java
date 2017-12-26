package com.strive.cache.memcached;

import net.spy.memcached.AddrUtil;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * Setter from String to list of InetSocketAddress representation.
 */
final class InetSocketAddressListPropertySetter extends AbstractPropertySetter<List<InetSocketAddress>> {

    private static final List<InetSocketAddress> SOCKET_LIST = Collections.singletonList(new InetSocketAddress("localhost", 11211));

    /**
     * Instantiates a String to List&lt;InetSocketAddress&gt; setter.
     */
    public InetSocketAddressListPropertySetter() {
        super("org.mybatis.caches.memcached.servers", "addresses", SOCKET_LIST);
    }

    @Override
    protected List<InetSocketAddress> convert(String property) throws Exception {
        return AddrUtil.getAddresses(property);
    }
}
