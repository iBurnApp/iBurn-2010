package com.trailbehind.android.iburn.map.download;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import com.trailbehind.android.iburn.map.MapActivity.FileCacheWriter;
import com.trailbehind.android.iburn.map.MapActivity.FileCacheWriter.FileCacheInfo;
import com.trailbehind.android.iburn.util.FastAndroidFileSystemCache;
import com.trailbehind.android.iburn.util.IConstants;
import com.trailbehind.android.iburn.util.Utils;

/**
 * The Class MapDownloadThread.
 */
public class MapDownloadThread extends Thread implements IConstants {

    /** The Constant RETRY_MAX. */
    static final private int RETRY_MAX = 3;

    /** The context. */
    final private Context mContext;

    /** The cache. */
    final private FastAndroidFileSystemCache mCache;

    final private MapDownloadListerner mMapDownloadListerner;

    private boolean mCancel;

    /** The file cache writer. */
    private FileCacheWriter mFileCacheWriter;

    /** The thread number. */
    final private int mThreadNumber;

    DefaultHttpClient mDefaultHttpClient;

    final private LinkedList<String> mUrlList;

    /**
     * Instantiates a new map download thread.
     * 
     * @param context
     *            the context
     * @param mapDownloadInfo
     *            the map download info
     * @param threadNumber
     *            the thread number
     * @param cache
     *            the cache
     */
    public MapDownloadThread(Context context, FastAndroidFileSystemCache cache,
            MapDownloadListerner mapDownloadListerner, DefaultHttpClient defaultHttpClient, int threadNumber,
            FileCacheWriter fileCacheWriter, LinkedList<String> urlList) {
        mContext = context;
        mCache = cache;
        mMapDownloadListerner = mapDownloadListerner;
        mDefaultHttpClient = defaultHttpClient;
        mThreadNumber = threadNumber;
        mFileCacheWriter = fileCacheWriter;
        mUrlList = urlList;
    }

    /**
     * Executes the download in a separate thread.
     */
    public void run() {
        Log.v(TAG, "Thread " + mThreadNumber + ". new thread started..");

        final File externalStoragePath = Environment.getExternalStorageDirectory();
        if (externalStoragePath != null) {
            final File appDir = new File(externalStoragePath, APP_DIR_ON_SD_CARD);
            appDir.mkdirs();

            final File cacheDir = new File(appDir, DIR_MAP_CACHE);
            cacheDir.mkdirs();

            final PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

            //  use the existing HttpClient  
            final HttpClient client = mDefaultHttpClient;

            try {
                wakeLock.acquire();

                final HttpGet get = new HttpGet();

                int retryCount = 0;
                String url = null;

                while (retryCount != 0 || (url = mUrlList.poll()) != null) {
                    if (mCancel) {
                        mMapDownloadListerner.notifyCancel();
                        return;
                    }
                    if (mFileCacheWriter.mError) {
                        Log.v(TAG, "Thread " + mThreadNumber + ". FileCacheWriter error is true. Thread terminated.");
                        throw new FileNotFoundException(mFileCacheWriter.mException.getMessage());
                    }

                    /*
                     * if (mCache.contains(url.toString())) { tilesDownloaded++;
                     * totalTilesDownloaded++; retryCount = 0;
                     * 
                     * mMapDownloadListerner.notifyProgress(
                     * totalTilesDownloaded, totalFileCount);
                     * 
                     * Log.d(TAG, "Thread " + mThreadNumber +
                     * ". skipping existing tile.. " + url); } else {
                     */
                    //  Create a Get Request
                    get.setURI(URI.create(url));

                    // Execute HTTP Request
                    final HttpResponse response = client.execute(get);

                    final int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        int contentLength = -1;
                        final Header header = response.getFirstHeader("Content-Length");
                        if (header != null) {
                            contentLength = Integer.parseInt(header.getValue());
                        }

                        byte data[] = new byte[BUFFER_SIZE];
                        int bytesSoFar = 0;

                        final InputStream entityStream = response.getEntity().getContent();
                        final ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();

                        try {
                            while (true) {
                                final int bytesRead = entityStream.read(data);
                                if (bytesRead != -1) {
                                    bytesSoFar += bytesRead;
                                    byteOutStream.write(data, 0, bytesRead);
                                } else {
                                    retryCount = 0;

                                    if (contentLength == -1 || contentLength == bytesSoFar) {
                                        final FileCacheInfo bundle = new FileCacheInfo();
                                        bundle.mUrl = new String(url);
                                        bundle.mData = byteOutStream.toByteArray();

                                        // Log.i(TAG, "Thread " +
                                        // mThreadNumber +
                                        // ". file downloaded. "
                                        // + totalTilesDownloaded);
                                        mFileCacheWriter.push(bundle);
                                    } else {
                                        Log.e(TAG, "Thread " + mThreadNumber
                                                + ". mismatched content length. skipping tile. contentLength "
                                                + contentLength + " Bytes read " + bytesSoFar);
                                        mMapDownloadListerner.notifyProgress();
                                    }

                                    break;
                                }
                            }
                        } catch (Exception e) {
                            final String msg = "Thread " + mThreadNumber + ". unexpected error " + e.getMessage();
                            Log.e(TAG, msg, e);
                            if (retryCount < RETRY_MAX) {
                                retryCount++;
                            } else {
                                e.printStackTrace();
                                throw new InvalidServerResponseException(e.getMessage());
                            }
                        } finally {
                            try {
                                if (byteOutStream != null) {
                                    byteOutStream.close();
                                }
                            } catch (Exception e) {
                            }
                        }
                    } else if (statusCode == 404 || statusCode == 403) {
                        Log.e(TAG, "Thread " + mThreadNumber + ". received " + statusCode + ". skipping tile. " + url);
                        retryCount = 0;
                        mMapDownloadListerner.notifyProgress();
                    } else {
                        final String msg = "Thread " + mThreadNumber + ". unexpected response code " + statusCode
                                + " for url " + url;
                        Log.e(TAG, msg);
                        if (retryCount < RETRY_MAX) {
                            retryCount++;
                        } else {
                            throw new InvalidServerResponseException(msg);
                        }
                    }
                }

                mMapDownloadListerner.notifyComplete();
            } catch (FileNotFoundException fe) {
                Log.e(TAG, "Thread " + mThreadNumber + ". FILE ERROR.", fe);
                mMapDownloadListerner.notifyError(STATUS_FILE_ERROR);
            } catch (NoHttpResponseException ie) {
                Log.e(TAG, "Thread " + mThreadNumber + ". NO HTTP RESP ERROR.", ie);
                mMapDownloadListerner.notifyError(STATUS_SERVER_ERROR);
            } catch (InvalidServerResponseException ie) {
                Log.e(TAG, "Thread " + mThreadNumber + ". INVALID SERVER RESP ERROR.", ie);
                mMapDownloadListerner.notifyError(STATUS_SERVER_ERROR);
            } catch (Exception e) {
                if (Utils.isNetworkAvailable(mContext)) {
                    Log.e(TAG, "Thread " + mThreadNumber + ". UNKNOWN ERROR.", e);
                    mMapDownloadListerner.notifyError(STATUS_UNKNOWN_ERROR);
                } else {
                    Log.e(TAG, "Thread " + mThreadNumber + ". NETWORK ERROR.", e);
                    mMapDownloadListerner.notifyError(STATUS_NETWORK_ERROR);
                }
            } finally {
                if (wakeLock != null) {
                    wakeLock.release();
                    wakeLock = null;
                }
            }
        }

        Log.i(TAG, "Thread " + mThreadNumber + ". thread ended..");
    }

    synchronized public void cancel() {
        mCancel = true;
    }

    // inner classes

    /**
     * The Class InvalidServerResponseException.
     */
    @SuppressWarnings("serial")
    static private class InvalidServerResponseException extends IOException {

        /**
         * Instantiates a new invalid server response exception.
         * 
         * @param detailMessage
         *            the detail message
         */
        public InvalidServerResponseException(String detailMessage) {
            super(detailMessage);
        }
    }

    static public interface MapDownloadListerner {
        public void notifyProgress();

        public void notifyError(int status);

        public void notifyComplete();

        public void notifyCancel();
    }
}
