package com.trailbehind.android.iburn.util;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;
import android.view.View;

import com.nutiteq.components.PlaceIcon;
import com.nutiteq.wrappers.Graphics;
import com.nutiteq.wrappers.Image;
import com.trailbehind.android.iburn.R;

/**
 * The Class CompassUtils.
 */
public class CompassUtils {

    /** The Constant NOT_SET. */
    static final private int NOT_SET = -999;

    /** The sensor manager. */
    static private SensorManager sSensorManager;

    /** The view to invalidate. */
    static private WeakReference<View> sViewToInvalidateRef;

    /** The compass bitmap. */
    static private Bitmap sCompassBitmap;

    /** The rotated compass bitmap. */
    static private Bitmap sRotatedCompassBitmap;

    /** The paint. */
    static private Paint sPaint;

    /** The last sensor bearing. */
    static private float sLastSensorBearing = NOT_SET;

    /** The last user bearing. */
    static private float sLastUserBearing = NOT_SET;

    // sensor listener --------

    /** The sensor listener. */
    static private SensorEventListener sSensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float sensorBearing = event.values[0];
            float pitch = event.values[2];

            float diff = sLastSensorBearing - sensorBearing;
            if (Math.abs(diff) > 5) {
                sLastSensorBearing = sensorBearing + getOrientationDelta(pitch);

                final WeakReference<View> viewReference = sViewToInvalidateRef;
                if (viewReference != null) {
                    final View view = sViewToInvalidateRef.get();
                    if (view != null) {
                        sViewToInvalidateRef.get().invalidate();
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // for now, we are not handling accuracy
        }

        private int getOrientationDelta(float pitch) {
            final Resources resources = Globals.sResources;
            if (resources != null) {
                final int orientation = resources.getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    return (pitch > 0) ? 90 : -90;
                }
            }
            return 0;
        }
    };

    /**
     * Update user bearing.
     * 
     * @param userBearing the user bearing
     * @param hasBearing the has bearing
     */
    static public void updateUserBearing(boolean hasBearing, float userBearing) {
        boolean invalidate = false;
        if (!hasBearing) {
            invalidate = true;
            sLastUserBearing = NOT_SET;
        } else {
            float diff = sLastUserBearing - userBearing;
            if (Math.abs(diff) > 5) {
                invalidate = true;
                sLastUserBearing = userBearing;
            }
        }
        if (invalidate) {
            final WeakReference<View> viewReference = sViewToInvalidateRef;
            if (viewReference != null) {
                final View view = sViewToInvalidateRef.get();
                if (view != null) {
                    sViewToInvalidateRef.get().invalidate();
                }
            }
        }
    }

    // register/unregister receiver --------

    /**
     * Register receiver.
     * 
     * @param context the context
     * @param viewToInvalidate the map view
     */
    static public void registerReceiver(Context context, View viewToInvalidate) {
        sViewToInvalidateRef = new WeakReference<View>(viewToInvalidate);
        
        /*
        if (sSensorManager == null) {
            sSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sSensorManager.registerListener(sSensorListener, sSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
        */
    }

    /**
     * Unregister receiver.
     */
    static public void unregisterReceiver() {
        if (sSensorManager != null) {
            sSensorManager.unregisterListener(sSensorListener);
        }

        if (sCompassBitmap != null) {
            sCompassBitmap.recycle();
            sCompassBitmap = null;
        }

        if (sRotatedCompassBitmap != null) {
            sRotatedCompassBitmap.recycle();
            sRotatedCompassBitmap = null;
        }

        System.gc();

        // reset everything
        sSensorManager = null;
        sViewToInvalidateRef = null;
        sLastUserBearing = NOT_SET;
        sLastSensorBearing = NOT_SET;
    }

    // draw compass image --------

    /**
     * Gets the bearing image.
     * 
     * @param context the context
     * 
     * @return the bearing image
     */
    static public Image getBearingImage(Context context) {
        if (sCompassBitmap == null) {
            sCompassBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.compass_arrow);

            sPaint = new Paint();
            sPaint.setAntiAlias(true);
        }

        int w = sCompassBitmap.getWidth();
        int h = sCompassBitmap.getHeight();
        int cx = w / 2;
        int cy = h / 2;

        if (sRotatedCompassBitmap == null) {
            sRotatedCompassBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        } else {
            sRotatedCompassBitmap.eraseColor(Color.TRANSPARENT);
        }

        final Canvas canvas = new Canvas(sRotatedCompassBitmap);

        canvas.translate(cx, cy);
        if (sLastUserBearing != NOT_SET) {
            canvas.rotate(sLastUserBearing);
        } else if (sLastSensorBearing  != NOT_SET) {
            // special handling for compass reading
            canvas.rotate(-sLastSensorBearing);
        }
        canvas.drawBitmap(sCompassBitmap, -cx, -cy, sPaint);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sRotatedCompassBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();

        return Image.createImage(bytes, 0, bytes.length);
    }

    // inner classes --------

    /**
     * The Class CompassPlaceIcon.
     */
    static public class CompassPlaceIcon extends PlaceIcon {

        /** The context. */
        private Context mContext;

        /**
         * Instantiates a new compass place icon.
         * 
         * @param image the image
         * @param context the context
         */
        public CompassPlaceIcon(Image image, Context context) {
            super(image);
            mContext = context;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.nutiteq.components.PlaceIcon#paint(javax.microedition.lcdui.Graphics
         * , int, int, int)
         */
        @Override
        public void paint(Graphics graphics, int screenX, int screenY, int zoom) {
            try {
                if ((sLastUserBearing == NOT_SET) && (sLastSensorBearing == NOT_SET)) {
                    super.paint(graphics, screenX, screenY, zoom);
                } else {
                    int dx = 0;
                    int dy = 0;
                    if (sLastUserBearing != NOT_SET) {
                        final float alpha = MathematicalConstants.DEG2RAD * (-sLastUserBearing + 90f);
                        dx = (int) FloatMath.cos(alpha);
                        dy = (int) FloatMath.sin(alpha);
                    }
                    graphics.drawImage(getBearingImage(mContext), screenX + dx, screenY - dy, Graphics.LEFT
                            | Graphics.TOP);
                    // graphics.drawRect(screenX, screenY, 2, 2);
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
    }
}
