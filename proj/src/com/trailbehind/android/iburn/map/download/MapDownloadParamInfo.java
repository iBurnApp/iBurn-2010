/*
 * Copyright (C) 2010 Trail Behind.
 * Andrew Johnson, Anna Hentzel, Abhishek Nath
 */
package com.trailbehind.android.iburn.map.download;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The Class MapDownloadInfo.
 */
public class MapDownloadParamInfo implements Parcelable {

    // properties ---------

    /** The id. */
    public int mId;

    /** The download id. */
    public int mDownloadId;

    /** The start x. */
    public int mStartX;

    /** The start y. */
    public int mStartY;

    /** The end x. */
    public int mEndX;

    /** The end y. */
    public int mEndY;

    /** The zoom. */
    public int mZoom;

    /** The total file count. */
    public int mTotalFileCount;

    /** The created time. */
    public long mCreatedTime;

    /** The modified time. */
    public long mModifiedTime;

    /** The Constant CREATOR. */
    public static final Parcelable.Creator<MapDownloadParamInfo> CREATOR = new Parcelable.Creator<MapDownloadParamInfo>() {

        @Override
        public MapDownloadParamInfo createFromParcel(Parcel source) {
            return new MapDownloadParamInfo(source);
        }

        @Override
        public MapDownloadParamInfo[] newArray(int size) {
            return new MapDownloadParamInfo[size];
        }
    };

    // public constructors ---------

    /**
     * Instantiates a new map download param info.
     * 
     * @param id the id
     * @param downloadId the download id
     * @param startX the start x
     * @param startY the start y
     * @param endX the end x
     * @param endY the end y
     * @param zoom the zoom
     * @param totalFileCount the total file count
     * @param thread1DownloadCount the thread1 download count
     * @param thread2DownloadCount the thread2 download count
     * @param createdTime the created time
     * @param modifiedTime the modified time
     */
    public MapDownloadParamInfo(int id, int downloadId, int startX, int startY, int endX, int endY, int zoom,
            int totalFileCount, long createdTime, long modifiedTime) {
        mId = id;
        mDownloadId = downloadId;

        mEndX = endX;
        mEndY = endY;
        mStartX = startX;
        mStartY = startY;
        mZoom = zoom;

        mTotalFileCount = totalFileCount;

        mCreatedTime = createdTime;
        mModifiedTime = modifiedTime;
    }

    /**
     * Instantiates a new map download param info.
     * 
     * @param startX the start x
     * @param startY the start y
     * @param endX the end x
     * @param endY the end y
     * @param zoom the zoom
     * @param totalFileCount the total file count
     */
    public MapDownloadParamInfo(int startX, int startY, int endX, int endY, int zoom, int totalFileCount) {
        mEndX = endX;
        mEndY = endY;
        mStartX = startX;
        mStartY = startY;
        mZoom = zoom;

        mTotalFileCount = totalFileCount;
    }

    /**
     * Instantiates a new map download info.
     * 
     * @param in the in
     */
    public MapDownloadParamInfo(Parcel in) {
        readFromParcel(in);
    }

    // private methods ---------

    /**
     * Read from parcel.
     * 
     * @param in the in
     */
    private void readFromParcel(Parcel in) {
        mId = in.readInt();
        mDownloadId = in.readInt();

        mStartX = in.readInt();
        mStartY = in.readInt();
        mEndX = in.readInt();
        mEndY = in.readInt();
        mZoom = in.readInt();

        mTotalFileCount = in.readInt();

        mCreatedTime = in.readLong();
        mModifiedTime = in.readLong();
    }

    // overriding methods ---------

    /*
     * (non-Javadoc)
     * 
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeInt(mDownloadId);

        dest.writeInt(mStartX);
        dest.writeInt(mStartY);
        dest.writeInt(mEndX);
        dest.writeInt(mEndY);
        dest.writeInt(mZoom);

        dest.writeInt(mTotalFileCount);

        dest.writeLong(mCreatedTime);
        dest.writeLong(mModifiedTime);
    }
}
