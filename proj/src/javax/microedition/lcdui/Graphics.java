/*
 * Copyright (C) 2010 Trail Behind.
 * Andrew Johnson, Anna Hentzel, Abhishek Nath
 */
package javax.microedition.lcdui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Bitmap.Config;

import com.nutiteq.log.Log;

/**
 * The Class Graphics.
 */
public class Graphics {
  
  /** The Constant BASELINE. */
  public static final int BASELINE = 0x01;
  
  /** The Constant BOTTOM. */
  public static final int BOTTOM = 0x02;
  
  /** The Constant LEFT. */
  public static final int LEFT = 0x04;
  
  /** The Constant RIGHT. */
  public static final int RIGHT = 0x08;
  
  /** The Constant TOP. */
  public static final int TOP = 0x10;
  
  /** The Constant VCENTER. */
  public static final int VCENTER = 0x20;
  
  /** The Constant HCENTER. */
  public static final int HCENTER = 0x40;

  /** The Constant DOTTED. */
  public static final int DOTTED = 0x01;
  
  /** The Constant SOLID. */
  public static final int SOLID = 0x02;
  
  /** The canvas. */
  private final Canvas canvas;
  
  /** The font. */
  private Font font;
  
  /** The paint. */
  private final Paint paint;

  /**
   * Instantiates a new graphics.
   * 
   * @param wrapped the wrapped
   */
  public Graphics(final Canvas wrapped) {
    canvas = wrapped;
    font = Font.getDefaultFont();
    paint = new Paint(font.getTypefacePaint());
    paint.setAntiAlias(true);
  }

  /**
   * Draw image.
   * 
   * @param image the image
   * @param x the x
   * @param y the y
   * @param anchor the anchor
   */
  public void drawImage(final Image image, final int x, final int y, final int anchor) {
    int ax;
    int ay;
    if ((anchor & LEFT) != 0) {
      ax = x;
    } else if ((anchor & HCENTER) != 0) {
      ax = x - image.getWidth() / 2;
    } else {
      ax = x - image.getWidth();
    }
    if ((anchor & TOP) != 0) {
      ay = y;
    } else if ((anchor & VCENTER) != 0) {
      ay = y - image.getHeight() / 2;
    } else {
      ay = y - image.getHeight();
    }

    //TODO jaanus : check this. not really sure why this sometimes happens
    try {
      this.canvas.drawBitmap(image.getBitmap(), ax, ay, null);
    } catch (final NullPointerException e) {
      Log.error("NPE in G");
    }
  }

  /**
   * Draw line.
   * 
   * @param x1 the x1
   * @param y1 the y1
   * @param x2 the x2
   * @param y2 the y2
   */
  public void drawLine(final int x1, final int y1, final int x2, final int y2) {
    canvas.drawLine(x1, y1, x2, y2, paint);
  }

  /**
   * Draw rect.
   * 
   * @param x the x
   * @param y the y
   * @param width the width
   * @param height the height
   */
  public void drawRect(final int x, final int y, final int width, final int height) {
    paint.setStyle(Paint.Style.STROKE);
    canvas.drawRect(x, y, x + width, y + height, paint);
  }

  /**
   * Draw rgb.
   * 
   * @param rgbData the rgb data
   * @param offset the offset
   * @param scanlength the scanlength
   * @param x the x
   * @param y the y
   * @param width the width
   * @param height the height
   * @param processAlpha the process alpha
   */
  public void drawRGB(final int[] rgbData, final int offset, final int scanlength, final int x,
      final int y, final int width, final int height, final boolean processAlpha) {
    final Bitmap drawn = Bitmap.createBitmap(rgbData, width, height, Config.ARGB_4444);
    canvas.drawBitmap(drawn, x, y, null);
  }

  /**
   * Draw string.
   * 
   * @param str the str
   * @param x the x
   * @param y the y
   * @param anchor the anchor
   */
  public void drawString(final String str, final int x, final int y, final int anchor) {
    int paintX = x;
    int paintY = y;
    if ((anchor & TOP) != 0) {
      paintY += font.getSize();
    } else if ((anchor & BOTTOM) != 0) {
      paintY -= font.getDescent();
    }

    final int stringWidth = font.stringWidth(str);
    if ((anchor & RIGHT) != 0) {
      paintX -= stringWidth;
    } else if ((anchor & HCENTER) != 0) {
      paintX -= stringWidth / 2;
    }

    canvas.drawText(str, paintX, paintY, paint);
  }

  /**
   * Fill rect.
   * 
   * @param x the x
   * @param y the y
   * @param width the width
   * @param height the height
   */
  public void fillRect(final int x, final int y, final int width, final int height) {
    paint.setStyle(Paint.Style.FILL);
    canvas.drawRect(x, y, x + width, y + height, paint);
  }

  /**
   * Fill triangle.
   * 
   * @param x1 the x1
   * @param y1 the y1
   * @param x2 the x2
   * @param y2 the y2
   * @param x3 the x3
   * @param y3 the y3
   */
  public void fillTriangle(final int x1, final int y1, final int x2, final int y2, final int x3,
      final int y3) {
    paint.setStyle(Paint.Style.FILL);
    final Path triangle = new Path();
    triangle.moveTo(x1, y1);
    triangle.lineTo(x2, y2);
    triangle.lineTo(x3, y3);
    triangle.close();
    canvas.drawPath(triangle, paint);
  }

  /**
   * Gets the clip height.
   * 
   * @return the clip height
   */
  public int getClipHeight() {
    return canvas.getClipBounds().height();
  }

  /**
   * Gets the clip width.
   * 
   * @return the clip width
   */
  public int getClipWidth() {
    return canvas.getClipBounds().width();
  }

  /**
   * Gets the clip x.
   * 
   * @return the clip x
   */
  public int getClipX() {
    return canvas.getClipBounds().left;
  }

  /**
   * Gets the clip y.
   * 
   * @return the clip y
   */
  public int getClipY() {
    return canvas.getClipBounds().top;
  }

  /**
   * Sets the clip.
   * 
   * @param x the x
   * @param y the y
   * @param width the width
   * @param height the height
   */
  public void setClip(final int x, final int y, final int width, final int height) {
    canvas.clipRect(x, y, x + width, y + height, Region.Op.REPLACE);
  }

  /**
   * Sets the color.
   * 
   * @param rgb the new color
   */
  public void setColor(final int rgb) {
    paint.setColor(rgb);
  }

  /**
   * Sets the font.
   * 
   * @param font the new font
   */
  public void setFont(final Font font) {
    this.font = font;
    paint.setTypeface(font.getTypefacePaint().getTypeface());
    paint.setTextSize(font.getSize());
  }

  /**
   * Fill round rect.
   * 
   * @param x the x
   * @param y the y
   * @param width the width
   * @param height the height
   * @param arcWidth the arc width
   * @param arcHeight the arc height
   */
  public void fillRoundRect(final int x, final int y, final int width, final int height,
      final int arcWidth, final int arcHeight) {
    paint.setStyle(Paint.Style.FILL);
    canvas.drawRoundRect(new RectF(x, y, x + width, y + height), arcWidth, arcHeight, paint);
  }
}
