package com.strive.cache.memcached;

import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;
import org.apache.ibatis.cache.CacheException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The Transcoder that compress and decompress the stored objects using the
 * GZIP compression algorithm.
 *
 */
final class CompressorTranscoder implements Transcoder<Object> {

    /**
     * The serialized and compressed flag.
     */
    private static final int SERIALIZED_COMPRESSED = 3;

    @Override
    public boolean asyncDecode(final CachedData cachedData) {
        return false;
    }

    @Override
    public Object decode(final CachedData cachedData) {
        byte[] buffer = cachedData.getData();

        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        GZIPInputStream gzis = null;
        ObjectInputStream ois = null;
        Object ret;

        try {
            gzis = new GZIPInputStream(bais);
            ois = new ObjectInputStream(gzis);
            ret = ois.readObject();
        } catch (Exception e) {
            throw new CacheException("Impossible to decompress cached object, see nested exceptions", e);
        } finally {
            closeQuietly(ois);
            closeQuietly(gzis);
            closeQuietly(bais);
        }
        return ret;
    }

    @Override
    public CachedData encode(final Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzops = null;
        ObjectOutputStream oos = null;

        try {
            gzops = new GZIPOutputStream(baos);
            oos = new ObjectOutputStream(gzops);
            oos.writeObject(object);
        } catch (IOException e) {
            throw new CacheException("Impossible to compress object [" + object + "], see nested exceptions", e);
        } finally {
            closeQuietly(oos);
            closeQuietly(gzops);
            closeQuietly(baos);
        }

        byte[] buffer = baos.toByteArray();
        return new CachedData(SERIALIZED_COMPRESSED, buffer, CachedData.MAX_SIZE);
    }

    @Override
    public int getMaxSize() {
        return Integer.MAX_VALUE;
    }

    /**
     * Unconditionally close an {@link InputStream}.
     *
     * @param closeable the InputStream to close, may be null or already closed.
     */
    private static void closeQuietly(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }
}
