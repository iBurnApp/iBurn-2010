package com.trailbehind.android.iburn.util;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.trailbehind.android.iburn.R;

/**
 * The Class Utils.
 */
public class Utils implements IConstants {

    /**
     * Delete file.
     * 
     * @param fileToDelete
     *            the file to delete
     */
    static public void deleteFile(File fileToDelete) {
        final String path = fileToDelete.getAbsolutePath();
        final String[] cmd = new String[] { "rm", "-R", path };
        final Runtime r = Runtime.getRuntime();
        try {
            r.exec(cmd);
        } catch (IOException e) {
            Log.e(TAG, "error deleting file", e);
        }
        if (fileToDelete != null) {
            fileToDelete = new File(path);
            if (fileToDelete.exists()) {
                if (fileToDelete.isDirectory()) {
                    final File[] children = fileToDelete.listFiles();
                    for (File file : children) {
                        deleteFile(file);
                    }
                }
                fileToDelete.delete();
            }
        }
    }

    /**
     * Delete sub folders.
     * 
     * @param parentDir
     *            the parent dir
     */
    static public void deleteSubFolders(File parentDir) {
        if (parentDir != null && parentDir.isDirectory()) {
            final File[] children = parentDir.listFiles();
            for (File file : children) {
                if (file.isDirectory()) {
                    deleteFile(file);
                }
            }
        }
    }

    /**
     * Clean file name.
     * 
     * @param name
     *            the name
     * 
     * @return the string
     */
    static public String cleanFileName(String name) {
        name = name.replace(' ', '_');
        name = name.replace('-', '_');
        name = name.replace(':', '_');
        name = name.replace(';', '_');
        name = name.replace('\'', '_');
        name = name.replace('\"', '_');
        name = name.replace('\\', '_');
        name = name.replace('/', '_');
        return name;
    }

    /**
     * Returns whether the network is available.
     * 
     * @param context
     *            the context
     * 
     * @return true, if checks if is network available
     */
    static public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(TAG, "couldn't get connectivity manager");
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        Log.d(TAG, "network is available");
                        return true;
                    }
                }
            }
        }
        Log.d(TAG, "network is not available");
        return false;
    }

    /**
     * Checks if is sD card available.
     * 
     * @return true, if is sD card available
     */
    static public boolean isSDCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Gets the about text.
     * 
     * @param context
     *            the context
     * 
     * @return the about text
     */
    static public String getAboutText(Context context) {
        String versionName = "";
        int versionCode = 0;

        try {
            final PackageManager packageManager = context.getPackageManager();
            final PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
            versionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "error", e);
        }

        final StringBuilder buff = new StringBuilder();

        buff.append(context.getString(R.string.version));
        buff.append(":\t\t");
        buff.append(versionName);
        buff.append(" (");
        buff.append(versionCode);
        buff.append(")");
        buff.append(context.getString(R.string.website));
        buff.append(context.getString(R.string.team));
        buff.append(context.getString(R.string.about));
        buff.append(context.getString(R.string.whats_new));

        return buff.toString();
    }
}
