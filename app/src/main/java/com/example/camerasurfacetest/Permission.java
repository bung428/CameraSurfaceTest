package com.example.camerasurfacetest;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


import android.Manifest.permission;

/**
 * Utility for interacting with the permission system.
 */
public final class Permission {

    private final Activity activity;

    public Permission(Activity activity) {
        this.activity = activity;
    }

    /**
     * Returns true if the app has all needed permissions.
     */
    public boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(activity, permission.CAMERA);
        int writeExternalStoragePermission =
                ContextCompat.checkSelfPermission(activity, permission.WRITE_EXTERNAL_STORAGE);
        return cameraPermission == PERMISSION_GRANTED
                && writeExternalStoragePermission == PERMISSION_GRANTED;
    }

    /**
     * Request permissions from the user.
     */
    public void requestPermissions(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(
                    new String[]{permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
        }
    }

    /**
     * Call this from the activity's onRequestPermissionResult for the previously
     * given requestCode. Returns true if the permissions have been granted.
     * Terminates the activity and shows usage if the permissions were not granted.
     */
    public boolean onRequestPermissionResult(int[] grantResults) {
        boolean hasPermissions = grantResults.length > 1
                && grantResults[0] == PERMISSION_GRANTED
                && grantResults[1] == PERMISSION_GRANTED;
        if (!hasPermissions) {
            Log.d("asd", "Camera and Storage permissions are required to use the app");
            activity.finish();
        }
        return hasPermissions;
    }
}