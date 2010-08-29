package com.trailbehind.android.iburn.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;

/**
 * The Class Globals.
 */
public class Globals implements IConstants {

    // private fields --------

    // globals initialized flag
    /** The initialized. */
    static private boolean sInitialized = false;

    /** The base context. */
    static public Context sContext;

    /** The layout factory. */
    static public LayoutInflater sLayoutFactory;

    /** The resources. */
    static public Resources sResources;

    /** The content resolver. */
    static public ContentResolver sContentResolver;

    /** The shared preferences. */
    static public SharedPreferences sSharedPreferences;

    /** The current location. */
    static public Location sLastSavedLocation;

    /** The last known location. */
    static public Location sCurrentGPSLocation;

    /** The cache cleaned. */
    static public boolean sCacheCleared = false;

    /**
     * Inits the.
     * 
     * @param context
     *            the context
     */
    static public void init(Context context) {
        if (!sInitialized) {
            sContext = context;

            sLayoutFactory = LayoutInflater.from(context);
            sResources = context.getResources();
            sContentResolver = context.getContentResolver();
            sLastSavedLocation = new Location(LocationManager.NETWORK_PROVIDER);

            sSharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            loadPreferences();

            sInitialized = true;
        }
    }

    /**
     * Load preferences.
     */
    static public void loadPreferences() {

    }

    /**
     * Cleanup.
     */
    static public void cleanup() {
        sContext = null;

        sLayoutFactory = null;
        sResources = null;
        sContentResolver = null;

        sSharedPreferences = null;
        sLastSavedLocation = null;
        sCurrentGPSLocation = null;

        sInitialized = false;
        sCacheCleared = false;
    }
}
