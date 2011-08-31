package com.trailbehind.android.iburn.map.source;

import java.io.IOException;

import com.nutiteq.maps.projections.EPSG3785;
import com.nutiteq.ui.StringCopyright;

/**
 * The Class MapSource3785.
 */
public class MapSource3785 extends EPSG3785 implements IMapSource {

    /** The Constant PARAM_CLOUD_MADE_KEY. */
    final static protected String PARAM_CLOUD_MADE_KEY = "CLOUD_MADE_KEY";

    /** The Constant PARAM_ZPARAM. */
    final static protected String PARAM_ZPARAM = "ZPARAM";

    /** The Constant PARAM_XPARAM. */
    final static protected String PARAM_XPARAM = "XPARAM";

    /** The Constant PARAM_YPARAM. */
    final static protected String PARAM_YPARAM = "YPARAM";

    /** The source id. */
    protected final int mSourceId;

    /** The name. */
    protected final String mName;

    /** The display name. */
    protected final String mDisplayName;

    /** The base url. */
    protected final String mBaseUrl;

    /** The tile matrix. */
    protected final int mTileMatrix;

    /** The avg tile size. */
    protected final int mTileFileSize;

    /** The min zoom. */
    protected final int mMinZoom;

    /** The max zoom. */
    protected final int mMaxZoom;

    /** The icon resource id. */
    protected final int mIconResourceId;

    /**
     * Instantiates a new map source.
     * 
     * @param sourceId
     *            the source id
     * @param name
     *            the name
     * @param url
     *            the tile url
     * @param tileMatrix
     *            the tile matrix
     * @param minZoom
     *            the min zoom
     * @param maxZoom
     *            the max zoom
     * @param tileFileSize
     *            the avg tile size
     * @param maxDownload
     *            the max download
     * @param displayName
     *            the display name
     * @param iconResourceId
     *            the icon resource id
     */
    public MapSource3785(int sourceId, String name, String displayName, int iconResourceId, String url, int tileMatrix,
            int minZoom, int maxZoom, int tileFileSize) {
        super(new StringCopyright(name), 256, minZoom, maxZoom);
        mSourceId = sourceId;

        mName = name;
        mDisplayName = displayName;

        mIconResourceId = iconResourceId;
        mBaseUrl = url;

        mTileMatrix = tileMatrix;

        mMinZoom = minZoom;
        mMaxZoom = maxZoom;

        mTileFileSize = tileFileSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.maps.UnstreamedMap#buildPath(int, int, int)
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trailbehind.android.gaiagps.maps.source.IMapSource#buildPath(int,
     * int, int)
     */
    public String buildPath(final int mapX, final int mapY, final int zoom) {
        final int x = mapX / getTileSize();
        int y = mapY / getTileSize();

        final int maxY = 1 << zoom;
        y = maxY - y - 1;

        String result = mBaseUrl.replace(PARAM_ZPARAM, Integer.toString(zoom));
        result = result.replace(PARAM_XPARAM, Integer.toString(x));
        result = result.replace(PARAM_YPARAM, Integer.toString(y));

        // Log.d(ApplicationConstants.TAG, result);
        return result;
    }

    // getters and setters

    /*
     * (non-Javadoc)
     * 
     * @see com.trailbehind.android.gaiagps.maps.source.IMapSource#getSourceId()
     */
    public int getSourceId() {
        return mSourceId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trailbehind.android.gaiagps.maps.source.IMapSource#getName()
     */
    public String getName() {
        return mName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trailbehind.android.gaiagps.maps.source.IMapSource#getBaseURL()
     */
    public String getBaseURL() {
        return mBaseUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trailbehind.android.gaiagps.maps.source.IMapSource#getTileFileSize()
     */
    public int getTileFileSize() {
        return mTileFileSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.maps.BaseMap#getMinZoom()
     */
    /*
     * (non-Javadoc)
     * 
     * @see com.trailbehind.android.gaiagps.maps.source.IMapSource#getMinZoom()
     */
    public int getMinZoom() {
        return mMinZoom;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.maps.BaseMap#getMaxZoom()
     */
    /*
     * (non-Javadoc)
     * 
     * @see com.trailbehind.android.gaiagps.maps.source.IMapSource#getMaxZoom()
     */
    public int getMaxZoom() {
        return mMaxZoom;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trailbehind.android.gaiagps.maps.source.IMapSource#getTileMatrix()
     */
    public int getTileMatrix() {
        return mTileMatrix;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.maps.LocalMap#getTileImageData(int, int, int)
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trailbehind.android.gaiagps.maps.source.IMapSource#getTileImageData
     * (int, int, int)
     */
    @Override
    public byte[] getTileImageData(int arg0, int arg1, int arg2) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trailbehind.android.gaiagps.maps.source.IMapSource#getDisplayName()
     */
    public String getDisplayName() {
        return mDisplayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trailbehind.android.gaiagps.maps.source.IMapSource#getIconResourceId
     * ()
     */
    public int getIconResourceId() {
        return mIconResourceId;
    }
}
