package com.trailbehind.android.iburn.util;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

/**
 * The Class ApplicationUtils.
 */
public class MapUtils implements IConstants {

    /**
     * Inits the dir.
     * 
     * @param dir
     *            the dir
     * 
     * @throws Exception
     *             the exception
     */
    static public void initDir(String dir) throws Exception {
        final File outDir = new File(dir);
        final boolean result = outDir.mkdirs();

        Log.v(TAG, "created dir " + dir + " | " + result);
    }

    /**
     * Gets the total tile count.
     * 
     * @param rect
     *            the rect
     * 
     * @return the total tile count
     */
    static public int getTotalTileCount(Rect rect) {
        return ((rect.right + 1) - rect.left) * ((rect.bottom + 1) - rect.top);
    }

    /**
     * Gets the saved map title hint.
     * 
     * @param hint
     *            the hint
     * 
     * @return the saved map title hint
     */
    static public String getSavedMapTitleHint(String hint) {
        final SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yy hh:mm:ss a");
        return String.format(hint, formatter.format(new Date()));
    }

    /**
     * Gets the saved map description.
     * 
     * @param msgString
     *            the msg string
     * @param lat
     *            the lat
     * @param lon
     *            the lon
     * 
     * @return the saved map description
     */
    static public String getSavedMapDescription(String msgString, double lat, double lon) {
        final DecimalFormat decimalFormatter = new DecimalFormat("#0.00000");
        return String.format(msgString, decimalFormatter.format(lat), decimalFormatter.format(lon));
    }

    /**
     * Gets the saved map created time.
     * 
     * @param createdTime
     *            the created time
     * 
     * @return the saved map created time
     */
    static public String getSavedMapCreatedTime(long createdTime) {
        final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd");
        return formatter.format(new Date(createdTime));
    }

    /**
     * Gets the sD card destination dir.
     * 
     * @return the sD card destination dir
     */
    static public String getSDCardDestinationDir() {
        final File externalStoragePath = Environment.getExternalStorageDirectory();
        if (externalStoragePath != null) {
            return externalStoragePath.getAbsolutePath() + FILE_SEPARATOR + APP_DIR_ON_SD_CARD;

        } else {
            return null;
        }
    }

    /**
     * Gets the saved maps destination dir.
     * 
     * @param title
     *            the title
     * 
     * @return the saved maps destination dir
     */
    static public String getSavedMapsDestinationDir(String title) {
        final String path = getSDCardDestinationDir();
        if (path != null) {
            return path + FILE_SEPARATOR + DIR_SAVED_MAPS + FILE_SEPARATOR + Utils.cleanFileName(title);

        } else {
            return null;
        }
    }

    /**
     * Calculates the bearing of the two Locations supplied and returns the
     * Angle in the following (GPS-likely) manner: <br />
     * <code>N:0°, E:90°, S:180°, W:270°</code>.
     * 
     * @param before
     *            the before
     * @param after
     *            the after
     * 
     * @return the float
     */
    static public float calculateBearing(final Location before, final Location after) {
        final Point pBefore = location2Point(before);
        final Point pAfter = location2Point(after);

        final float res = -(float) (Math.atan2(pAfter.y - pBefore.y, pAfter.x - pBefore.x) * 180 / MathematicalConstants.PI) + 90.0f;

        if (res < 0) {
            return res + 360.0f;
        } else {
            return res;
        }
    }

    /**
     * Converts an {@link Location} to an {@link Point}.
     * 
     * @param aLocation
     *            the a location
     * 
     * @return the point
     */
    static public Point location2Point(final Location aLocation) {
        return new Point((int) (aLocation.getLongitude() * 1E6), (int) (aLocation.getLatitude() * 1E6));
    }
}
