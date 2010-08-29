package com.trailbehind.android.iburn.util;

import android.graphics.RectF;

import com.nutiteq.components.WgsPoint;
import com.trailbehind.android.iburn.R;
import com.trailbehind.android.iburn.map.source.IMapSource;
import com.trailbehind.android.iburn.map.source.MapSource3785;

public interface IConstants {
    public String TAG = "iBurn";

    public String BURN_TILES_URL = "http://earthdev.burningman.com/osm_tiles_2010/ZPARAM/XPARAM/YPARAM.png";

    /** The Constant LINE_SEPARATOR. */
    public String LINE_SEPARATOR = System.getProperty("line.separator");

    /** The Constant FILE_SEPARATOR. */
    public String FILE_SEPARATOR = System.getProperty("file.separator");

    // Nutiteq constants --------

    /** The NUTITE q_ license key. */
    public String NUTITEQ_LICENSE_KEY = "96da2f590cd7246bbde0051047b0d6f74bf0480488d495.58445244";

    /** The NUTITE q_ vendor. */
    public String NUTITEQ_VENDOR = "TrailBehind, INC.";

    /** The NUTITE q_ ap p_ name. */
    public String NUTITEQ_APP_NAME = "Gaia GPS";

    // IO constants ---------

    /** The PAT h_ imag e_ compass. */
    public String PATH_IMAGE_COMPASS = "/res/drawable/compass_arrow.png";

    /** The Constant PATH_BLANK_IMAGE. */
    public String PATH_BLANK_IMAGE = "/res/drawable/blank.png";

    /** The PAT h_ o n_ s d_ card. */
    public String APP_DIR_ON_SD_CARD = TAG;

    /** The DI r_ map cache. */
    public String DIR_MAP_CACHE = "Cache";

    /** The Constant DIR_SAVED_MAPS. */
    public String DIR_SAVED_MAPS = "SavedMaps";

    // common keys

    /** The KE y_ rec t_ left. */
    public String KEY_RECT_LEFT = "left";

    /** The KE y_ rec t_ top. */
    public String KEY_RECT_TOP = "top";

    /** The KE y_ rec t_ right. */
    public String KEY_RECT_RIGHT = "right";

    /** The KE y_ rec t_ bottom. */
    public String KEY_RECT_BOTTOM = "bootom";

    /** The KE y_ zoom. */
    public String KEY_ZOOM = "zoom";

    /** The Constant KEY_ID. */
    public String KEY_ID = "id";

    public String KEY_COUNT = "count";

    /** The Constant KEY_LON. */
    final static public String KEY_LON = "lon";

    /** The Constant KEY_LAT. */
    final static public String KEY_LAT = "lat";

    public String KEY_TYPE = "type";

    /** The Constant RESOURCE_PATH_BLUE_PIN. */
    public String RESOURCE_PATH_BLUE_PIN = "/res/drawable/blue_pin_down.png";

    public String RESOURCE_PATH_RED_PIN = "/res/drawable/red_pin_down.png";

    public RectF BURN_MAP_RECT = new RectF(40.756f, -119.176f, 40.816f, -119.236f);

    public WgsPoint BURN_START_POINT = new WgsPoint(BURN_MAP_RECT.top, BURN_MAP_RECT.left);
    public WgsPoint BURN_END_POINT = new WgsPoint(BURN_MAP_RECT.bottom, BURN_MAP_RECT.right);

    public WgsPoint BURN_CENTER_POINT = new WgsPoint(BURN_MAP_RECT.centerY(), BURN_MAP_RECT.centerX());

    public int BURN_DEFULT_ZOOM = 14;

    public IMapSource BURN_MAP_SOURCE = new MapSource3785(1, TAG, TAG, R.drawable.icon, BURN_TILES_URL, 256, 10, 17, 50);

    // status codes ---------

    /** This download hasn't stated yet. */
    public int STATUS_PENDING = 1;

    /** This download has started. */
    public int STATUS_RUNNING = 2;

    /** This download has started and is paused. */
    public int STATUS_PAUSED = 3;

    /** This download was completed. */
    public int STATUS_COMPLETED = 11;

    /** This download has completed with an error. */
    public int STATUS_NETWORK_ERROR = 21;

    /**
     * This download couldn't be completed because of a storage issue.
     * Typically, that's because the filesystem is missing or full.
     */
    public int STATUS_FILE_ERROR = 22;

    /** The Constant STATUS_SERVER_ERROR. */
    public int STATUS_SERVER_ERROR = 23;

    /** The Constant STATUS_UNKNOWN_ERROR. */
    public int STATUS_UNKNOWN_ERROR = 24;

    // other constants ---------

    /** The buffer size used to stream the data. */
    public int BUFFER_SIZE = 4096;

    // pref key ----------

    public String PREF_KEY_MAP_DOWNLOADED = "map.downloaded";
}
