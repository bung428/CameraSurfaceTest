package com.example.camerasurfacetest

import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED

import android.Manifest.permission
import android.app.Activity

/**
 * Utility for interacting with the permission system.
 */
class AppPermissions(private val activity: Activity) {

    val TAG = "AppPermission"

    /** Returns true if the app has all needed permissions.  */
    fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(activity, permission.CAMERA)
        val writeExternalStoragePermission =
            ContextCompat.checkSelfPermission(activity, permission.WRITE_EXTERNAL_STORAGE)
        return cameraPermission == PERMISSION_GRANTED && writeExternalStoragePermission == PERMISSION_GRANTED
    }

    /** Request permissions from the user.  */
    fun requestPermissions(requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(
                arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE),
                requestCode
            )
        }
    }

    /**
     * Call this from the activity's onRequestPermissionResult for the previously
     * given requestCode. Returns true if the permissions have been granted.
     * Terminates the activity and shows usage if the permissions were not granted.
     */
    fun onRequestPermissionResult(grantResults: IntArray): Boolean {
        val hasPermissions = (grantResults.size > 1
                && grantResults[0] == PERMISSION_GRANTED
                && grantResults[1] == PERMISSION_GRANTED)
        if (!hasPermissions) {
            Log.d(TAG, "Camera and Storage permissions are required to use the app")
            activity.finish()
        }
        return hasPermissions
    }
}