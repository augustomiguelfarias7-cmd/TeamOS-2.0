package com.teamos.launcher.apps

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import com.teamos.launcher.databinding.ActivityCameraBinding
import com.teamos.launcher.i18n.Strings
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * System camera: live preview with CameraX, take a photo or record video, switch
 * between front/back cameras, and save results to the device gallery via MediaStore.
 */
class CameraActivity : AppCompatActivity() {

    private lateinit var b: ActivityCameraBinding
    private var provider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var videoMode = false

    private val permissions = ActivityResultContracts.RequestMultiplePermissions()
    private val requestPermissions = registerForActivityResult(permissions) { result ->
        if (result.values.all { it }) startCamera() else showPermissionHint()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.permissionHint.text = Strings.get(this, "camera.permission")
        updateModeLabel()

        b.shutter.setOnClickListener { onShutter() }
        b.switchCam.setOnClickListener { toggleLens() }
        b.mode.setOnClickListener { toggleMode() }

        if (hasPermissions()) startCamera() else requestPermissions.launch(requiredPermissions())
    }

    private fun requiredPermissions(): Array<String> =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

    private fun hasPermissions(): Boolean = requiredPermissions().all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun showPermissionHint() {
        b.permissionHint.visibility = android.view.View.VISIBLE
    }

    private fun startCamera() {
        b.permissionHint.visibility = android.view.View.GONE
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            provider = future.get()
            bindUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindUseCases() {
        val provider = provider ?: return
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(b.preview.surfaceProvider)
        }
        imageCapture = ImageCapture.Builder().build()
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        runCatching {
            provider.unbindAll()
            provider.bindToLifecycle(
                this,
                selector,
                preview,
                if (videoMode) videoCapture!! else imageCapture!!
            )
        }.onFailure {
            Toast.makeText(this, Strings.get(this, "camera.error"), Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleLens() {
        if (recording != null) return
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        bindUseCases()
    }

    private fun toggleMode() {
        if (recording != null) return
        videoMode = !videoMode
        updateModeLabel()
        bindUseCases()
    }

    private fun updateModeLabel() {
        b.mode.text = Strings.get(this, if (videoMode) "camera.video" else "camera.photo")
    }

    private fun onShutter() {
        if (videoMode) toggleRecording() else takePhoto()
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return
        val name = "TeamOS_${timestamp()}"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TeamOS")
            }
        }
        val options = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ).build()

        capture.takePicture(
            options,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        this@CameraActivity,
                        Strings.get(this@CameraActivity, "camera.photo_saved"),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        Strings.get(this@CameraActivity, "camera.error"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun toggleRecording() {
        val capture = videoCapture ?: return
        val current = recording
        if (current != null) {
            current.stop()
            recording = null
            return
        }

        val name = "TeamOS_${timestamp()}"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/TeamOS")
            }
        }
        val options = MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(values).build()

        val hasAudio = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        recording = capture.output
            .prepareRecording(this, options)
            .apply { if (hasAudio) withAudioEnabled() }
            .start(ContextCompat.getMainExecutor(this)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        b.timer.visibility = android.view.View.VISIBLE
                        b.timer.text = Strings.get(this, "camera.recording")
                    }
                    is VideoRecordEvent.Finalize -> {
                        b.timer.visibility = android.view.View.GONE
                        val msg = if (event.hasError()) "camera.error" else "camera.video_saved"
                        Toast.makeText(this, Strings.get(this, msg), Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())

    override fun onStop() {
        super.onStop()
        recording?.stop()
        recording = null
    }
}
