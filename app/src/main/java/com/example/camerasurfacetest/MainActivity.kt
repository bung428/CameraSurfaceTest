package com.example.camerasurfacetest

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }

        val surfaceReadyCallback = object: SurfaceHolder.Callback {
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) { }
            override fun surfaceDestroyed(p0: SurfaceHolder?) { }

            override fun surfaceCreated(p0: SurfaceHolder?) {
                startCameraSession()
            }
        }

        surfaceView.holder.addCallback(surfaceReadyCallback)

    }

    @SuppressLint("MissingPermission")
    private fun startCameraSession() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (cameraManager.cameraIdList.isEmpty()) {
            // no cameras
            return
        }
        val firstCamera = cameraManager.cameraIdList[0]
        cameraManager.openCamera(firstCamera, object: CameraDevice.StateCallback() {
            override fun onDisconnected(p0: CameraDevice) { }
            override fun onError(p0: CameraDevice, p1: Int) { }

            override fun onOpened(cameraDevice: CameraDevice) {
                // use the camera
                val cameraCharacteristics =    cameraManager.getCameraCharacteristics(cameraDevice.id)

                cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let { streamConfigurationMap ->
                    streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)?.let { yuvSizes ->
                        val previewSize = yuvSizes.last()

                        val displayRotation = windowManager.defaultDisplay.rotation
                        val swappedDimensions = areDimensionsSwapped(displayRotation, cameraCharacteristics)
                        // swap width and height if needed
                        val rotatedPreviewWidth = if (swappedDimensions) previewSize.height else previewSize.width
                        val rotatedPreviewHeight = if (swappedDimensions) previewSize.width else previewSize.height

                        surfaceView. holder .setFixedSize (rotatedPreviewWidth, rotatedPreviewHeight)
                    }

                }
            }
        }, Handler { true })


    }

    object CameraPermissionHelper {
        private const val CAMERA_PERMISSION_CODE = 0
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

        /** Check to see we have the necessary permissions for this app.  */
        fun hasCameraPermission(activity: Activity): Boolean {
            return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }

        /** Check to see we have the necessary permissions for this app, and ask for them if we don't.  */
        fun requestCameraPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_CODE)
        }

        /** Check to see if we need to show the rationale for this permission.  */
        fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION)
        }

        /** Launch Application Setting to grant permission.  */
        fun launchPermissionSettings(activity: Activity) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }

        recreate()
    }

    private fun areDimensionsSwapped(displayRotation: Int, cameraCharacteristics: CameraCharacteristics): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 90 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 0 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                // invalid display rotation
            }
        }
        return swappedDimensions
    }

}



//import android.hardware.Camera
//import android.hardware.camera2.CameraDevice
//import android.media.MediaRecorder
//import android.os.Bundle
//import android.view.SurfaceHolder
//import android.view.SurfaceView
//import android.widget.Button
//
//import androidx.appcompat.app.AppCompatActivity
//
//import java.io.IOException
//
//class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
//
//    private val camera: CameraDevice? = null
//    private var mCameraView: SurfaceView? = null
//    private var mCameraHolder: SurfaceHolder? = null
//    private var mCamera: Camera? = null
//    private val mStart: Button? = null
//    private val recording = false
//    private val mediaRecorder: MediaRecorder? = null
//
//    lateinit var appPermissions: AppPermissions
//    private val REQUEST_CODE = 1
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        appPermissions = AppPermissions(this)
//        if (appPermissions.checkPermissions()) {
//            init()
//        } else {
//            appPermissions.requestPermissions(REQUEST_CODE)
//        }
//
////        init()
//    }
//
//    private fun init() {
//
//        mCamera = Camera.open()
//        mCamera!!.setDisplayOrientation(90)
//
//        // surfaceview setting
//        mCameraHolder = mCameraView!!.holder
//        mCameraHolder!!.addCallback(this)
//        mCameraHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
//    }
//
//    // surfaceholder 와 관련된 구현 내용
//    override fun surfaceCreated(holder: SurfaceHolder) {
//        try {
//            if (mCamera == null) {
//                mCamera!!.setPreviewDisplay(holder)
//                mCamera!!.startPreview()
//            }
//        } catch (e: IOException) {
//        }
//
//    }
//
//    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//
//        // View 가 존재하지 않을 때
//        if (mCameraHolder!!.surface == null) {
//            return
//        }
//
//        // 작업을 위해 잠시 멈춘다
//        try {
//            mCamera!!.stopPreview()
//        } catch (e: Exception) {
//            // 에러가 나더라도 무시한다.
//        }
//
//        // 카메라 설정을 다시 한다.
//        val parameters = mCamera!!.parameters
//        val focusModes = parameters.supportedFocusModes
//        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
//            parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
//        }
//        mCamera!!.parameters = parameters
//
//        // View 를 재생성한다.
//        try {
//            mCamera?.setPreviewDisplay(mCameraHolder)
//            mCamera?.startPreview()
//        } catch (e: Exception) {
//        }
//
//    }
//
//    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        if (mCamera != null) {
//            mCamera?.run {
//                stopPreview()
//                release()
//            }
//            mCamera = null
//        }
//    }
//}
