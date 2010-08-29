package com.trailbehind.android.iburn.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.ZoomButton;

import com.trailbehind.android.iburn.R;

/**
 * The Class MapZoomControls.
 */
public class MapZoomControls extends LinearLayout {

    /** The zoom in. */
    private final ZoomButton mZoomIn;

    /** The zoom out. */
    private final ZoomButton mZoomOut;

    /**
     * Instantiates a new map zoom controls.
     * 
     * @param context
     *            the context
     */
    public MapZoomControls(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new map zoom controls.
     * 
     * @param context
     *            the context
     * @param attrs
     *            the attrs
     */
    public MapZoomControls(Context context, AttributeSet attrs) {
        super(context, attrs);

        setFocusable(false);
        
        LayoutInflater.from(context).inflate(R.layout.zoom_controls, this, true);
        
        mZoomIn = (ZoomButton) findViewById(R.id.zoomIn);
        mZoomOut = (ZoomButton) findViewById(R.id.zoomOut);
    }

    /**
     * Sets the on zoom in click listener.
     * 
     * @param listener
     *            the new on zoom in click listener
     */
    public void setOnZoomInClickListener(OnClickListener listener) {
        mZoomIn.setOnClickListener(listener);
    }

    /**
     * Sets the on zoom out click listener.
     * 
     * @param listener
     *            the new on zoom out click listener
     */
    public void setOnZoomOutClickListener(OnClickListener listener) {
        mZoomOut.setOnClickListener(listener);
    }

    /*
     * Sets how fast you get zoom events when the user holds down the zoom
     * in/out buttons.
     */
    /**
     * Sets the zoom speed.
     * 
     * @param speed
     *            the new zoom speed
     */
    public void setZoomSpeed(long speed) {
        mZoomIn.setZoomSpeed(speed);
        mZoomOut.setZoomSpeed(speed);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        /*
         * Consume all touch events so they don't get dispatched to the view
         * beneath this view.
         */
        return true;
    }

    /**
     * Show.
     */
    public void show() {
        fade(View.VISIBLE, 0.0f, 1.0f);
    }

    /**
     * Hide.
     */
    public void hide() {
        fade(View.GONE, 1.0f, 0.0f);
    }

    /**
     * Fade.
     * 
     * @param visibility
     *            the visibility
     * @param startAlpha
     *            the start alpha
     * @param endAlpha
     *            the end alpha
     */
    private void fade(int visibility, float startAlpha, float endAlpha) {
        AlphaAnimation anim = new AlphaAnimation(startAlpha, endAlpha);
        anim.setDuration(500);
        startAnimation(anim);
        setVisibility(visibility);
    }

    /**
     * Sets the checks if is zoom in enabled.
     * 
     * @param isEnabled
     *            the new checks if is zoom in enabled
     */
    public void setIsZoomInEnabled(boolean isEnabled) {
        mZoomIn.setEnabled(isEnabled);
    }

    /**
     * Sets the checks if is zoom out enabled.
     * 
     * @param isEnabled
     *            the new checks if is zoom out enabled
     */
    public void setIsZoomOutEnabled(boolean isEnabled) {
        mZoomOut.setEnabled(isEnabled);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.ViewGroup#hasFocus()
     */
    @Override
    public boolean hasFocus() {
        return mZoomIn.hasFocus() || mZoomOut.hasFocus();
    }
}
