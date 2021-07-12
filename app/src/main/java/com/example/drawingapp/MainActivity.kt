package com.example.drawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawingapp.databinding.ActivityMainBinding
import com.example.drawingapp.databinding.DialogBrushSizeBinding
import com.example.drawingapp.databinding.DialogColorPickerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingDialogBrushSize: DialogBrushSizeBinding
    private lateinit var brushDialog: Dialog
    private lateinit var bindingDialogColorPicker: DialogColorPickerBinding
    private lateinit var colorPickerDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

        bindingDialogBrushSize = DialogBrushSizeBinding.inflate(layoutInflater)
        brushDialog = Dialog(this)
        view = bindingDialogBrushSize.root
        brushDialog.setContentView(view)

        bindingDialogColorPicker = DialogColorPickerBinding.inflate(layoutInflater)
        colorPickerDialog = Dialog(this)
        view = bindingDialogColorPicker.root
        colorPickerDialog.setContentView(view)


        binding.drawingView.setBrushSize(20F)

        binding.ibBrush.setOnClickListener {
            showBrushSizeDialog()
        }

        binding.ibColorPicker.setOnClickListener {
            showColorPickerDialog()
        }

        binding.ibImagePicker.setOnClickListener {
            if (isReadStorageAllowed()) {
                val pickPhotoIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                resultLauncher.launch(pickPhotoIntent)

            } else {
                requestStoragePermission()
            }
        }

        binding.ibUndo.setOnClickListener {
            binding.drawingView.onClickUndo()
        }

        binding.ibSave.setOnClickListener {
            if (isReadStorageAllowed()) {
                BitmapAsyncTask(getBitmapFromView(binding.flDrawingViewContainer)).execute()
            } else {
                requestStoragePermission()
            }
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    if (result.data!!.data != null) {
                        binding.ivBackground.visibility = View.VISIBLE
                        binding.ivBackground.setImageURI(result.data!!.data)
                    } else {
                        Toast.makeText(this, "Error selecting an image", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


    private fun showBrushSizeDialog() {

        bindingDialogBrushSize.ibSmallBrush.setOnClickListener {
            binding.drawingView.setBrushSize(10F)
            brushDialog.dismiss()
        }

        bindingDialogBrushSize.ibMediumBrush.setOnClickListener {
            binding.drawingView.setBrushSize(20F)
            brushDialog.dismiss()
        }

        bindingDialogBrushSize.ibLargeBrush.setOnClickListener {
            binding.drawingView.setBrushSize(30F)
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    private fun showColorPickerDialog() {
        colorPickerDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        colorPickerDialog.show()

        val color = binding.drawingView.getColor()

        bindingDialogColorPicker.ivColor.setBackgroundColor(color)
        var red = Color.red(color)
        var green = Color.green(color)
        var blue = Color.blue(color)

        bindingDialogColorPicker.sbRed.progress = red
        bindingDialogColorPicker.sbGreen.progress = green
        bindingDialogColorPicker.sbBlue.progress = blue

        bindingDialogColorPicker.sbRed.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                red = bindingDialogColorPicker.sbRed.progress
                bindingDialogColorPicker.ivColor.setBackgroundColor(Color.rgb(red, green, blue))
            }
        })

        bindingDialogColorPicker.sbGreen.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                green = bindingDialogColorPicker.sbGreen.progress
                bindingDialogColorPicker.ivColor.setBackgroundColor(Color.rgb(red, green, blue))
            }
        })

        bindingDialogColorPicker.sbBlue.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                blue = bindingDialogColorPicker.sbBlue.progress
                bindingDialogColorPicker.ivColor.setBackgroundColor(Color.rgb(red, green, blue))
            }
        })

        bindingDialogColorPicker.btnSelectColor.setOnClickListener {
            binding.drawingView.setColor(Color.rgb(red, green, blue))
            colorPickerDialog.dismiss()
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        ) {
            Toast.makeText(this, "Need permission", Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background

        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return bitmap
    }

    private inner class BitmapAsyncTask(val mBitmap: Bitmap) : ViewModel() {

        fun execute() = viewModelScope.launch {
            onPreExecute()
            val result = doInBackground()
            onPostExecute(result)
        }

        private lateinit var mProgressDialog: Dialog

        private fun onPreExecute() {
            showProgressDialog()
        }

        private suspend fun doInBackground(): String = withContext(Dispatchers.IO) {

            var result = ""

            try {
                val bytes = ByteArrayOutputStream()
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
                val f =
                    File(externalCacheDir!!.absoluteFile.toString() + File.separator + "DrawingApp_" + System.currentTimeMillis() / 1000 + ".png")
                val fos = FileOutputStream(f)
                fos.write(bytes.toByteArray())
                fos.close()
                result = f.absolutePath
            } catch (e: Exception) {
                result = ""
                e.printStackTrace()
            }
            return@withContext result
        }

        private fun onPostExecute(result: String?) {
            cancelDialog()
            if (!result!!.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "File Saved Succesfully : $result",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong while saving file",
                    Toast.LENGTH_SHORT
                ).show()
            }

            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null) { path, uri ->
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/png"
                startActivity(
                    Intent.createChooser(
                        shareIntent, "Share"
                    )
                )
            }
        }

        private fun showProgressDialog() {
            mProgressDialog = Dialog(this@MainActivity)
            mProgressDialog.setContentView(R.layout.dialog_custom_progress)
            mProgressDialog.show()

        }

        private fun cancelDialog() {
            mProgressDialog.dismiss()
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }
}