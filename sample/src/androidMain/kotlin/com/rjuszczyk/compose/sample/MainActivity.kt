package com.rjuszczyk.compose.sample

import App
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity: AppCompatActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                selectVideoActivityLauncher.launch(Unit)
            } else {
                AlertDialog.Builder(this).apply {
                    setMessage("You rejected permission to read external storage. The app will not be able open any videos. You can change it in app's settings.")
                    setPositiveButton("Whatever") { _, _ ->

                    }
                    setNegativeButton("SETTINGS") { _, _ ->
                        openAppSettingsScreen()
                    }
                }.show()
            }
        }

    private val selectVideoActivityLauncher =
        registerForActivityResult(object: ActivityResultContract<Unit, Uri?>() {
            override fun createIntent(context: Context, input: Unit): Intent {
                return Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                return intent?.data
            }

        }) {
            it?.let { uri ->
                onVideoUriSelected(uri)
            }?: run {
                println("Nothing selected")
            }
        }

    private val selectedVideoFileStateFlow = MutableStateFlow<String?>(null)

    private fun onVideoUriSelected(uri: Uri) {
        val file = FileUtils.getFileFromUri(this, uri)
        selectedVideoFileStateFlow.value = file?.absolutePath
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val selectedVideoFile by selectedVideoFileStateFlow.collectAsState()
            selectedVideoFile?.let {
                App(it)
            }?:run {
                Button(
                    onClick = {
                        onOpenFileChooser()
                    }
                ) {
                    Text("Select video...")
                }
            }

        }
    }

    private fun onOpenFileChooser() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED -> {
                selectVideoActivityLauncher.launch(Unit)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VIDEO) -> {
                AlertDialog.Builder(this).apply {
                    setMessage("The app needs read external storage permission in order to select videos to edit.")
                    setPositiveButton("Grant permission") { _, _ ->
                        requestPermissionLauncher.launch(
                            Manifest.permission.READ_MEDIA_VIDEO
                        )
                    }
                    setNegativeButton("No thanks") { _, _ -> }
                }.show()

            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_MEDIA_VIDEO)
            }
        }
    }

    private fun openAppSettingsScreen() {
        startActivity(
            Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
        )
    }
}