/*
 * Copyright (C) 2010 Trail Behind.
 * Andrew Johnson, Anna Hentzel, Abhishek Nath
 */
package javax.microedition.lcdui;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;

/**
 * The Class Image.
 */
public class Image {
  
  /** The bitmap. */
  private final Bitmap bitmap;

  /**
   * Instantiates a new image.
   * 
   * @param bitmap the bitmap
   */
  private Image(final Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  /**
   * Creates the image.
   * 
   * @param imageData the image data
   * @param imageOffset the image offset
   * @param imageLength the image length
   * 
   * @return the image
   */
  public static Image createImage(final byte[] imageData, final int imageOffset,
      final int imageLength) {
    final Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, imageOffset, imageLength);
    return new Image(bitmap);
  }

  /**
   * Creates the image.
   * 
   * @param image the image
   * @param x the x
   * @param y the y
   * @param width the width
   * @param height the height
   * @param transform the transform
   * 
   * @return the image
   */
  public static Image createImage(final Image image, final int x, final int y, final int width,
      final int height, final int transform) {
    //TODO jaanus : transform
    return new Image(Bitmap.createBitmap(image.bitmap, x, y, width, height));
  }

  /**
   * Creates the image.
   * 
   * @param width the width
   * @param height the height
   * 
   * @return the image
   */
  public static Image createImage(final int width, final int height) {
    final Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_4444);
    return new Image(bitmap);
  }

  /**
   * Creates the image.
   * 
   * @param name the name
   * 
   * @return the image
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static Image createImage(final String name) throws java.io.IOException {
    return createImage(Image.class.getResourceAsStream(name));
  }

  /**
   * Creates the image.
   * 
   * @param stream the stream
   * 
   * @return the image
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static Image createImage(final InputStream stream) throws IOException {
    final Bitmap bitmap = BitmapFactory.decodeStream(stream);
    return new Image(bitmap);
  }

  /**
   * Gets the graphics.
   * 
   * @return the graphics
   */
  public Graphics getGraphics() {
    return new Graphics(new android.graphics.Canvas(bitmap));
  }

  /**
   * Gets the height.
   * 
   * @return the height
   */
  public int getHeight() {
    return bitmap.getHeight();
  }

  /**
   * Gets the width.
   * 
   * @return the width
   */
  public int getWidth() {
    return bitmap.getWidth();
  }

  /**
   * Gets the rGB.
   * 
   * @param rgbData the rgb data
   * @param offset the offset
   * @param scanlength the scanlength
   * @param x the x
   * @param y the y
   * @param width the width
   * @param height the height
   * 
   * @return the rGB
   */
  public void getRGB(final int[] rgbData, final int offset, final int scanlength, final int x,
      final int y, final int width, final int height) {
    bitmap.getPixels(rgbData, offset, scanlength, x, y, width, height);
  }

  /**
   * Gets the bitmap.
   * 
   * @return the bitmap
   */
  public Bitmap getBitmap() {
    return bitmap;
  }
}
