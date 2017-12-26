package com.strive.cache.memcached;

import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

final class MemcachedClientWrapper {

    private static final Log LOG = LogFactory.getLog(MemcachedCache.class);

    private final MemcachedConfiguration configuration;

    private final MemcachedClient client;

    /**
     * 用于表示从Memcached检索到的对象及其CAS信息
     */
    private class ObjectWithCas {

        Object object;
        long cas;

        ObjectWithCas(Object object, long cas) {
            this.setObject(object);
            this.setCas(cas);
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public long getCas() {
            return cas;
        }

        public void setCas(long cas) {
            this.cas = cas;
        }
    }

    public MemcachedClientWrapper() {
        configuration = MemcachedConfigurationBuilder.getInstance().parseConfiguration();
        try {
            client = new MemcachedClient(configuration.getConnectionFactory(), configuration.getAddresses());
        } catch (IOException e) {
            String message = "Impossible to instantiate a new memecached client instance, see nested exceptions";
            LOG.error(message, e);
            throw new RuntimeException(message, e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Running new Memcached client using " + configuration);
        }
    }

    private String toKeyString(final Object key) {
        String keyString = configuration.getKeyPrefix() + StringUtils.sha1Hex(key.toString());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Object key '" + key + "' converted in '" + keyString + "'");
        }
        return keyString;
    }

    public Object getObject(Object key) {
        String keyString = this.toKeyString(key);
        Object ret = this.retrieve(keyString);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrived object (" + keyString + ", " + ret + ")");
        }
        return ret;
    }

    /**
     * @param groupKey
     * @return
     */
    private ObjectWithCas getGroup(String groupKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving group with id '" + groupKey + "'");
        }

        ObjectWithCas groups = null;
        try {
            groups = retrieveWithCas(groupKey);
        } catch (Exception e) {
            LOG.error("Impossible to retrieve group '" + groupKey + "' see nested exceptions", e);
        }

        if (groups == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Group '" + groupKey + "' not previously stored");
            }
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieved group '" + groupKey + "' with values " + groups);
        }

        return groups;
    }

    private Object retrieve(final String keyString) {
        Object retrieved;

        if (configuration.isUsingAsyncGet()) {
            Future<Object> future;
            if (configuration.isCompressionEnabled()) {
                future = client.asyncGet(keyString, new CompressorTranscoder());
            } else {
                future = client.asyncGet(keyString);
            }

            try {
                retrieved = future.get(configuration.getTimeout(), configuration.getTimeUnit());
            } catch (Exception e) {
                future.cancel(false);
                throw new CacheException(e);
            }
        } else {
            if (configuration.isCompressionEnabled()) {
                retrieved = client.get(keyString, new CompressorTranscoder());
            } else {
                retrieved = client.get(keyString);
            }
        }

        return retrieved;
    }

    /**
     * 使用给定的键检索一个对象及其cas
     *
     * @param keyString
     * @return
     * @throws Exception
     */
    private ObjectWithCas retrieveWithCas(final String keyString) {
        CASValue<Object> retrieved;

        if (configuration.isUsingAsyncGet()) {
            Future<CASValue<Object>> future;
            if (configuration.isCompressionEnabled()) {
                future = client.asyncGets(keyString, new CompressorTranscoder());
            } else {
                future = client.asyncGets(keyString);
            }

            try {
                retrieved = future.get(configuration.getTimeout(), configuration.getTimeUnit());
            } catch (Exception e) {
                future.cancel(false);
                throw new CacheException(e);
            }
        } else {
            if (configuration.isCompressionEnabled()) {
                retrieved = client.gets(keyString, new CompressorTranscoder());
            } else {
                retrieved = client.gets(keyString);
            }
        }

        if (retrieved == null) {
            return null;
        }

        return new ObjectWithCas(retrieved.getValue(), retrieved.getCas());
    }

    @SuppressWarnings("unchecked")
    public void putObject(Object key, Object value, String id) {
        String keyString = this.toKeyString(key);
        String groupKey = this.toKeyString(id);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Putting object (" + keyString + ", " + value + ")");
        }

        this.storeInMemcached(keyString, value);

        boolean jobDone = false;
        while (!jobDone) {
            ObjectWithCas group = this.getGroup(groupKey);
            Set<String> groupValues;

            if (group == null || group.getObject() == null) {
                groupValues = new HashSet<>();
                groupValues.add(keyString);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Insert/Updating object (" + groupKey + ", " + groupValues + ")");
                }

                jobDone = this.tryToAdd(groupKey, groupValues);
            } else {
                groupValues = (Set<String>) group.getObject();
                groupValues.add(keyString);

                jobDone = this.storeInMemcached(groupKey, group);
            }
        }
    }

    /**
     * 在Memcached内存储一个key标识的对象
     *
     * @param keyString
     * @param value
     */
    private void storeInMemcached(String keyString, Object value) {
        if (value != null && !Serializable.class.isAssignableFrom(value.getClass())) {
            throw new CacheException(
                    "Object of type '" + value.getClass().getName() + "' that's non-serializable is not supported by Memcached");
        }

        if (configuration.isCompressionEnabled()) {
            client.set(keyString, configuration.getExpiration(), value, new CompressorTranscoder());
        } else {
            client.set(keyString, configuration.getExpiration(), value);
        }
    }

    /**
     * 考虑cas验证，尝试更新memcached内的对象值
     * <p>
     * 如果对象通过cas验证并被修改，则返回true
     *
     * @param keyString
     * @param value
     * @return
     */
    private boolean storeInMemcached(String keyString, ObjectWithCas value) {
        if (value != null && value.getObject() != null
                && !Serializable.class.isAssignableFrom(value.getObject().getClass())) {
            throw new CacheException("Object of type '" + value.getObject().getClass().getName()
                    + "' that's non-serializable is not supported by Memcached");
        }

        CASResponse response;

        if (configuration.isCompressionEnabled()) {
            response = client.cas(keyString, value.getCas(), value.getObject(), new CompressorTranscoder());
        } else {
            response = client.cas(keyString, value.getCas(), value.getObject());
        }

        return (response.equals(CASResponse.OBSERVE_MODIFIED) || response.equals(CASResponse.OK));
    }

    /**
     * 尝试在Memcached内存储由一个key标识的对象
     * <p>
     * 如果对象已经存在，就会失败
     *
     * @param keyString
     * @param value
     * @return
     */
    private boolean tryToAdd(String keyString, Object value) {
        if (value != null && !Serializable.class.isAssignableFrom(value.getClass())) {
            throw new CacheException(
                    "Object of type '" + value.getClass().getName() + "' that's non-serializable is not supported by Memcached");
        }

        boolean done;
        OperationFuture<Boolean> result;

        if (configuration.isCompressionEnabled()) {
            result = client.add(keyString, configuration.getExpiration(), value, new CompressorTranscoder());
        } else {
            result = client.add(keyString, configuration.getExpiration(), value);
        }

        try {
            done = result.get();
        } catch (InterruptedException | ExecutionException e) {
            done = false;
        }

        return done;
    }

    public Object removeObject(Object key) {
        String keyString = this.toKeyString(key);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing object '" + keyString + "'");
        }

        Object result = this.getObject(key);
        if (result != null) {
            client.delete(keyString);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public void removeGroup(String id) {
        String groupKey = this.toKeyString(id);

        ObjectWithCas group = this.getGroup(groupKey);
        Set<String> groupValues;

        if (group == null || group.getObject() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No need to flush cached entries for group '" + id + "' because is empty");
            }
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Flushing keys: " + group);
        }

        groupValues = (Set<String>) group.getObject();

        for (String key : groupValues) {
            client.delete(key);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Flushing group: " + groupKey);
        }

        client.delete(groupKey);
    }

    @Override
    protected void finalize() throws Throwable {
        client.shutdown(configuration.getTimeout(), configuration.getTimeUnit());
        super.finalize();
    }
}
