package com.trailbehind.android.iburn.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.nutiteq.cache.Cache;
import com.nutiteq.utils.IOUtils;

/**
 * The Class FastAndroidFileSystemCache.
 */
public class FastAndroidFileSystemCache implements Cache, IConstants {

    /** The cache dir. */
    private final File mCacheDir;

    /** The mContext. */
    private final Context mContext;

    /**
     * Instantiates a new custom android file system cache.
     * 
     * @param ctx
     *            the ctx
     * @param caheName
     *            the cahe name
     * @param cacheDir
     *            the cache dir
     * @param cacheSize
     *            the cache size
     * @param readOnly
     *            the read only
     */
    public FastAndroidFileSystemCache(final Context ctx, final File cacheDir) {
        this.mContext = ctx;
        this.mCacheDir = cacheDir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.cache.Cache#cache(java.lang.String, byte[], int)
     */
    synchronized public void cache(final String cacheKey, final byte[] data, final int cacheLevel) {
        final String cacheableKey = normalizeKey(cacheKey);

        final File cacheFile = new File(mCacheDir, cacheableKey);
        cacheFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(cacheFile);
            fos.write(data);
        } catch (final IOException e) {
            Log.e(TAG, "Error writing " + cacheableKey, e);
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeStream(fos);
        }
    }

    /**
     * Normalize key.
     * 
     * @param cacheKey
     *            the cache key
     * 
     * @return the string
     */
    private String normalizeKey(final String cacheKey) {
        return cacheKey.replaceAll("://", "_").replaceAll("[^a-zA-Z\\d/]", "_");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.cache.Cache#contains(java.lang.String)
     */
    synchronized public boolean contains(final String cacheKey) {
        final String cacheableKey = normalizeKey(cacheKey);
        final File cacheFile = new File(mCacheDir, cacheableKey);
        final boolean exists = (cacheFile.exists() && cacheFile.isFile());
        return exists;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.cache.Cache#contains(java.lang.String, int)
     */
    synchronized public boolean contains(final String cacheKey, final int cacheLevel) {
        final boolean contains = contains(cacheKey);
        return contains;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.cache.Cache#deinitialize()
     */
    synchronized public void deinitialize() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.cache.Cache#get(java.lang.String)
     */
    synchronized public byte[] get(final String cacheKey) {
        final String cacheableKey = normalizeKey(cacheKey);

        final File resource = new File(mCacheDir, cacheableKey);
        if (resource.exists()) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(resource);
                return IOUtils.readFullyAndClose(fis);
            } catch (final FileNotFoundException e) {
                Log.e(TAG, "Could not load " + cacheKey, e);
                return null;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.cache.Cache#initialize()
     */
    synchronized public void initialize() {

    }
}
