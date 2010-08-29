package com.nutiteq.components;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Icon used for marking a place on map.
 */
public class PlaceIcon implements Placemark {

    /** The Constant Y_OFFSET. */
    static final private int Y_OFFSET = 15;

    /** The icon. */
    private final Image icon;

    /** The anchor x. */
    private final int anchorX;

    /** The anchor y. */
    private final int anchorY;

    /**
     * Create a icon object with default anchor point.
     * 
     * @param icon
     *            image used for place marking
     */
    public PlaceIcon(final Image icon) {
        this(icon, icon.getWidth() / 2, icon.getHeight() / 2);
    }

    /**
     * Create a icon object with custom image placement. For example for balloon
     * image, that should point to a place on map, bottom center should be
     * defined as anchor point for correct image placement.
     * 
     * @param icon
     *            place image
     * @param anchorX
     *            x coordinate on icon anchor point
     * @param anchorY
     *            y coordinate on icon anchor point
     */
    public PlaceIcon(final Image icon, final int anchorX, final int anchorY) {
        this.icon = icon;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    /**
     * Not part of public API.
     * 
     * @return the icon
     */
    public Image getIcon() {
        return icon;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.components.Placemark#getWidth(int)
     */
    public int getWidth(final int zoom) {
        return icon.getWidth();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.components.Placemark#getHeight(int)
     */
    public int getHeight(final int zoom) {
        return icon.getHeight();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.components.Placemark#getAnchorX(int)
     */
    public int getAnchorX(final int zoom) {
        return anchorX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.components.Placemark#getAnchorY(int)
     */
    public int getAnchorY(final int zoom) {
        return anchorY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nutiteq.components.Placemark#paint(javax.microedition.lcdui.Graphics,
     * int, int, int)
     */
    public void paint(final Graphics g, final int screenX, final int screenY, final int zoom) {
        if (icon.getHeight() == 57) { // ugly hack
            g.drawImage(icon, screenX, screenY - Y_OFFSET, Graphics.TOP | Graphics.LEFT);
        } else {
            g.drawImage(icon, screenX, screenY, Graphics.TOP | Graphics.LEFT);
        }
    }
}
