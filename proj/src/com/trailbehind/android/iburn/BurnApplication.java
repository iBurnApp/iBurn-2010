package com.trailbehind.android.iburn;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.nutiteq.location.providers.AndroidGPSProvider;
import com.trailbehind.android.iburn.util.Globals;
import com.trailbehind.android.iburn.util.IConstants;

import dalvik.system.VMRuntime;

public class BurnApplication extends Application implements IConstants {

    /** The Constant TARGET_HEAP_UTILIZATION. */
    final static private float TARGET_HEAP_UTILIZATION = 0.70f;

    /** The active location source. */
    private AppLocationProvider mActiveLocationProvider;

    /** The gPS location source. */
    private AppLocationProvider mGPSLocationProvider;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "starting app");

        final VMRuntime runtime = VMRuntime.getRuntime();
        // set optimal target heap size for browsers
        runtime.setTargetHeapUtilization(TARGET_HEAP_UTILIZATION);
        runtime.setMinimumHeapSize(4 * 1024 * 1024);

        // init global params
        Globals.init(getBaseContext());

        setupLocationProvider();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Application#onTerminate()
     */
    @Override
    public void onTerminate() {
        // cleanup globals
        Globals.cleanup();

        super.onTerminate();
    }

    // public methods ---------

    /**
     * Gets the active location source.
     * 
     * @return the active location source
     */
    public AppLocationProvider getActiveLocationProvider() {
        if (mActiveLocationProvider == null) {
            setupLocationProvider();
        }
        return mActiveLocationProvider;
    }

    public void onDestroy() {
        if (mActiveLocationProvider != mGPSLocationProvider) {
            mGPSLocationProvider.quit();
        }
        mActiveLocationProvider.quit();

        mGPSLocationProvider = null;
        mActiveLocationProvider = null;

        Globals.sCurrentGPSLocation = null;
        Log.i(TAG, "Loc providers cleared..");
    }

    // private methods ---------

    /**
     * Sets the location source.
     */
    private void setupLocationProvider() {
        final LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mGPSLocationProvider = new AppLocationProvider(locManager, LocationManager.GPS_PROVIDER, 1000L);
        mGPSLocationProvider.start();

        mActiveLocationProvider = new AppLocationProvider(locManager, LocationManager.NETWORK_PROVIDER, 1000L);
        mActiveLocationProvider.start();
    }

    // inner classes ---------

    /**
     * The Class GPSLocationProvider.
     */
    public class AppLocationProvider extends AndroidGPSProvider {

        private String mProvider;
        private AppLocationListener mLocationListener;

        /**
         * Instantiates a new gPS location provider.
         * 
         * @param locationManager
         *            the location manager
         * @param updateInterval
         *            the update interval
         */
        public AppLocationProvider(LocationManager locationManager, String provider, long updateInterval) {
            super(locationManager, provider, updateInterval);
            mProvider = provider;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.nutiteq.location.providers.AndroidGPSProvider#onLocationChanged
         * (android.location.Location)
         */
        @Override
        public void onLocationChanged(Location location) {
            super.onLocationChanged(location);

            if (mLocationListener != null) {
                mLocationListener.onLocationChanged(mProvider, location);
            }

            if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(mProvider)) {
                Globals.sCurrentGPSLocation = location;

                if (mActiveLocationProvider != this) {
                    mActiveLocationProvider.quit();

                    mLocationListener = mActiveLocationProvider.getLocationListener();
                    mActiveLocationProvider = this;

                    if (mLocationListener != null) {
                        mLocationListener.onLocationChanged(mProvider, location);
                        mLocationListener.onProviderChanged(LocationManager.NETWORK_PROVIDER,
                                LocationManager.GPS_PROVIDER);
                    }
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.nutiteq.location.providers.AndroidGPSProvider#onProviderDisabled
         * (java.lang.String)
         */
        @Override
        public void onProviderDisabled(String provider) {
            super.onProviderDisabled(provider);

            if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(mProvider)) {
                Globals.sCurrentGPSLocation = null;
            }
            if (mLocationListener != null) {
                mLocationListener.onProviderDisabled(mProvider);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.nutiteq.location.providers.AndroidGPSProvider#onProviderEnabled
         * (java.lang.String)
         */
        @Override
        public void onProviderEnabled(String provider) {
            super.onProviderEnabled(provider);
            if (mLocationListener != null) {
                mLocationListener.onProviderEnabled(mProvider);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.nutiteq.location.providers.AndroidGPSProvider#onStatusChanged
         * (java.lang.String, int, android.os.Bundle)
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            super.onStatusChanged(provider, status, extras);
            if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(mProvider)) {
                if (status == LocationProvider.OUT_OF_SERVICE) {
                    Globals.sCurrentGPSLocation = null;
                }
            }
            if (mLocationListener != null) {
                mLocationListener.onStatusChanged(provider, status, extras);
            }
        }

        @Override
        public void quit() {
            super.quit();
            if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(mProvider)) {
                Globals.sCurrentGPSLocation = null;
            }
        }

        // public getters n setters ---------

        public String getProvider() {
            return mProvider;
        }

        public AppLocationListener getLocationListener() {
            return mLocationListener;
        }

        public void setLocationListener(AppLocationListener locationListener) {
            this.mLocationListener = locationListener;
        }
    }

    static public interface AppLocationListener {
        public void onLocationChanged(String provider, Location location);

        public void onProviderEnabled(String provider);
        public void onProviderDisabled(String provider);

        public void onProviderChanged(String oldProvider, String newProvider);
        public void onStatusChanged(String provider, int status, Bundle extras);
    }
}
