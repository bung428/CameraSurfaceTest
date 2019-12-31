package com.example.camerasurfacetest

import android.annotation.SuppressLint
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.app.Activity
import android.content.Context
import android.util.Size
import android.view.Surface
import java.util.*


class Camera2APIs(private val mInterface: Camera2Interface) {
    private var mCameraSize: Size? = null

    private var mCaptureSession: CameraCaptureSession? = null
    private var mCameraDevice: CameraDevice? = null
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null

    private val mCameraDeviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            mInterface.onCameraDeviceOpened(camera, mCameraSize)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
        }
    }

    private val mCaptureSessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            try {
                mCaptureSession = cameraCaptureSession
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                cameraCaptureSession.setRepeatingRequest(
                    mPreviewRequestBuilder!!.build(),
                    mCaptureCallback,
                    null
                )
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }

        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {

        }
    }

    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            super.onCaptureProgressed(session, request, partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
        }
    }

    interface Camera2Interface {
        fun onCameraDeviceOpened(cameraDevice: CameraDevice, cameraSize: Size?)
        fun onCameraDeviceOpened(cameraDevice: CameraDevice, cameraSize: Any?)
    }

    fun CameraManager_1(activity: Activity): CameraManager {
        return activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    fun CameraCharacteristics_2(cameraManager: CameraManager): String? {
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    val map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sizes = map!!.getOutputSizes(SurfaceTexture::class.java)

                    mCameraSize = sizes[0]
                    for (size in sizes) {
                        if (size.width > mCameraSize!!.getWidth()) {
                            mCameraSize = size
                        }
                    }

                    return cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        return null
    }

    @SuppressLint("MissingPermission")
    fun CameraDevice_3(cameraManager: CameraManager, cameraId: String) {
        try {
            cameraManager.openCamera(cameraId, mCameraDeviceStateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    fun CaptureSession_4(cameraDevice: CameraDevice, surface: Surface) {
        try {
            cameraDevice.createCaptureSession(
                Collections.singletonList(surface),
                mCaptureSessionCallback,
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    fun CaptureRequest_5(cameraDevice: CameraDevice, surface: Surface) {
        try {
            mPreviewRequestBuilder =
                cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder!!.addTarget(surface)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    fun closeCamera() {
        if (null != mCaptureSession) {
            mCaptureSession!!.close()
            mCaptureSession = null
        }

        if (null != mCameraDevice) {
            mCameraDevice!!.close()
            mCameraDevice = null
        }
    }
}