package com.example.elearningman5.ui.profile.editprofile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.elearningman5.Http
import com.example.elearningman5.R
import com.example.elearningman5.UTask
import com.example.elearningman5.databinding.ActivityEditProfileBinding
import com.example.elearningman5.pelengkap.alertFail
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private var fileUri: Uri? = null
    private var fileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar!!.hide()

        super.onCreate(savedInstanceState)
        binding =  ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUpdate.setOnClickListener { checkValid() }
        binding.btnKembali.setOnClickListener { returnResultToFragment("kembali") }

        getProfile()
        inputFocusListener()

        binding.recProfile.setOnClickListener {
            selectImage()
        }

        binding.root.setOnClickListener { view ->
            if (
                view.id != binding.recProfile.id &&
                view.id != binding.txtNama.id &&
                view.id != binding.txtNoHP.id &&
                view.id != binding.txtAlamat.id &&
                view.id != binding.btnKembali.id &&
                view.id != binding.btnUpdate.id
            ) {
                val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val currentFocus = currentFocus

                if (currentFocus != null) {
                    inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }
        }
    }

    // https://www.section.io/engineering-education/picking-pdf-and-image-from-phone-storage/
    @SuppressLint("IntentReset")
    @Suppress("DEPRECATION")
    private fun selectImage() {
        val choice = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val alertDialog = AlertDialog.Builder(this@EditProfileActivity)

        alertDialog.setTitle("Select Image")
        alertDialog.setItems(choice, DialogInterface.OnClickListener { dialog, item ->
            when {
                // Select "Choose from Gallery" to pick image from gallery
                choice[item] == "Choose from Gallery" -> {
                    val pickFromGallery = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    pickFromGallery.type = "image/*"
                    startActivityForResult(Intent.createChooser(pickFromGallery, "Choose from Gallery"), 1)
                }
                // Select "Take Photo" to take a photo
                choice[item] == "Take Photo" -> {
                    val cameraPicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraPicture, 0)
                }
                // Select "Cancel" to cancel the task
                choice[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        })
        alertDialog.show()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        // For loading Image
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {
                    // Gambar diambil dari kamera
                    val imageBitmap = data.extras?.get("data") as Bitmap
                    binding.recProfile.setImageBitmap(imageBitmap)

                    // Simpan gambar ke file
                    val file = createImageFile()
                    saveBitmapToFile(imageBitmap, file)

                    // Mengatur fileUri dengan URI file yang disimpan
                    fileUri = Uri.fromFile(file)
                    fileName = file.name
                }
                1 -> if (resultCode == RESULT_OK && data != null) {
                    val imageSelected = data.data

                    if (imageSelected != null) {
                        val inputStream = contentResolver.openInputStream(imageSelected)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.recProfile.setImageBitmap(bitmap)
                        inputStream?.close()

                        fileUri = imageSelected
                        fileName = getFileNameFromUri(imageSelected)
                    }
                }
            }
        }
    }

    private fun createImageFile(): File {
        // Buat nama file unik
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        // Direktori penyimpanan
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Buat file
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        )
    }

    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    }

    private fun updateProfile() {
        val params = JSONObject()
        try {
            params.put("email", intent.extras?.getString("email"))
            params.put("name", binding.txtNama.text)
            params.put("no_hp", binding.txtNoHP.text)
            params.put("alamat", binding.txtAlamat.text)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val url = getString(R.string.api_server) + "/profil/updateProfile"

        Thread {
            val uploadTask = UTask(this@EditProfileActivity, url)
            uploadTask.setDataFile(fileUri)
            uploadTask.setFileName(fileName.toString())
            uploadTask.setDataKey(params.toString())
            uploadTask.send()

            runOnUiThread {
                val response = uploadTask.getResponse()?.let { JSONObject(it) }
                when (val code = uploadTask.getStatusCode()) {
                    200 -> {
                        try {
                            Toast.makeText(this@EditProfileActivity, "Profile berhasil diupdate", Toast.LENGTH_LONG).show()
                            returnResultToFragment("refresh")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    422 -> {
                        try {
                            response?.let { alertFail(it.getString("message"), this) }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    401 -> {
                        try {
                            response?.let { alertFail(it.getString("message"), this) }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    else -> {
                        Toast.makeText(this@EditProfileActivity, "Error $code, ${response?.getString("message")}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()
    }

    private fun returnResultToFragment(s: String) {
        val resultIntent = Intent()
        resultIntent.putExtra("result_key", s)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun getFileNameFromUri(uri: Uri?): String {
        var fileName: String? = null
        val filePathColumn = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        val cursor = contentResolver.query(uri!!, filePathColumn, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                fileName = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return fileName.toString()
    }

    private fun getProfile() {
        val params = JSONObject()
        try {
            params.put("email", intent.extras?.getString("email"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val data = params.toString()
        val url = getString(R.string.api_server) + "/profil"

        Thread {
            val http = Http(this, url)
            http.setMethod("post")
            http.setData(data)
            http.send()

            runOnUiThread {
                val code = http.getStatusCode()
                if (code == 200) {
                    try {
                        val response =
                            http.getResponse()?.let { JSONObject(it).getJSONObject("data") }

                        Picasso.with(this)
                            .load(getString(R.string.api_server)
                                .replace("/api",
                                    "/assets/img/${response?.getString("gambar")}"))
                            .error(R.drawable.uploadimg)
                            .resize(300, 300)
                            .centerCrop()
                            .skipMemoryCache()
                            .into(binding.recProfile)

                        binding.txtNama.setText(response?.getString("name"))
                        binding.txtNoHP.setText(response?.getString("no_hp"))
                        binding.txtAlamat.setText(response?.getString("alamat"))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this, "Error $code", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun checkValid() {
        binding.namaContainer.helperText = validNama()
        binding.noHpContainer.helperText = validHp()

        val nama = binding.namaContainer.helperText == null
        val hp = binding.noHpContainer.helperText == null

        if (nama && hp) {
//            updateProfile()
            alertFail("Fungsi belum diaktifkan", this@EditProfileActivity)
        } else {
            alertFail("Tolong dicek kembali", this@EditProfileActivity)
        }
    }

    private fun inputFocusListener() {
        binding.txtNama.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.namaContainer.helperText = validNama()
            }
        }

        binding.txtNoHP.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.noHpContainer.helperText = validHp()
            }
        }
    }

    private fun validHp(): String? {
        val noHP = binding.txtNoHP.text.toString()

        if (noHP.isEmpty())
            return "required"
        if (noHP.length > 12)
            return "Maximum 12 number"
        return null
    }

    private fun validNama(): String? {
        val nama = binding.txtNama.text.toString()

        if (nama.isEmpty())
            return "required"
        if (nama.length < 4)
            return "Minimum 4 Character"
        return null
    }
}