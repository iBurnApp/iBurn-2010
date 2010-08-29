package com.trailbehind.android.iburn.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;

import com.trailbehind.android.iburn.util.Globals;

/**
 * The Class CompassView.
 */
public class CompassView extends View {

    /** The Constant Y_TEXT_OFFSET. */
    private static final int Y_TEXT_OFFSET = 19;

    /** The Constant NOT_SET. */
    static final private int NOT_SET = -999;

    /** The Constant X_MID. */
    static final private int X_MID = 50;

    /** The sensor manager. */
    static private SensorManager sSensorManager;

    /** The border paint. */
    private Paint mBorderPaint = null;

    /** The inner paint. */
    private Paint mInnerPaint = null;

    /** The text paint. */
    private Paint mTextPaint = null;

    /** The last sensor bearing. */
    static private float sSensorBearing = NOT_SET;

    // sensor listener --------

    /** The sensor listener. */
    private SensorEventListener sSensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float sensorBearing = event.values[0];
            float pitch = event.values[2];

            sSensorBearing = sensorBearing + getOrientationDelta(pitch);
            if (sSensorBearing < 0) {
                sSensorBearing = 360 + sSensorBearing;
            } else if (sSensorBearing > 360) {
                sSensorBearing = sSensorBearing - 360;
            }

            invalidate();
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
     * Instantiates a new compass view.
     * 
     * @param context
     *            the context
     * @param attrs
     *            the attrs
     * @param defStyle
     *            the def style
     */
    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Instantiates a new compass view.
     * 
     * @param context
     *            the context
     * @param attrs
     *            the attrs
     */
    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Instantiates a new compass view.
     * 
     * @param context
     *            the context
     */
    public CompassView(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (mTextPaint == null) {
            mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.setAntiAlias(true);
            mTextPaint.setStrokeCap(Paint.Cap.ROUND);
            mTextPaint.setTextSize(14);
            mTextPaint.setStrokeWidth(2);
            mTextPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        }

        if (mInnerPaint == null) {
            mInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mInnerPaint.setARGB(225, 75, 75, 75);
        }

        if (mBorderPaint == null) {
            mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBorderPaint.setARGB(255, 165, 42, 42);
            mBorderPaint.setAntiAlias(true);
            mBorderPaint.setStrokeWidth(2);
            mBorderPaint.setStrokeCap(Paint.Cap.ROUND);
            mBorderPaint.setStyle(Style.STROKE);
        }

        final int w = getMeasuredWidth();
        final int h = getMeasuredHeight();

        final RectF drawRect = new RectF();
        drawRect.set(1, 1, w - 1, h - 1);

        canvas.drawRoundRect(drawRect, 10, 10, mInnerPaint);

        if (sSensorBearing != NOT_SET) {
            int bearing = (int) sSensorBearing;
            for (int i = bearing, end = bearing - X_MID, x = X_MID; end <= i && x >= 0; i--, x -= 2) {
                if (i % 90 == 0 || i == 0) {
                    drawText(canvas, x, i);
                } else if (i % 30 == 0) {
                    drawNumber(canvas, x, i);
                } else if (i % 10 == 0) {
                    drawLine(canvas, x);
                }

                if (i == 1) {
                    i = 361;
                    end = 360 + end;
                }
            }

            for (int i = bearing, end = bearing + X_MID, x = X_MID; i <= end && x <= 100; i++, x += 2) {
                if (i % 90 == 0 || i == 0) {
                    drawText(canvas, x, i);
                } else if (i % 30 == 0) {
                    drawNumber(canvas, x, i);
                } else if (i % 10 == 0) {
                    drawLine(canvas, x);
                }

                if (i == 359) {
                    i = -1;
                    end = end - 360;
                }
            }
        }

        canvas.drawRoundRect(drawRect, 10, 10, mBorderPaint);
        canvas.drawLine(50, 0, 50, 28, mBorderPaint);
    }

    /**
     * Draw line.
     * 
     * @param canvas
     *            the canvas
     * @param x
     *            the x
     */
    private void drawLine(Canvas canvas, int x) {
        mTextPaint.setARGB(225, 205, 205, 205);
        canvas.drawLine(x, 8, x, 20, mTextPaint);
    }

    /**
     * Draw ticks.
     * 
     * @param canvas
     *            the canvas
     * @param x
     *            the x
     */
    private void drawTicks(Canvas canvas, int x) {
        mTextPaint.setARGB(225, 205, 205, 205);
        canvas.drawLine(x, 4, x, 5, mTextPaint);
        canvas.drawLine(x, 23, x, 24, mTextPaint);
    }

    /**
     * Draw number.
     * 
     * @param canvas
     *            the canvas
     * @param x
     *            the x
     * @param bearing
     *            the bearing
     */
    private void drawNumber(Canvas canvas, int x, int bearing) {
        drawTicks(canvas, x);

        bearing = Math.abs(bearing);
        int startX = x;

        switch (Math.abs(bearing)) {
        case 30:
        case 60:
            startX = x - 9;
            break;
        default:
            startX = x - 13;
            break;
        }

        mTextPaint.setARGB(225, 235, 235, 235);
        canvas.drawText(Integer.toString(bearing), startX, Y_TEXT_OFFSET, mTextPaint);
    }

    /**
     * Draw text.
     * 
     * @param canvas
     *            the canvas
     * @param x
     *            the x
     * @param bearing
     *            the bearing
     */
    private void drawText(Canvas canvas, int x, int bearing) {
        drawTicks(canvas, x);

        int startX = x - 6;
        String text = "N";

        switch (Math.abs(bearing)) {
        case 90:
            text = "E";
            startX = x - 5;
            break;
        case 180:
            text = "S";
            startX = x - 4;
            break;
        case 270:
            text = "W";
            startX = x - 7;
            break;
        default:
            break;
        }

        mTextPaint.setARGB(225, 235, 235, 235);
        canvas.drawText(text, startX, Y_TEXT_OFFSET, mTextPaint);
    }

    /**
     * Register receiver.
     */
    public void registerReceiver() {
        if (sSensorManager == null) {
            sSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
            sSensorManager.registerListener(sSensorListener, sSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Unregister receiver.
     */
    public void unregisterReceiver() {
        try {
            if (sSensorManager != null) {
                sSensorManager.unregisterListener(sSensorListener);
            }
        } catch (Exception e) {

        }

        System.gc();

        // reset everything
        sSensorManager = null;
        sSensorBearing = NOT_SET;
    }
}
