package com.trailbehind.android.iburn.map.download;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Rect;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import com.nutiteq.cache.Cache;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.WgsPoint;
import com.trailbehind.android.iburn.map.source.IMapSource;
import com.trailbehind.android.iburn.util.FastAndroidFileSystemCache;
import com.trailbehind.android.iburn.util.IConstants;
import com.trailbehind.android.iburn.util.MapUtils;
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
            MapDownloadListerner mapDownloadListerner) {
        mContext = context;
        mCache = cache;
        mMapDownloadListerner = mapDownloadListerner;
    }

    /**
     * Executes the download in a separate thread.
     */
    public void run() {
        Log.v(TAG, "Thread. new thread started..");

        final IMapSource mapSource = BURN_MAP_SOURCE;

        final WgsPoint startPoint = BURN_START_POINT;
        final WgsPoint endPoint = BURN_END_POINT;

        final int tileSize = mapSource.getTileSize();

        final File externalStoragePath = Environment.getExternalStorageDirectory();
        if (externalStoragePath != null) {
            final File appDir = new File(externalStoragePath, APP_DIR_ON_SD_CARD);
            appDir.mkdirs();

            final File cacheDir = new File(appDir, DIR_MAP_CACHE);
            cacheDir.mkdirs();

            final PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

            //  Create a new HttpClient  
            final HttpClient client = new DefaultHttpClient();

            try {
                wakeLock.acquire();

                int totalFileCount = 0;
                final MapDownloadParamInfo[] paramInfoArr = new MapDownloadParamInfo[(mapSource.getMaxZoom() - mapSource
                        .getMinZoom()) + 1];
                for (int zoom = mapSource.getMinZoom(), endZoom = mapSource.getMaxZoom(), i = 0; zoom <= endZoom; zoom++, i++) {
                    // switching to coord system
                    final MapPos mapStartPos = mapSource.wgsToMapPos(endPoint.toInternalWgs(), zoom);
                    final MapPos mapEndPos = mapSource.wgsToMapPos(startPoint.toInternalWgs(), zoom);

                    final Rect overlayRect = new Rect();
                    overlayRect.left = mapStartPos.getX() / tileSize;
                    overlayRect.top = mapStartPos.getY() / tileSize;

                    overlayRect.right = mapEndPos.getX() / tileSize;
                    overlayRect.bottom = mapEndPos.getY() / tileSize;

                    final int tilesCount = MapUtils.getTotalTileCount(overlayRect);
                    if (tilesCount > 0) {
                        totalFileCount += tilesCount;
                    }

                    paramInfoArr[i] = new MapDownloadParamInfo(overlayRect.left, overlayRect.top, overlayRect.right,
                            overlayRect.bottom, zoom, tilesCount);
                }

                int totalTilesDownloaded = 0;
                for (int i = 0, len = paramInfoArr.length; i < len; i++) {
                    if (mCancel) {
                        mMapDownloadListerner.notifyCancel();
                        return;
                    }

                    final MapDownloadParamInfo paramInfo = paramInfoArr[i];

                    final int startX = paramInfo.mStartX;
                    final int endX = paramInfo.mEndX;

                    final int tileCount = paramInfo.mTotalFileCount;

                    int tilesDownloaded = 0;
                    int retryCount = 0;

                    for (int x = startX; x <= endX; x++) {
                        if (mCancel) {
                            mMapDownloadListerner.notifyCancel();
                            return;
                        }

                        for (int y = paramInfo.mStartY; y <= paramInfo.mEndY; y++) {
                            final int tileX = x * tileSize;
                            final int tileY = y * tileSize;

                            final String url = mapSource.buildPath(tileX, tileY, paramInfo.mZoom);

                            //  Create a Get Request
                            final HttpGet get = new HttpGet(url);

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
                                            if (contentLength == -1 || contentLength == bytesSoFar) {
                                                mCache.cache(url, byteOutStream.toByteArray(),
                                                        Cache.CACHE_LEVEL_PERSISTENT);
                                                tilesDownloaded++;
                                                totalTilesDownloaded++;
                                                retryCount = 0;
                                            } else {
                                                Log.e(TAG, "Thread. mismatched content length. Bytes read "
                                                        + bytesSoFar);
                                            }
                                            break;
                                        }
                                    }

                                    mMapDownloadListerner.notifyProgress((totalTilesDownloaded * 100) / totalFileCount,
                                            100);
                                } catch (Exception e) {
                                    final String msg = "Thread. unexpected error " + e.getMessage();
                                    Log.e(TAG, msg, e);
                                    if (retryCount < RETRY_MAX) {
                                        retryCount++;
                                        y--;
                                    } else {
                                        e.printStackTrace();
                                        throw new InvalidServerResponseException(e.getMessage());
                                    }
                                }
                            } else if (statusCode == 404) {
                                Log.e(TAG, "Thread. received 404. skipping tile.");
                                tilesDownloaded++;
                                retryCount = 0;
                            } else {
                                final String msg = "Thread. unexpected response code " + statusCode + " for url " + url;
                                Log.e(TAG, msg);
                                if (retryCount < RETRY_MAX) {
                                    retryCount++;
                                    y--;
                                } else {
                                    throw new InvalidServerResponseException(msg);
                                }
                            }
                        }
                    }
                }

                mMapDownloadListerner.notifyComplete();
            } catch (FileNotFoundException fe) {
                Log.e(TAG, "Thread. FILE ERROR.", fe);
                mMapDownloadListerner.notifyError(STATUS_FILE_ERROR);
            } catch (NoHttpResponseException ie) {
                Log.e(TAG, "Thread. NO HTTP RESP ERROR.", ie);
                mMapDownloadListerner.notifyError(STATUS_SERVER_ERROR);
            } catch (InvalidServerResponseException ie) {
                Log.e(TAG, "Thread. INVALID SERVER RESP ERROR.", ie);
                mMapDownloadListerner.notifyError(STATUS_SERVER_ERROR);
            } catch (Exception e) {
                if (Utils.isNetworkAvailable(mContext)) {
                    Log.e(TAG, "Thread. UNKNOWN ERROR.", e);
                    mMapDownloadListerner.notifyError(STATUS_UNKNOWN_ERROR);
                } else {
                    Log.e(TAG, "Thread. NETWORK ERROR.", e);
                    mMapDownloadListerner.notifyError(STATUS_NETWORK_ERROR);
                }
            } finally {
                try {
                    if (client != null) {
                        client.getConnectionManager().shutdown();
                    }
                } catch (Exception e) {
                    // we dont care
                }

                if (wakeLock != null) {
                    wakeLock.release();
                    wakeLock = null;
                }
            }
        }

        Log.i(TAG, "Thread. thread ended..");
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
        public void notifyProgress(int progress, int total);

        public void notifyError(int status);

        public void notifyComplete();

        public void notifyCancel();
    }
}
