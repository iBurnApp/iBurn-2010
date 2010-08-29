package com.trailbehind.android.iburn.map;

import android.util.Log;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.components.WgsPoint;
import com.trailbehind.android.iburn.util.IConstants;

/**
 * The Class CustomMapComponent.
 */
public class CustomMapComponent extends BasicMapComponent implements IConstants {

    /** The old w. */
    private int mOldW;

    /** The old h. */
    private int mOldH;

    /**
     * Instantiates a new custom map component.
     * 
     * @param licenseKey
     *            the license key
     * @param vendor
     *            the vendor
     * @param appname
     *            the appname
     * @param width
     *            the width
     * @param height
     *            the height
     * @param middlePoint
     *            the middle point
     * @param zoom
     *            the zoom
     */
    public CustomMapComponent(final String licenseKey, final String vendor, final String appname, final int width,
            final int height, final WgsPoint middlePoint, final int zoom) {
        super(licenseKey, vendor, appname, width, height, middlePoint, zoom);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.BasicMapComponent#resize(int, int)
     */
    @Override
    public void resize(int w, int h) {
        if (mOldW != w && mOldH != h) {
            Log.d(TAG, "CustomMapComponent width[" + w + "] heighr[" + h + "] ");
            super.resize(w, h);
            mOldW = w;
            mOldH = h;
        }
    }
}
