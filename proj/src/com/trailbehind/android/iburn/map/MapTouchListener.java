package com.trailbehind.android.iburn.map;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnTouchListener;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.components.ZoomRange;
import com.trailbehind.android.iburn.R;
import com.trailbehind.android.iburn.util.UIUtils;

/**
 * The listener interface for receiving mapTouch events. The class that is
 * interested in processing a mapTouch event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addMapTouchListener<code> method. When
 * the mapTouch event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see MapTouchEvent
 */
class MapTouchListener implements OnTouchListener {

    /** The Constant ACTION_MASK. */
    static final private int ACTION_MASK = 255;

    /** The Constant ACTION_POINTER_DOWN. */
    static final private int ACTION_POINTER_DOWN = 5;

    /** The Constant ACTION_POINTER_UP. */
    static final private int ACTION_POINTER_UP = 6;

    /** The Constant MODE_DEFAULT. */
    static final private int MODE_DEFAULT = 0;

    /** The Constant MODE_TAP_ONE. */
    static final private int MODE_TAP_ONE = 1;

    /** The Constant MODE_TAP_DOUBLE. */
    static final private int MODE_TAP_DOUBLE = 2;

    /** The Constant MODE_TAP_TRIPLE. */
    static final private int MODE_TAP_TRIPLE = 3;

    /** The Constant MODE_MULTITOUCH_DEFAULT. */
    static final private int MODE_MULTITOUCH_DEFAULT = 0;

    /** The Constant MODE_MULTITOUCH_DRAG. */
    static final private int MODE_MULTITOUCH_DRAG = 1;

    /** The Constant MODE_MULTITOUCH_ZOOM. */
    static final private int MODE_MULTITOUCH_ZOOM = 2;

    /** The Constant TAP_TIMEOUT. */
    static final private int TAP_TIMEOUT = 350;

    /** The Constant ZOOM_SENSITIVITY. */
    private static final double ZOOM_SENSITIVITY = 1.3;

    /** The Constant ZOOM_LOG_BASE_INV. */
    private static final double ZOOM_LOG_BASE_INV = 1.0 / Math.log(2.0 / ZOOM_SENSITIVITY);

    /** The zoom timeout. */
    private long mZoomTimeout = ViewConfiguration.getZoomControlsTimeout();

    /** The mMode. */
    private int mMode = MODE_DEFAULT;

    /** The touch handler. */
    private TouchHandler mTouchHandler;

    /** The map activity. */
    private MapActivity mMapActivity;

    /** The last x. */
    private float mLastX;

    /** The last y. */
    private float mLastY;

    /**
     * Instantiates a new map touch listener.
     * 
     * @param mapActivity
     *            the map activity
     */
    MapTouchListener(MapActivity mapActivity) {
        mMapActivity = mapActivity;
        mTouchHandler = new TouchHandler();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnTouchListener#onTouch(android.view.View,
     * android.view.MotionEvent)
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final MapActivity mapActivity = mMapActivity;
        final TouchHandler touchHandler = mTouchHandler;

        // Handle touch events here...
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
            mLastX = event.getX();
            mLastY = event.getY();

            mapActivity.setZoomControlsVisible(true);
        }
            break;
        case MotionEvent.ACTION_UP: {
            touchHandler.removeMessages(TouchHandler.MESSAGE_TAP_TIMEOUT);
            touchHandler.removeMessages(TouchHandler.MESSAGE_ZOOM_TIMEOUT);

            switch (mMode) {
            case MODE_DEFAULT:
                mMode = MODE_TAP_ONE;
                break;
            case MODE_TAP_ONE:
                mMode = MODE_TAP_DOUBLE;
                break;
            case MODE_TAP_DOUBLE:
                mMode = MODE_TAP_TRIPLE;
                break;
            case MODE_TAP_TRIPLE:
                mMode = MODE_DEFAULT;
                break;
            default:
                break;
            }
            touchHandler.sendMessageDelayed(touchHandler.obtainMessage(TouchHandler.MESSAGE_TAP_TIMEOUT), TAP_TIMEOUT);
            touchHandler
                    .sendMessageDelayed(touchHandler.obtainMessage(TouchHandler.MESSAGE_ZOOM_TIMEOUT), mZoomTimeout);
        }
            break;
        case MotionEvent.ACTION_MOVE:
            float x = event.getX();
            float y = event.getY();

            float diffX = Math.abs(x - mLastX);
            float diffY = Math.abs(y - mLastY);

            if (diffX > 2f || diffY > 2f) {
                mLastX = event.getX();
                mLastY = event.getY();
            }
            break;
        case MotionEvent.ACTION_CANCEL: {
            mapActivity.setZoomControlsVisible(false);
            touchHandler.removeMessages(TouchHandler.MESSAGE_ZOOM_TIMEOUT);
            break;
        }
        default:
            break;
        }

        return false;
    }

    /**
     * Map clicked.
     * 
     * @param point
     *            the point
     */
    public void mapClicked(WgsPoint point) {
        mTouchHandler.setLastClickedPoint(point);
    }

    /**
     * Map moved.
     */
    public void mapMoved() {
        mTouchHandler.setLastClickedPoint(null);
    }

    // inner classes ---------

    /**
     * The Class TouchHandler.
     */
    private class TouchHandler extends Handler {

        /** The Constant MESSAGE_TAP_TIMEOUT. */
        private static final int MESSAGE_TAP_TIMEOUT = 0;

        /** The Constant MESSAGE_ZOOM_TIMEOUT. */
        private static final int MESSAGE_ZOOM_TIMEOUT = 1;

        /** The last clicked point. */
        private WgsPoint mLastClickedPoint;

        /*
         * (non-Javadoc)
         * 
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            final MapActivity mapActivity = mMapActivity;
            final BasicMapComponent mapComponent = mapActivity.getMapComponent();

            switch (msg.what) {
            case MESSAGE_TAP_TIMEOUT:
                if (mapComponent != null && mLastClickedPoint != null) {
                    switch (mMode) {
                    case MODE_TAP_DOUBLE: {
                        final ZoomRange zoomRange = mapComponent.getZoomRange();
                        final int zoomLevel = mapComponent.getZoom();
                        if (zoomRange.getMaxZoom() != zoomLevel) {
                            mapActivity.zoomInToPoint(mLastClickedPoint, 1);
                        } else {
                            UIUtils.showDefaultToast(mapActivity.getBaseContext(), R.string.toast_max_zoom, false);
                        }
                        break;
                    }
                    case MODE_TAP_TRIPLE: {
                        final ZoomRange zoomRange = mapComponent.getZoomRange();
                        final int zoomLevel = mapComponent.getZoom();
                        if (zoomRange.getMinZoom() != zoomLevel) {
                            mapActivity.zoomOutToPoint(mLastClickedPoint, 1);
                        } else {
                            UIUtils.showDefaultToast(mapActivity.getBaseContext(), R.string.toast_min_zoom, false);
                        }
                        break;
                    }
                    default:
                        break;
                    }
                }
                break;
            case MESSAGE_ZOOM_TIMEOUT:
                mapActivity.setZoomControlsVisible(false);
                removeMessages(MESSAGE_ZOOM_TIMEOUT);
                break;
            default:
                break;
            }

            mMode = MODE_DEFAULT;
        }

        /**
         * Sets the last clicked point.
         * 
         * @param lastClickedPoint
         *            the new last clicked point
         */
        public void setLastClickedPoint(WgsPoint lastClickedPoint) {
            this.mLastClickedPoint = lastClickedPoint;
        }
    }
}
