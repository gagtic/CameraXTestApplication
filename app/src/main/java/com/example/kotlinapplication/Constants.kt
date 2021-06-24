package com.example.kotlinapplication

import android.Manifest

object Constants {

    const val TAG = "cameraX"
    const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"
    const val REQUEST_CODE_PERMISSION_CAMERA = 123
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA);

}