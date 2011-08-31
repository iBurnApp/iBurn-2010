package com.trailbehind.android.iburn.view;

import java.lang.reflect.Field;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;

import com.mgmaps.utils.Queue;
import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;
import com.nutiteq.android.RepaintHandler;
import com.nutiteq.core.MappingCore;
import com.nutiteq.task.TasksRunnerImpl;
import com.trailbehind.android.iburn.util.IConstants;

/**
 * The Class CustomMapView.
 */
public class CustomMapView extends MapView implements IConstants {

    /** The view cleaned. */
    private boolean viewCleaned = false;

    /** The component. */
    private BasicMapComponent mComponent;

    private Field mExecutionQueueField;

    /**
     * Instantiates a new custom map view.
     * 
     * @param context
     *            the context
     * @param component
     *            the component
     */
    public CustomMapView(Context context, BasicMapComponent component) {
        super(context, component);
        mComponent = component;

        try {
            final Field field = MapView.class.getDeclaredField("repaintHandler");
            field.setAccessible(true);
            field.set(this, new CustomRepaintHandler(this));
        } catch (Exception e) {
            Log.e(TAG, "unable to set custom repaint handler. ", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.android.MapView#clean()
     */
    @Override
    public void clean() {
        viewCleaned = true;
        mComponent = null;
        super.clean();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.android.MapView#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            if (!viewCleaned) {
                super.onDraw(canvas);
            }
            final ViewGroup parent = ((ViewGroup) getParent());
            if (parent != null) {
                parent.invalidate();
            }
        } catch (Exception e) {
            Log.e(TAG, "unable to draw map. ", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onSizeChanged(int, int, int, int)
     */
    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != 0 && h != 0 && mComponent != null) {
            if (oldw != w && oldh != h) {
                mComponent.resize(w, h);
            }
        }
    }

    /**
     * Need repaint.
     * 
     * @param mapIsComplete
     *            the map is complete
     * @param clearQueue
     *            the clear queue
     */
    public void needRepaint(boolean mapIsComplete, boolean clearQueue) {
        try {
            final TasksRunnerImpl tr = (TasksRunnerImpl) MappingCore.getInstance().getTasksRunner();
            if (mExecutionQueueField == null) {
                mExecutionQueueField = TasksRunnerImpl.class.getDeclaredField("executionQueue");
                mExecutionQueueField.setAccessible(true);
            }

            Queue q = (Queue) mExecutionQueueField.get(tr);
            q.clear();
        } catch (Exception e) {
            Log.e(TAG, "error clearing task queue", e);
        }
        super.needRepaint(mapIsComplete);
    }

    /**
     * The Class CustomRepaintHandler.
     */
    private class CustomRepaintHandler extends RepaintHandler {

        /**
         * Instantiates a new custom repaint handler.
         * 
         * @param view
         *            the view
         */
        public CustomRepaintHandler(MapView view) {
            super(view);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.nutiteq.android.RepaintHandler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message message) {
            if (!viewCleaned) {
                super.handleMessage(message);
            }
        }
    }
}
