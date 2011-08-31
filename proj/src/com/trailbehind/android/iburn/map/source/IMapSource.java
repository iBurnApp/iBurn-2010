/*
 * Copyright (C) 2010 Trail Behind.
 * Andrew Johnson, Anna Hentzel, Abhishek Nath
 */
package com.trailbehind.android.iburn.map.source;

import java.io.IOException;

import com.nutiteq.maps.GeoMap;
import com.nutiteq.maps.UnstreamedMap;

/**
 * The Interface IMapSource.
 */
public interface IMapSource extends GeoMap, UnstreamedMap {

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.maps.UnstreamedMap#buildPath(int, int, int)
     */
    public abstract String buildPath(final int mapX, final int mapY, final int zoom);

    /**
     * Gets the source type.
     * 
     * @return the source type
     */
    public abstract int getSourceId();

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public abstract String getName();

    /**
     * Gets the base url.
     * 
     * @return the base url
     */
    public abstract String getBaseURL();

    /**
     * Gets the tile file size.
     * 
     * @return the tile file size
     */
    public abstract int getTileFileSize();

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.maps.BaseMap#getMinZoom()
     */
    public abstract int getMinZoom();

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.maps.BaseMap#getMaxZoom()
     */
    public abstract int getMaxZoom();

    /**
     * Gets the tile matrix.
     * 
     * @return the tile matrix
     */
    public abstract int getTileMatrix();

    /*
     * (non-Javadoc)
     * 
     * @see com.nutiteq.maps.LocalMap#getTileImageData(int, int, int)
     */
    public abstract byte[] getTileImageData(int arg0, int arg1, int arg2) throws IOException;

    /**
     * Gets the display name.
     * 
     * @return the display name
     */
    public abstract String getDisplayName();

    /**
     * Gets the icon resource id.
     * 
     * @return the icon resource id
     */
    public abstract int getIconResourceId();

}