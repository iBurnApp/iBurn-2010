package com.trailbehind.android.iburn.map;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpVersion;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;
import com.nutiteq.cache.Cache;
import com.nutiteq.cache.CachingChain;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.PlaceIcon;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.components.ZoomRange;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.core.MappingCore;
import com.nutiteq.fs.AndroidFileSystem;
import com.nutiteq.listeners.DelayedMapListener;
import com.nutiteq.location.LocationMarker;
import com.nutiteq.location.NutiteqLocationMarker;
import com.nutiteq.ui.DefaultZoomIndicator;
import com.nutiteq.ui.ThreadDrivenPanning;
import com.trailbehind.android.iburn.BurnApplication;
import com.trailbehind.android.iburn.BurnApplication.AppLocationListener;
import com.trailbehind.android.iburn.BurnApplication.AppLocationProvider;
import com.trailbehind.android.iburn.R;
import com.trailbehind.android.iburn.map.download.MapDownloadThread;
import com.trailbehind.android.iburn.map.download.MapDownloadThread.MapDownloadListerner;
import com.trailbehind.android.iburn.map.source.IMapSource;
import com.trailbehind.android.iburn.util.CompassUtils;
import com.trailbehind.android.iburn.util.CompassUtils.CompassPlaceIcon;
import com.trailbehind.android.iburn.util.FastAndroidFileSystemCache;
import com.trailbehind.android.iburn.util.Globals;
import com.trailbehind.android.iburn.util.IConstants;
import com.trailbehind.android.iburn.util.MapUtils;
import com.trailbehind.android.iburn.util.UIUtils;
import com.trailbehind.android.iburn.util.Utils;
import com.trailbehind.android.iburn.view.CompassView;
import com.trailbehind.android.iburn.view.CustomMapView;
import com.trailbehind.android.iburn.view.MapZoomControls;

public class MapActivity extends Activity implements IConstants {

    /** The Constant DIALOG_ABOUT. */
    final static private int DIALOG_ABOUT = 101;

    /** The Constant DIALOG_SDCARD_NOT_AVAILABLE. */
    final static public int DIALOG_SDCARD_NOT_AVAILABLE = 102;

    /** The Constant DIALOG_NO_INTERNET_DOWNLOAD. */
    final static private int DIALOG_NO_INTERNET_DOWNLOAD = 103;

    /** The Constant DIALOG_CLOSING_RESOURCES. */
    final static private int DIALOG_CLOSING_RESOURCES = 104;

    /** The Constant DIALOG_CLOSING_RESOURCES. */
    final static private int DIALOG_DOWNLOAD_PROGRESS = 105;

    final static private int DIALOG_DOWNLOAD_ERROR = 106;

    // UI views ---------

    /** The map layout. */
    private FrameLayout mMapLayoutView;

    /** The map view. */
    private CustomMapView mMapView;

    /** The compass view. */
    private CompassView mCompassView;

    /** The message panel. */
    private View mMessagePanel;

    // UI controls ---------

    /** The zoom controls. */
    private MapZoomControls mZoomControls;

    /** The progress bar. */
    private ProgressBar mProgressBar;

    /** The message control. */
    private TextView mMessageTextView;

    private ProgressDialog mProgressDialog;

    // menu --------

    /** The menu. */
    private Menu mMenu;

    // map components ---------

    /** The map component. */
    BasicMapComponent mMapComponent;

    /** The saved map cache. */
    private FastAndroidFileSystemCache mSavedMapCache;

    /** The map touch listener. */
    private MapTouchListener mMapTouchListener;

    /** The control keys handler. */
    private AndroidKeysHandler mControlKeysHandler;

    /** The dummy location marker. */
    private LocationMarker mDummyLocationMarker;

    // boolean flags ---------

    /** The locating location. */
    private boolean mGetMyLocation;

    // inner class handles ---------

    /** The map handler. */
    ActivityHandler mActivityHandler = new ActivityHandler();

    // other properties ---------

    /** The wake lock. */
    private PowerManager.WakeLock mWakeLock;

    /** The vibrator. */
    private Vibrator mVibrator;

    private MapDownloadThread[] mMapDownloadThreads;

    private DefaultHttpClient mDefaultHttpClient1;

    /** The file cache writer. */
    private FileCacheWriter mFileCacheWriter;

    private int mErrorMsgId;

    private int mTotalTileCount;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        Log.d(TAG, "MapActivity:onCreate called..");

        // set app params
        setActivityParams();

        // setup views
        setupViews();

        setupMapDownload(false);
    }

    private void setupMapDownload(boolean force) {
        if (Utils.isNetworkAvailable(getBaseContext())) {
            if (Utils.isSDCardAvailable()) {
                final boolean mapDownloaded = Globals.sSharedPreferences.getBoolean(PREF_KEY_MAP_DOWNLOADED, false);
                if (!mapDownloaded || force) {
                    showDialog(DIALOG_DOWNLOAD_PROGRESS);
                    mActivityHandler.sendMessageDelayed(
                            mActivityHandler.obtainMessage(ActivityHandler.MESSAGE_START_DOWNLOAD), 50);
                }
            } else {
                showDialog(DIALOG_SDCARD_NOT_AVAILABLE);
            }
        } else {
            showDialog(DIALOG_NO_INTERNET_DOWNLOAD);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        checkGPSProvider();
        registerReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mActivityHandler.removeCallbacksAndMessages(null);
        unregisterReceivers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onDestroy()
     */
    @Override
    public void onDestroy() {
        try {
            if (mDefaultHttpClient1 != null) {
                mDefaultHttpClient1.getConnectionManager().shutdown();
            }
        } catch (Exception e) {
            // we dont care
        }

        getBurnApplication().onDestroy();

        doCleanup();
        System.gc();

        Log.d(TAG, "onDestroy called..");
        super.onDestroy();
    }

    // dialog handler

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_ABOUT:
            final ScrollView dialogView = (ScrollView) LayoutInflater.from(getBaseContext()).inflate(
                    R.layout.about_dialog, null);
            final TextView aboutText = (TextView) dialogView.findViewById(R.id.message);
            aboutText.setText(Utils.getAboutText(getBaseContext()));

            final String title = getString(R.string.title_dialog_about) + " " + getString(R.string.app_name);
            return new AlertDialog.Builder(this).setView(dialogView).setIcon(R.drawable.icon).setTitle(title)
                    .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    }).create();
        case DIALOG_NO_INTERNET_DOWNLOAD:
            return new AlertDialog.Builder(MapActivity.this).setTitle(R.string.title_dialog_error)
                    .setMessage(R.string.error_no_internet_download)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            removeDialog(DIALOG_NO_INTERNET_DOWNLOAD);
                        }
                    }).create();
        case DIALOG_SDCARD_NOT_AVAILABLE:
            return new AlertDialog.Builder(this).setIcon(R.drawable.ic_dialog_menu_generic)
                    .setTitle(R.string.title_dialog_error).setMessage(R.string.error_sd_not_available)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            removeDialog(DIALOG_SDCARD_NOT_AVAILABLE);
                        }
                    }).create();
        case DIALOG_DOWNLOAD_PROGRESS:
            mProgressDialog = new ProgressDialog(MapActivity.this);
            mProgressDialog.setTitle(R.string.title_dialog_download);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setButton(getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    removeDialog(DIALOG_DOWNLOAD_PROGRESS);
                    for (int i = 0, len = mMapDownloadThreads.length; i < len; i++) {
                        MapDownloadThread t = mMapDownloadThreads[i];
                        if (t != null) {
                            t.cancel();
                        }
                        mMapDownloadThreads[i] = null;
                    }

                    mFileCacheWriter.stopRunning();
                    mFileCacheWriter = null;
                }
            });
            return mProgressDialog;
        case DIALOG_DOWNLOAD_ERROR:
            return new AlertDialog.Builder(MapActivity.this).setIcon(R.drawable.ic_dialog_menu_generic)
                    .setTitle(R.string.title_dialog_error).setMessage(mErrorMsgId)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            removeDialog(DIALOG_SDCARD_NOT_AVAILABLE);
                        }
                    }).create();
        default:
            return null;
        }
    }

    // menu handlers --------

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mMenu == null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.map_menu, menu);
            mMenu = menu;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_mylocation: {
            getMyLocation();
            break;
        }
        case R.id.menu_download: {
            setupMapDownload(true);
            break;
        }
        case R.id.menu_about: {
            showDialog(DIALOG_ABOUT);
            break;
        }
        default:
            return false;
        }
        return true;
    }

    // setup UI ---------

    /**
     * Sets the app params.
     */
    private void setActivityParams() {
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
        getWindow().setBackgroundDrawable(null);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mActivityHandler.sendMessage(mActivityHandler.obtainMessage(ActivityHandler.MESSAGE_INIT_FILE_SYSTEM_DIR));

        mMapDownloadThreads = new MapDownloadThread[DOWNLOAD_THREAD_COUNT];
    }

    /**
     * Setup views.
     */
    private void setupViews() {
        setContentView(R.layout.map);
        final FrameLayout contentView = (FrameLayout) findViewById(android.R.id.content);
        if (contentView != null) {
            contentView.setBackgroundDrawable(null);
            contentView.setForeground(null);
        }

        // setup top panel
        setupHeaderPanel();

        // setup map component
        setupMapComponent();

        // create map view now
        setupMapView();

        // setup loc source
        mActivityHandler.sendMessageDelayed(
                mActivityHandler.obtainMessage(ActivityHandler.MESSAGE_INIT_LOCATION_SOURCE_N_MAP_LISTENER), 1000);

        // create zoom controls
        setupZoomControls();
    }

    /**
     * Setup map component.
     */
    private void setupMapComponent() {
        if (mMapComponent == null) {
            mMapComponent = new CustomMapComponent(NUTITEQ_LICENSE_KEY, NUTITEQ_VENDOR, NUTITEQ_APP_NAME, 1, 1,
                    BURN_CENTER_POINT, BURN_DEFULT_ZOOM);

            mMapComponent.setMap(BURN_MAP_SOURCE);

            mMapComponent.setPanningStrategy(new ThreadDrivenPanning());

            mControlKeysHandler = new AndroidKeysHandler();
            mMapComponent.setControlKeysHandler(mControlKeysHandler);

            mMapComponent.setZoomLevelIndicator(new DefaultZoomIndicator(BURN_MAP_SOURCE.getMinZoom(), BURN_MAP_SOURCE
                    .getMaxZoom()));
            mMapComponent.setFileSystem(new AndroidFileSystem());
            mMapComponent.setTouchClickTolerance(BasicMapComponent.FINGER_CLICK_TOLERANCE);

            // setup cache
            setupDiskCache();

            // start mapping
            startMapping();
        }
    }

    /**
     * Setup map view.
     */
    private void setupMapView() {
        mMapTouchListener = new MapTouchListener(this);

        // setup map view
        mMapView = new CustomMapView(getBaseContext(), mMapComponent);
        mMapView.setOnTouchListener(mMapTouchListener);
        mMapView.setClickable(true);
        mMapView.setEnabled(true);

        // cache map layout view
        mMapLayoutView = (FrameLayout) findViewById(R.id.map_layout);

        final RelativeLayout.LayoutParams mapViewLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        mMapLayoutView.addView(mMapView, 0, mapViewLayoutParams);
    }

    /**
     * Setup map listener.
     */
    private void setupMapListener() {
        mMapComponent.setMapListener(new DelayedMapListener(mMapComponent.getMapListener()) {

            @Override
            public void mapMoved() {
                mMapView.post(new Runnable() {

                    @Override
                    public void run() {
                        final AppLocationProvider locProvider = getActiveLocationProvider();
                        if (mGetMyLocation && locProvider != null) {
                            final LocationMarker locationMarker = locProvider.getLocationMarker();
                            locationMarker.setTrackingEnabled(false);

                            mGetMyLocation = false;
                        }
                        mMapTouchListener.mapMoved();
                    }
                });
            }

            @Override
            public void mapClicked(final WgsPoint point) {
                mMapView.post(new Runnable() {

                    @Override
                    public void run() {
                        mMapTouchListener.mapClicked(point);
                    }
                });
            }
        });
    }

    /**
     * Setup disk cache.
     */
    private void setupDiskCache() {
        final boolean fileSystemAvailable = Utils.isSDCardAvailable();
        if (!fileSystemAvailable) {
            showDialog(DIALOG_SDCARD_NOT_AVAILABLE);
        } else {
            final File externalStoragePath = Environment.getExternalStorageDirectory();
            if (externalStoragePath != null) {
                final File appDir = new File(externalStoragePath, APP_DIR_ON_SD_CARD);
                appDir.mkdirs();

                final File cacheDir = new File(appDir, DIR_MAP_CACHE);
                cacheDir.mkdirs();

                mSavedMapCache = new FastAndroidFileSystemCache(getBaseContext(), cacheDir);
                mSavedMapCache.initialize();
                final CachingChain cachingChain = new CachingChain(new Cache[] { mSavedMapCache });

                mMapComponent.setNetworkCache(cachingChain);
            }
        }
    }

    private FastAndroidFileSystemCache getNewSavedMapCache() {
        final File externalStoragePath = Environment.getExternalStorageDirectory();
        if (externalStoragePath != null) {
            final File appDir = new File(externalStoragePath, APP_DIR_ON_SD_CARD);
            appDir.mkdirs();

            final File cacheDir = new File(appDir, DIR_MAP_CACHE);
            cacheDir.mkdirs();

            FastAndroidFileSystemCache savedMapCache = new FastAndroidFileSystemCache(getBaseContext(), cacheDir);
            savedMapCache.initialize();
            return savedMapCache;
        }
        return null;
    }

    /**
     * Setup zoom controls.
     */
    private void setupZoomControls() {
        // Create new ZoomControls
        mZoomControls = (MapZoomControls) findViewById(R.id.zoom_controls);
        mZoomControls.setVisibility(View.GONE);

        mZoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                zoomIn(1);
            }
        });
        mZoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                zoomOut(1);
            }
        });
    }

    /**
     * Sets the location source.
     */
    private void setupLocationSource() {
        final AppLocationProvider locProvider = getActiveLocationProvider();

        locProvider.setLocationMarker(createLocationMarker());
        locProvider.setLocationListener(new AppLocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)) {
                    if (status == LocationProvider.OUT_OF_SERVICE) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mMessageTextView.setText(R.string.msg_locating);

                        UIUtils.showPanel(getBaseContext(), mMessagePanel, false);
                    } else {
                        UIUtils.hidePanel(getBaseContext(), mMessagePanel, false);
                    }
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
                if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)) {
                    checkGPSProvider();
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)) {
                    checkGPSProvider();
                }
            }

            @Override
            public void onLocationChanged(String provider, Location location) {
                if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)) {
                    if (Globals.sCurrentGPSLocation == null) {
                        CompassUtils.updateUserBearing(false, location.getBearing());
                    } else {
                        final boolean hasBearing = location.hasBearing();
                        final float pointBearing = MapUtils.calculateBearing(Globals.sCurrentGPSLocation, location);
                        CompassUtils.updateUserBearing(hasBearing, (hasBearing) ? location.getBearing() : pointBearing);
                    }

                    if (mMessagePanel.getVisibility() == View.VISIBLE) {
                        mProgressBar.setVisibility(View.GONE);
                        UIUtils.hidePanel(getBaseContext(), mMessagePanel, false);
                    }
                } else {
                    CompassUtils.updateUserBearing(false, location.getBearing());

                    if (mGetMyLocation) {
                        final AppLocationProvider locProvider = getActiveLocationProvider();
                        final LocationMarker locationMarker = locProvider.getLocationMarker();
                        locationMarker.updatePosition();
                    }
                }
            }

            @Override
            public void onProviderChanged(String oldProvider, String newProvider) {
                if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(newProvider)) {
                    mMapComponent.removeLocationSource();

                    final AppLocationProvider locProvider = getActiveLocationProvider();
                    final LocationMarker locationMarker = createLocationMarker();

                    locProvider.setLocationMarker(locationMarker);
                    mMapComponent.setLocationSource(locProvider);

                    locationMarker.setTrackingEnabled(mGetMyLocation);
                }
            }
        });

        mMapComponent.setLocationSource(locProvider);
    }

    /**
     * Setup button panel.
     */
    private void setupHeaderPanel() {
        mMessagePanel = (LinearLayout) findViewById(R.id.message_panel);
        mMessageTextView = (TextView) findViewById(R.id.label_message);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_circular);

        mMessageTextView.setOnClickListener(null);

        // Add track location button
        final Button homeButton = (Button) findViewById(R.id.btn_mylocation);
        homeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMapComponent.setMiddlePoint(BURN_CENTER_POINT, BURN_DEFULT_ZOOM);
            }
        });

        mCompassView = (CompassView) findViewById(R.id.compass_view);
        mCompassView.setVisibility(View.VISIBLE);
    }

    /**
     * Check gps provider.
     */
    private void checkGPSProvider() {
        final LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mProgressBar.setVisibility(View.GONE);
            mMessageTextView.setText(R.string.error_gps_disabled);
            mMessageTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        mVibrator.vibrate(50);

                        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    } catch (Exception e) {
                        // we dont care
                    }
                }
            });

            UIUtils.showPanel(getBaseContext(), mMessagePanel, false);
        } else if (Globals.sCurrentGPSLocation == null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mMessageTextView.setText(R.string.msg_locating);

            UIUtils.showPanel(getBaseContext(), mMessagePanel, false);
        } else {
            UIUtils.hidePanel(getBaseContext(), mMessagePanel, false);
        }
    }

    /**
     * Start mapping.
     */
    private void startMapping() {
        try {
            // start mapping
            mMapComponent.startMapping();
        } catch (Exception e) {
            try {
                Log.e(TAG, "error starting mapping.. cleaningup first..", e);
                if (mMapView != null) {
                    mMapView.clean();
                    mMapView = null;
                }
                mMapComponent.stopMapping();
                MappingCore.getInstance().setTasksRunner(null);
                final Field field = BasicMapComponent.class.getDeclaredField("taskRunner");
                field.setAccessible(true);
                field.set(mMapComponent, MappingCore.getInstance().getTasksRunner());

                // try to start mapping again
                mMapComponent.startMapping();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Show zoom level toast.
     */
    private void showZoomLevelToast() {
        UIUtils.showDefaultToast(getBaseContext(),
                String.format(getString(R.string.toast_zoom_level), mMapComponent.getZoom()), false);
    }

    // manage receivers ----------

    /**
     * Register receiver.
     */
    private void registerReceivers() {
        if (mWakeLock == null) {
            final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
        }
        mWakeLock.acquire();

        mCompassView.registerReceiver();
    }

    /**
     * Unregister receivers.
     */
    private void unregisterReceivers() {
        mCompassView.unregisterReceiver();

        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    /**
     * Zoom in.
     * 
     * @param factor
     *            the factor
     */
    public void zoomIn(int factor) {
        for (int i = 0; i < factor; i++) {
            mMapComponent.zoomIn();
        }
        setZoomControlsEnabled();
        showZoomLevelToast();
    }

    /**
     * Zoom in to point.
     * 
     * @param point
     *            the point
     * @param factor
     *            the factor
     */
    public void zoomInToPoint(WgsPoint point, int factor) {
        mMapComponent.moveMap(point);
        zoomIn(factor);
    }

    /**
     * Zoom out to point.
     * 
     * @param point
     *            the point
     * @param factor
     *            the factor
     */
    public void zoomOutToPoint(WgsPoint point, int factor) {
        mMapComponent.moveMap(point);
        zoomOut(factor);
    }

    /**
     * Zoom out.
     * 
     * @param factor
     *            the factor
     */
    public void zoomOut(int factor) {
        for (int i = 0; i < factor; i++) {
            mMapComponent.zoomOut();
        }
        setZoomControlsEnabled();
        showZoomLevelToast();
    }

    /**
     * Convert screen point to wgs.
     * 
     * @param x
     *            the x
     * @param y
     *            the y
     * 
     * @return the wgs point
     */
    public WgsPoint convertScreenPointToWgs(int x, int y) {
        final int mapZoom = mMapComponent.getZoom();

        final MapPos mapMiddlePos = BURN_MAP_SOURCE
                .wgsToMapPos(mMapComponent.getMiddlePoint().toInternalWgs(), mapZoom);

        final int mapMiddleX = mapMiddlePos.getX() - (mMapView.getWidth() / 2);
        final int mapMiddleY = mapMiddlePos.getY() - (mMapView.getHeight() / 2);

        final MapPos overlayMiddlePos = new MapPos(mapMiddleX + x, mapMiddleY + y, mapZoom);

        return BURN_MAP_SOURCE.mapPosToWgs(overlayMiddlePos).toWgsPoint();
    }

    /**
     * Gets the dummy location marker.
     * 
     * @return the dummy location marker
     */
    public LocationMarker getDummyLocationMarker() {
        if (mDummyLocationMarker == null) {
            mDummyLocationMarker = new NutiteqLocationMarker(new PlaceIcon(
                    com.nutiteq.utils.Utils.createImage(PATH_BLANK_IMAGE)), 3000, false);
        }
        return mDummyLocationMarker;
    }

    // utility methods ----------

    /**
     * Creates the location marker.
     * 
     * @return the location marker
     */
    private LocationMarker createLocationMarker() {
        return new NutiteqLocationMarker(new CompassPlaceIcon(com.nutiteq.utils.Utils.createImage(PATH_IMAGE_COMPASS),
                getBaseContext()), 3000, false);
    }

    /**
     * Track location.
     */
    private void getMyLocation() {
        final AppLocationProvider locProvider = getActiveLocationProvider();
        if (locProvider != null) {
            final LocationMarker locationMarker = locProvider.getLocationMarker();
            final WgsPoint point = locProvider.getLocation();

            if (point != null) {
                locationMarker.setLocation(point);
                moveMap(locProvider.getLocation());
                locationMarker.updatePosition();

                if (mGetMyLocation) {
                    locationMarker.setTrackingEnabled(false);
                    mGetMyLocation = false;
                }
            } else {
                UIUtils.showDefaultToast(getBaseContext(), R.string.toast_retrieving_current_loc, false);

                if (!mGetMyLocation) {
                    mGetMyLocation = true;
                    locationMarker.setTrackingEnabled(true);
                    locationMarker.updatePosition();
                }
            }
        } else {
            Log.d(TAG, "Get my loc button clicked. Active loc source is null.");
        }
    }

    /**
     * Do cleanup.
     */
    private void doCleanup() {
        try {
            if (mMapView != null) {
                mMapView.clean();
                mMapView = null;
            }

            mMapComponent.stopMapping();
            mMapComponent = null;
        } catch (Exception e) {
            // we don't care if something fails above
        }
    }

    /**
     * Move map.
     * 
     * @param point
     *            the point
     */
    private void moveMap(WgsPoint point) {
        final double lat1 = point.getLat();
        final double lon1 = point.getLon();

        final WgsPoint middlePoint = mMapComponent.getMiddlePoint();
        final double lat0 = middlePoint.getLat();
        final double lon0 = middlePoint.getLon();

        final double latDiff = (lat1 - lat0) / 20;
        final double lonDiff = (lon1 - lon0) / 20;

        for (int i = 1; i < 21; i++) {
            final Bundle bundle = new Bundle();
            if (i != 20) {
                bundle.putDouble(KEY_LAT, (lat0 + (latDiff * i)));
                bundle.putDouble(KEY_LON, (lon0 + (lonDiff * i)));
            } else {
                bundle.putDouble(KEY_LAT, lat1);
                bundle.putDouble(KEY_LON, lon1);
            }

            final Message msg = mActivityHandler.obtainMessage(ActivityHandler.MESSAGE_MOVE_MAP);
            msg.setData(bundle);
            mActivityHandler.sendMessageDelayed(msg, i * 30);
        }
    }

    // package scope methods

    /**
     * Sets the zoom controls enabled.
     */
    void setZoomControlsEnabled() {
        final ZoomRange zoomRange = mMapComponent.getZoomRange();
        final int zoomLevel = mMapComponent.getZoom();
        mZoomControls.setIsZoomInEnabled(zoomRange.getMaxZoom() != zoomLevel);
        mZoomControls.setIsZoomOutEnabled(zoomRange.getMinZoom() != zoomLevel);
    }

    /**
     * Sets the zoom controls visible.
     * 
     * @param visible
     *            the new zoom controls visible
     */
    void setZoomControlsVisible(boolean visible) {
        if (visible) {
            UIUtils.fadeView(mZoomControls, View.VISIBLE, 0f, 1f);
        } else {
            UIUtils.fadeView(mZoomControls, View.GONE, 1f, 0f);
        }
    }

    DefaultHttpClient getHttpClient() {
        // Create and initialize HTTP parameters
        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(10));
        ConnManagerParams.setMaxTotalConnections(params, 100);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(params, "Mozilla");

        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        // Default connection and socket timeout of 20 seconds. Tweak to
        // taste.
        HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
        HttpConnectionParams.setSoTimeout(params, 30 * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpClientParams.setRedirecting(params, true);

        // Create and initialize scheme registry
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        // Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        return new DefaultHttpClient(cm, params);
    }

    LinkedList<String> getUrlList() {
        final IMapSource mapSource = BURN_MAP_SOURCE;

        final WgsPoint startPoint = BURN_START_POINT;
        final WgsPoint endPoint = BURN_END_POINT;

        final int tileSize = mapSource.getTileSize();

        final LinkedList<String> urlList = new LinkedList<String>();
        for (int zoom = mapSource.getMinZoom(), endZoom = mapSource.getMaxZoom(), i = 0; zoom <= endZoom; zoom++, i++) {
            // switching to coord system
            final MapPos mapStartPos = mapSource.wgsToMapPos(startPoint.toInternalWgs(), zoom);
            final MapPos mapEndPos = mapSource.wgsToMapPos(endPoint.toInternalWgs(), zoom);

            final Rect overlayRect = new Rect();
            overlayRect.left = mapStartPos.getX() / tileSize;
            overlayRect.top = mapStartPos.getY() / tileSize;

            overlayRect.right = mapEndPos.getX() / tileSize;
            overlayRect.bottom = mapEndPos.getY() / tileSize;

            for (int x = overlayRect.left; x <= overlayRect.right; x++) {
                for (int y = overlayRect.top; y <= overlayRect.bottom; y++) {
                    final int tileX = x * tileSize;
                    final int tileY = y * tileSize;

                    urlList.add(mapSource.buildPath(tileX, tileY, zoom));
                }
            }
        }
        return urlList;
    }

    // protected methods ----------

    protected AppLocationProvider getActiveLocationProvider() {
        return getBurnApplication().getActiveLocationProvider();
    }

    protected BurnApplication getBurnApplication() {
        return (BurnApplication) getApplication();
    }

    // inner classes ----------

    /**
     * The Class ActivityHandler.
     */
    private class ActivityHandler extends Handler {

        /** The Constant MESSAGE_MOVE_MAP. */
        private static final int MESSAGE_MOVE_MAP = 0;

        /** The Constant MESSAGE_SAVE_PREF_BOOL. */
        private static final int MESSAGE_SAVE_PREF_BOOL = 1;

        /** The Constant MESSAGE_SAVE_PREF_INT. */
        private static final int MESSAGE_SAVE_PREF_INT = 2;

        /** The Constant MESSAGE_SAVE_PREF_LONG. */
        private static final int MESSAGE_SAVE_PREF_LONG = 3;

        /** The Constant MESSAGE_SAVE_PREF_FLOAT. */
        private static final int MESSAGE_SAVE_PREF_FLOAT = 4;

        /** The Constant MESSAGE_SAVE_PREF_STRING. */
        private static final int MESSAGE_SAVE_PREF_STRING = 5;

        /** The Constant MESSAGE_DO_GRACEFUL_FINISH. */
        private static final int MESSAGE_DO_GRACEFUL_FINISH = 6;

        /** The Constant MESSAGE_TIMEOUT_CLOSING_RESOURCES. */
        private static final int MESSAGE_TIMEOUT_CLOSING_RESOURCES = 7;

        /** The Constant MESSAGE_INIT_FILE_SYSTEM_DIR. */
        private static final int MESSAGE_INIT_FILE_SYSTEM_DIR = 8;

        /** The Constant MESSAGE_INIT_LOCATION_SOURCE_N_MAP_LISTENER. */
        private static final int MESSAGE_INIT_LOCATION_SOURCE_N_MAP_LISTENER = 9;

        /** The Constant MESSAGE_FREE_MAP_COMPONENT_MEMORY. */
        private static final int MESSAGE_FREE_MAP_COMPONENT_MEMORY = 10;

        /** The Constant MESSAGE_REPAINT_MAP_COMPONENT. */
        private static final int MESSAGE_REPAINT_MAP_COMPONENT = 11;

        private static final int MESSAGE_START_DOWNLOAD = 12;

        private static final int MESSAGE_DOWNLOAD_PROGRESS = 13;

        private static final int MESSAGE_DOWNLOAD_ERROR = 14;

        private static final int MESSAGE_END_DOWNLOAD = 15;

        private int mLastCount = 0;

        private long mStartTime = 0;

        /*
         * (non-Javadoc)
         * 
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            try {
                final Bundle bundle = msg.getData();
                switch (msg.what) {
                case MESSAGE_MOVE_MAP:
                    if (bundle != null) {
                        double lat = bundle.getDouble(KEY_LAT);
                        double lon = bundle.getDouble(KEY_LON);
                        mMapComponent.moveMap(new WgsPoint(lon, lat));
                    }
                    break;
                case MESSAGE_SAVE_PREF_BOOL:
                case MESSAGE_SAVE_PREF_INT:
                case MESSAGE_SAVE_PREF_LONG:
                case MESSAGE_SAVE_PREF_FLOAT:
                case MESSAGE_SAVE_PREF_STRING:
                    if (bundle != null) {
                        final SharedPreferences.Editor editor = Globals.sSharedPreferences.edit();

                        final Set<String> keys = bundle.keySet();
                        for (String key : keys) {
                            switch (msg.what) {
                            case MESSAGE_SAVE_PREF_BOOL:
                                editor.putBoolean(key, bundle.getBoolean(key));
                                break;
                            case MESSAGE_SAVE_PREF_INT:
                                editor.putInt(key, bundle.getInt(key));
                                break;
                            case MESSAGE_SAVE_PREF_LONG:
                                editor.putLong(key, bundle.getLong(key));
                                break;
                            case MESSAGE_SAVE_PREF_FLOAT:
                                editor.putFloat(key, bundle.getFloat(key));
                                break;
                            case MESSAGE_SAVE_PREF_STRING:
                                editor.putString(key, bundle.getString(key));
                                break;
                            default:
                                break;
                            }
                        }

                        editor.commit();
                    }
                    break;
                case MESSAGE_TIMEOUT_CLOSING_RESOURCES: {
                    removeMessages(MESSAGE_DO_GRACEFUL_FINISH);
                    removeDialog(DIALOG_CLOSING_RESOURCES);
                    finish();
                }
                    break;
                case MESSAGE_FREE_MAP_COMPONENT_MEMORY: {
                    removeMessages(MESSAGE_TIMEOUT_CLOSING_RESOURCES);
                    sendMessage(obtainMessage(MESSAGE_TIMEOUT_CLOSING_RESOURCES));
                }
                    break;
                case MESSAGE_INIT_FILE_SYSTEM_DIR: {
                    if (Utils.isSDCardAvailable()) {
                        try {
                            final File file = new File(MapUtils.getSDCardDestinationDir());
                            file.mkdirs();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                    break;
                case MESSAGE_INIT_LOCATION_SOURCE_N_MAP_LISTENER: {
                    setupLocationSource();
                    setupMapListener();
                }
                    break;
                case MESSAGE_REPAINT_MAP_COMPONENT: {
                    if (mMapView != null) {
                        mMapView.needRepaint(true, true);
                    }
                }
                    break;
                case MESSAGE_START_DOWNLOAD: {
                    mLastCount = 0;
                    mStartTime = System.currentTimeMillis();

                    if (mDefaultHttpClient1 == null) {
                        mDefaultHttpClient1 = getHttpClient();
                    }

                    FastAndroidFileSystemCache cache = getNewSavedMapCache();
                    MapDownloadListerner mapDownloadListerner = new MapDownloadListernerImpl();

                    mFileCacheWriter = new FileCacheWriter(cache, mapDownloadListerner);
                    mFileCacheWriter.start();

                    LinkedList<String> urlList = getUrlList();
                    mTotalTileCount = urlList.size();
                    for (int i = 0, len = mMapDownloadThreads.length; i < len; i++) {
                        MapDownloadThread t = new MapDownloadThread(getBaseContext(), cache, mapDownloadListerner,
                                mDefaultHttpClient1, i + 1, mFileCacheWriter, urlList);
                        mMapDownloadThreads[i] = t;
                        t.start();
                    }
                }
                    break;
                case MESSAGE_DOWNLOAD_PROGRESS:
                    ++mLastCount;
                    if (mLastCount % 1000 == 0) {
                        long t = System.currentTimeMillis();
                        Log.d(TAG, "Downloaded 1000 files in " + ((t - mStartTime) / 1000f) + " seconds");
                        mStartTime = t;
                    }
                    mProgressDialog.setProgress(mLastCount);
                    mProgressDialog.setMax(mTotalTileCount);
                    break;
                case MESSAGE_DOWNLOAD_ERROR:
                    for (int i = 0, len = mMapDownloadThreads.length; i < len; i++) {
                        MapDownloadThread t = mMapDownloadThreads[i];
                        if (t != null) {
                            t.cancel();
                        }
                        mMapDownloadThreads[i] = null;
                    }

                    if (mFileCacheWriter != null) {
                        mFileCacheWriter.stopRunning();
                        mFileCacheWriter = null;
                    }

                    removeDialog(DIALOG_DOWNLOAD_PROGRESS);
                    switch (bundle.getInt(KEY_ID)) {
                    case STATUS_FILE_ERROR:
                        mErrorMsgId = R.string.error_download_file_error;
                        break;
                    case STATUS_NETWORK_ERROR:
                        mErrorMsgId = R.string.error_download_network_error;
                        break;
                    case STATUS_SERVER_ERROR:
                        mErrorMsgId = R.string.error_download_server_error;
                        break;
                    default:
                        mErrorMsgId = R.string.error_download_unknown_error;
                        break;
                    }
                    showDialog(DIALOG_DOWNLOAD_ERROR);
                    break;
                case MESSAGE_END_DOWNLOAD:
                    boolean isAnyAlive = false;
                    for (MapDownloadThread t : mMapDownloadThreads) {
                        if (t != null) {
                            if (t.isAlive()) {
                                isAnyAlive = true;
                                break;
                            }
                        }
                    }
                    if (!isAnyAlive) {
                        if (mFileCacheWriter != null) {
                            mFileCacheWriter.stopRunning();
                            mFileCacheWriter = null;
                        }

                        removeDialog(DIALOG_DOWNLOAD_PROGRESS);
                    }
                    break;
                default:
                    break;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class MapDownloadListernerImpl implements MapDownloadListerner {

        @Override
        public void notifyProgress() {
            final Message msg = mActivityHandler.obtainMessage(ActivityHandler.MESSAGE_DOWNLOAD_PROGRESS);
            mActivityHandler.sendMessageDelayed(msg, 100);
        }

        @Override
        public void notifyError(int status) {
            final Message msg = mActivityHandler.obtainMessage(ActivityHandler.MESSAGE_DOWNLOAD_ERROR);

            Bundle data = new Bundle();
            data.putInt(KEY_ID, status);

            msg.setData(data);
            mActivityHandler.sendMessageDelayed(msg, 100);
        }

        @Override
        public void notifyComplete() {
            mActivityHandler.sendMessageDelayed(mActivityHandler.obtainMessage(ActivityHandler.MESSAGE_END_DOWNLOAD),
                    100);
            final Message msg = mActivityHandler.obtainMessage(ActivityHandler.MESSAGE_SAVE_PREF_BOOL);

            Bundle data = new Bundle();
            data.putBoolean(PREF_KEY_MAP_DOWNLOADED, true);

            msg.setData(data);
            mActivityHandler.sendMessageDelayed(msg, 100);
        }

        @Override
        public void notifyCancel() {
            mActivityHandler.sendMessageDelayed(mActivityHandler.obtainMessage(ActivityHandler.MESSAGE_END_DOWNLOAD),
                    100);
        }
    }

    /**
     * The Class FileCacheWriter.
     */
    static public class FileCacheWriter extends Thread {

        /** The cache. */
        final private FastAndroidFileSystemCache mCache;

        /** The stack. */
        private List<FileCacheInfo> mStack = Collections.synchronizedList(new ArrayList<FileCacheInfo>());

        /** The keep running. */
        private boolean mKeepRunning = true;

        /** The lock. */
        private Object mLock = new Object();

        private MapDownloadListerner mMapDownloadListerner;

        /** The error. */
        public boolean mError;

        /** The exception. */
        public Exception mException;

        /**
         * The Class FileCacheInfo.
         */
        static public class FileCacheInfo {

            /** The url. */
            public String mUrl;

            /** The data. */
            public byte[] mData;
        }

        /**
         * Instantiates a new file cache writer.
         * 
         * @param cache
         *            the cache
         */
        FileCacheWriter(FastAndroidFileSystemCache cache, MapDownloadListerner mapDownloadListerner) {
            mCache = cache;
            mMapDownloadListerner = mapDownloadListerner;
        }

        /**
         * Push.
         * 
         * @param data
         *            the data
         */
        synchronized public void push(FileCacheInfo data) {
            while (mStack.size() >= 300) {
                try {
                    Thread.currentThread().sleep(50);
                } catch (Exception e) {

                }
            }
            mStack.add(data);

            try {
                synchronized (mLock) {
                    mLock.notify();
                }
            } catch (Exception e) {
                Log.e(TAG, "Thread. Error while notifying.", e);
            }
        }

        /**
         * Stop running.
         */
        public void stopRunning() {
            mKeepRunning = false;
            try {
                synchronized (mLock) {
                    mLock.notify();
                }
            } catch (Exception e) {
                Log.e(TAG, "Thread. Error while notifying.", e);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        public void run() {
            long startTime = System.currentTimeMillis();
            while (mKeepRunning || !mStack.isEmpty()) {
                // Log.i(TAG, "writer running.. " + mStack.size());
                if (!mStack.isEmpty()) {
                    try {
                        final FileCacheInfo info = mStack.get(0);
                        mStack.remove(info);

                        mCache.cache(info.mUrl, info.mData, Cache.CACHE_LEVEL_PERSISTENT);
                        notifyChange();
                    } catch (Exception e) {
                        mError = true;
                        mException = e;
                        Log.e(TAG, "Thread. Error in writting file.", e);
                    }
                } else {
                    try {
                        // Log.i(TAG, "writer waiting.. " + mStack.size());
                        synchronized (mLock) {
                            mLock.wait();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Thread. Error while waiting.", e);
                    }
                }
            }
            long t = System.currentTimeMillis();
            Log.i(TAG, "Thread. FileCacheWriter thread ended. Total time " + ((t - startTime) / 1000f) + " seconds");
        }

        /**
         * Notify change.
         * 
         * @param id
         *            the id
         * @param fileCount
         *            the file count
         */
        private void notifyChange() {
            mMapDownloadListerner.notifyProgress();
        }
    }

    // getters and setters

    /**
     * Gets the map component.
     * 
     * @return the map component
     */
    public BasicMapComponent getMapComponent() {
        return mMapComponent;
    }

    /**
     * Gets the map view.
     * 
     * @return the map view
     */
    public MapView getMapView() {
        return mMapView;
    }
}