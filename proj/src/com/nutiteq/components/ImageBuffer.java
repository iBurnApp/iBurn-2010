package com.nutiteq.components;

import com.nutiteq.wrappers.Graphics;
import com.nutiteq.wrappers.Image;

public class ImageBuffer extends Object {

    private static ImageBuffer INSTANCE;
    private Image[] bufferImages;
    private Graphics[] bufferGraphics;
    private int front;

    public static ImageBuffer getInstance(final int numberOfImages, final int imageWidth, final int imageHeight) {
        if (INSTANCE == null) {
            INSTANCE = new ImageBuffer(numberOfImages, imageWidth, imageHeight);
        }
        return INSTANCE;
    }

    private ImageBuffer(final int numberOfImages, final int imageWidth, final int imageHeight) {
        bufferImages = new Image[numberOfImages];
        bufferGraphics = new Graphics[numberOfImages];
        for (int i = 0; i < bufferImages.length; i++) {
            bufferImages[i] = Image.createImage(imageWidth, imageHeight);
            bufferGraphics[i] = bufferImages[i].getGraphics();
        }
    }

    @Override
    protected void finalize() {
        clean();
    }

    public Image getFrontImage() {
        return bufferImages[front];
    }

    public Graphics getBackGraphics() {
        return bufferGraphics[nextValue(front)];
    }

    public Graphics getFrontGraphics() {
        return bufferGraphics[front];
    }

    public void flip() {
        front = nextValue(front);
    }

    private int nextValue(final int bufferIndex) {
        final int result = bufferIndex + 1;
        return result < bufferImages.length ? result : 0;
    }

    public void resize(final int newWidth, final int newHeight) {
        for (int i = 0; i < bufferImages.length; i++) {
            if (bufferImages[i].getBitmap() != null) {
                bufferImages[i].getBitmap().recycle();
            }
            // bufferImages[i] = Utils.resizeImageAndCopyPrevious(newWidth,
            // newHeight,
            // bufferImages[i]);
            bufferImages[i] = Image.createImage(newWidth, newHeight);
            bufferGraphics[i] = bufferImages[i].getGraphics();
        }
    }

    public void clean() {
        if (bufferImages != null) {
            for (int i = 0; i < bufferImages.length; i++) {
                if (bufferImages[i] != null) {
                    bufferImages[i].getBitmap().recycle();
                }
            }
            bufferImages = null;
        }
        bufferGraphics = null;
        INSTANCE = null;
        System.gc();
    }
}