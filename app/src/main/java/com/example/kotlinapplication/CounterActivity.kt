package com.example.kotlinapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kotlinapplication.databinding.ActivityMainBinding
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

private lateinit var binding: ActivityMainBinding


class MainActivity : AppCompatActivity() {

    var num: Int = 0;
    val CALL_REQUEST_CODE: Int = 12345;
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File

    private companion object

    val TAG = "CounterActivity"
    private val intent = Intent(Intent.ACTION_CALL);

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        outputDirectory = getOutputDirectory()
        setListeners();

        initializeCamera();
    }


    /**
     * get output directory to save the file
     *
     * @return File object for the output directory
     */
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let { mFile ->
            File(mFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    /**
     * Check Permissions and start the camera
     *
     */
    private fun initializeCamera() {
        if (allPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSION_CAMERA
            )
        }
    }

    /**
     * Take photo when the button is pressed
     *
     */
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                Constants.FILE_NAME_FORMAT,
                Locale.getDefault()
            ).format(System.currentTimeMillis()) + ".jpeg"
        )
        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val saveUri = Uri.fromFile(photoFile)
                    val msg = "Photo Saved"

                    Toast.makeText(
                        this@MainActivity,
                        "$msg\n$saveUri",
                        Toast.LENGTH_LONG
                    ).show();
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@MainActivity,
                        "${exception.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
        )

    }

    /**
     * Check if all the required permissions are granted
     *
     */
    private fun allPermissionGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Set all the listeners
     */
    private fun setListeners() {
        binding.btnTakePhoto.setOnClickListener {
            takePhoto();
        }
//        binding.btnMakeCall.setOnClickListener {
//            intent.setData(Uri.parse("tel:" + 12345))
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.CALL_PHONE
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.CALL_PHONE),
//                    CALL_REQUEST_CODE
//                );
//            } else {
//                startActivity(intent);
//            }
//        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALL_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user")
                    startActivity(intent);
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                }
            }
            Constants.REQUEST_CODE_PERMISSION_CAMERA -> {
                if (allPermissionGranted()) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show()
                    finish();
                }
            }
        }
    }

    /**
     * Start the camera
     *
     */

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { mPreview ->
                    mPreview.setSurfaceProvider(
                        binding.previewView.surfaceProvider
                    )
                }
            imageCapture = ImageCapture.Builder()
                .build();
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.i(TAG, "startCamera: ${e.localizedMessage}")
            }
        }, ContextCompat.getMainExecutor(this))
    }
}