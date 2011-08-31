package com.nutiteq.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Message;
import android.os.Process;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.listeners.MapListener;
import com.nutiteq.wrappers.Graphics;

/**
 * Simple map view handler for displaying map component on screen.
 */
public class MapView extends View implements MapListener {

  private static final int ACTION_POINTER_1_UP = 6;
  private static final int ACTION_POINTER_2_UP = 262;
  private static final int ACTION_POINTER_2_DOWN = 261;
  
  protected BasicMapComponent mapComponent;
  private Graphics g;
  private Canvas wrapped;
  private RepaintHandler repaintHandler;
  protected MapListener appMapListener;
  private float altPointerStartDist;
  private boolean dualZoom;
  protected Context mContext;

  public MapView(final Context context, final BasicMapComponent component) {
    super(context);
    setFocusable(true);
    mapComponent = component;
    appMapListener = component.getMapListener();
    component.setMapListener(this);
    repaintHandler = new RepaintHandler(this);
    mContext = context;
  }
  
  public Graphics getGraphics(){
      return g;
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    try {
        if (wrapped != canvas) {
            wrapped = canvas;
            g = new Graphics(wrapped);
            // TODO jaanus : what happens on size change
            mapComponent.resize(getWidth(), getHeight());
        }
        mapComponent.paint(g);
    }
    catch (OutOfMemoryError e) {
        oomQuit((Activity) mContext, e);
    } catch (Exception e) {
        e.printStackTrace();
    }
  }

  public boolean onTouchEvent(final MotionEvent event) {
      boolean hasMultiTouch = Integer.parseInt(Build.VERSION.SDK) >= 5;
      int nPointer = hasMultiTouch ? MotionEventWrap.getPointerCount(event) : 1;
      
      final int x = (int) event.getX();
      final int y = (int) event.getY();
      switch (event.getAction()) {
          case ACTION_POINTER_1_UP: // dual-touch finished
          case ACTION_POINTER_2_UP:
              if(hasMultiTouch){

              float altPointerStartX2 = MotionEventWrap.getX(event,1);
              float altPointerStartY2 = MotionEventWrap.getY(event,1);
              float altPointerStartDist2 = ((altPointerStartX2 - x) * (altPointerStartX2 - x))
                      + ((altPointerStartY2 - y) * (altPointerStartY2 - y));

              float moved = altPointerStartDist2 - altPointerStartDist;
              
              if (moved < -10000 && moved > -70000) {
                  mapComponent.zoomOut();
              }
              if (moved < -70000) {
                  mapComponent.zoomOut();
                  mapComponent.zoomOut();
              }

              if (moved > 10000 && moved < 70000) {
                  mapComponent.zoomIn();
              }
              if (moved > 70000) {
                  mapComponent.zoomIn();
                  mapComponent.zoomIn();
              }
              }
              break;
          case ACTION_POINTER_2_DOWN: // dual-touch started
              if(hasMultiTouch){
                  dualZoom=true;

              float altPointerStartX = MotionEventWrap.getX(event,1);
              float altPointerStartY = MotionEventWrap.getY(event,1);
              altPointerStartDist = ((altPointerStartX - x) * (altPointerStartX - x))
                      + ((altPointerStartY - y) * (altPointerStartY - y));
              }
              
              break;
          case MotionEvent.ACTION_DOWN:
              mapComponent.pointerPressed(x, y);
              break;
          case MotionEvent.ACTION_MOVE:
              if (nPointer == 1 && !dualZoom) {
                  mapComponent.pointerDragged(x, y);
              }
              break;
          case MotionEvent.ACTION_UP:
              mapComponent.pointerReleased(x, y);
              dualZoom=false; // reset 
              break;

          }
          return true;
}
  
  
  @Override
  public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    if (event.getRepeatCount() == 0) {
      mapComponent.keyPressed(keyCode);
    } else {
      mapComponent.keyRepeated(keyCode);
    }

    return isMapKey(keyCode);
  }

  private boolean isMapKey(final int keyCode) {
    return keyCode >= 19 && keyCode <= 23;
  }

  @Override
  public boolean onKeyUp(final int keyCode, final KeyEvent event) {
    mapComponent.keyReleased(keyCode);
    return isMapKey(keyCode);
  }

  public void mapClicked(final WgsPoint p) {
    if (appMapListener != null) {
      appMapListener.mapClicked(p);
    }
  }

  public void mapMoved() {
    if (appMapListener != null) {
      appMapListener.mapMoved();
    }
  }

  public void needRepaint(final boolean mapIsComplete) {
    if (appMapListener != null) {
      appMapListener.needRepaint(mapIsComplete);
    }

    if (repaintHandler != null) {
      repaintHandler.sendMessage(Message.obtain());
    }
  }

  public void clean() {
    if (repaintHandler != null) {
      repaintHandler.clean();
    }
    mapComponent = null;
    g = null;
    wrapped = null;
    repaintHandler = null;
    appMapListener = null;
    mContext = null;
  }
  
  public static void oomQuit(final Activity ctxt, OutOfMemoryError e) {
      e.printStackTrace();
      AlertDialog.Builder dialog = new AlertDialog.Builder(ctxt);
      dialog.setTitle("OutOfMemory");
      dialog.setMessage(e.getLocalizedMessage());
      dialog.setPositiveButton("Quit", new AlertDialog.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
              ctxt.finish();
              Process.killProcess(Process.myPid());
          }
      });
      dialog.show();
  }
}