package com.example.elearningman5.ui.home.tugas

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.elearningman5.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Suppress("DEPRECATION")
class TugasActivity : AppCompatActivity() {

    private lateinit var localStorage: LocalStorage
    private var fileTugas: String? = null
    private var fileSiswa: String? = null
    private var detailIdTugas: String? = null
    private lateinit var linearLayout: LinearLayout
    // Handler untuk memperbarui waktu setiap detik
    private val handler = Handler()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar!!.title = "TUGAS (${intent.extras?.getString("nama_materi")})"

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tugas)

        localStorage = LocalStorage(this@TugasActivity)
        linearLayout = findViewById(R.id.idLinearLayout)
        val btnUpload = findViewById<Button>(R.id.btnUpload)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val recCardFileTugas = findViewById<CardView>(R.id.recCardFileTugas)
        val pdfFileIcon = findViewById<CardView>(R.id.pdfFileIcon)

        val builder = this@TugasActivity.let { AlertDialog.Builder(it) }
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)

        val dialog = builder.create()
        dialog.show()

        getTugas(dialog)

        btnUpload.setOnClickListener {
            val intent = Intent()
            intent.type = "application/pdf"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "Select Pdf File"), 1)
        }

        btnUpdate.setOnClickListener {
            val intent = Intent()
            intent.type = "application/pdf"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "Select Pdf File Terbaru"), 2)
        }

        recCardFileTugas.setOnClickListener {
            if (! fileTugas?.isEmpty()!!) {
//                download(getString(R.string.api_server).trimEnd('/') + "/assets/tugas/$fileTugas",
                download(getString(R.string.api_server).replace("/api", "/assets/tugas/$fileTugas"),
                    fileTugas!!
                )
            }
        }

        pdfFileIcon.setOnClickListener {
            if (! fileSiswa?.isEmpty()!!) {
                download(getString(R.string.api_server).replace("/api", "/assets/tugas-siswa/$fileSiswa"),
                    fileSiswa!!
                )
            }
        }
    }

    private fun download(url: String, filename: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setDescription("Downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }

    @SuppressLint("SetTextI18n")
    private fun getTugas(dialog: AlertDialog) {
        val params = JSONObject()
        try {
            params.put("tugas_id", intent.extras?.getString("key_tugas"))
            params.put("siswa_id", localStorage.getNis())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val data = params.toString()
        val url = getString(R.string.api_server) + "/siswa/viewTugas"

        Thread {
            val http = Http(this@TugasActivity, url)
            http.setMethod("post")
            http.setData(data)
            http.send()

            runOnUiThread {
                var response = http.getResponse()?.let { JSONObject(it) }
                when (val code = http.getStatusCode()) {
                    200 -> {
                        try {
                            response = response?.getJSONObject("data") as JSONObject

                            findViewById<TextView>(R.id.judulTugas).text = response.getString("nama_tugas")
                            findViewById<TextView>(R.id.deskripsiTugas).text = response.getString("deskripsi")

                            findViewById<TextView>(R.id.creatTugas).text =
                                response.getString("created_at").String2Date("yyyy-MM-dd'T'HH:mm:ss")
                                    ?.let {
                                        SimpleDateFormat("EEEE hh:mm a, dd MMMM yyyy", Locale("id", "ID"))
                                            .format(it).toString()
                                    }

                            fileTugas = response.getString("file_tugas")
                            val waktuSisa = findViewById<TextView>(R.id.waktuSisa)

                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            val sekarang = LocalDateTime.now().minusDays(1).format(formatter).String2Date("yyyy-MM-dd HH:mm:ss")
                            val deatline = response.getString("tanggal_selesai").String2Date("yyyy-MM-dd HH:mm:ss")

                            if (response.getString("detail_tugas").toString() == "null") {
                                linearLayout.removeView(findViewById<CardView>(R.id.pdfFileIcon))
                                linearLayout.removeView(findViewById<Button>(R.id.btnUpdate))

                                if (sekarang?.before(deatline)!!) {
                                    // Memulai pembaruan waktu secara berkala
                                    handler.post(updateTimeRunnable(deatline))
                                } else {
                                    linearLayout.removeView(findViewById<Button>(R.id.btnUpload))
                                }
                            } else {
                                val detailTugas = response.getJSONObject("detail_tugas")

                                linearLayout.removeView(findViewById<Button>(R.id.btnUpload))
                                fileSiswa = detailTugas.getString("file")
                                detailIdTugas = detailTugas.getString("id_detail_tugas")

                                if (detailTugas.getInt("nilai") == 0) {
                                    if (sekarang?.before(deatline)!!) {
                                        handler.post(updateTimeRunnable(deatline))
                                    } else {
                                        waktuSisa.text = "Guru Belum Memberi Nilai"
                                        waktuSisa.setTextColor(ContextCompat.getColor(this@TugasActivity, R.color.lavender))
                                        linearLayout.removeView(findViewById<Button>(R.id.btnUpdate))
                                    }
                                } else {
                                    linearLayout.removeView(findViewById<Button>(R.id.btnUpdate))
                                    waktuSisa.text = "Nilai: " + detailTugas.getString("nilai")
                                    waktuSisa.setTextColor(ContextCompat.getColor(this@TugasActivity, R.color.green))
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    422 -> {
                        try {
                            response?.let { alertFail(it.getString("message")) }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    401 -> {
                        try {
                            response?.let { alertFail(it.getString("message")) }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    else -> {
                        Toast.makeText(this@TugasActivity, "Error $code --> ${
                            response?.getString("message")
                        }", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
        }.start()
    }

    // Menjalankan pembaruan waktu setiap detik
    private fun updateTimeRunnable(deatline: Date?): Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val timeNow = LocalDateTime.now().minusDays(1).format(formatter).String2Date("yyyy-MM-dd HH:mm:ss")

            // Menampilkan waktu di TextView
            findViewById<TextView>(R.id.waktuSisa).text = "Sisa waktu: " + SelisihDateTime(deatline!!,
                timeNow!!
            )

            if (!timeNow.before(deatline)) {
                handler.removeCallbacks(updateTimeRunnable(deatline))
                // refresh
                finish()
                startActivity(intent)
            }

            // Mengulangi pembaruan waktu setiap detik
            handler.postDelayed(this, 1000)
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null && data.data != null) {
            val params = JSONObject()

            if (requestCode == 1) {
                val url = getString(R.string.api_server) + "/siswa/uploadTugas"

                try {
                    params.put("tugas_id", intent.extras?.getString("key_tugas"))
                    params.put("siswa_id", localStorage.getNis().toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                setTugas(data.data!!, params, url)
            } else if (requestCode == 2) {
                val url = getString(R.string.api_server) + "/siswa/updateTugas"

                try {
                    params.put("detail_id", detailIdTugas)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                setTugas(data.data!!, params, url)
            }
        }
    }

    private fun setTugas(fileUri: Uri, params: JSONObject, url: String) {
        Thread {
            val uploadTask = UTask(this@TugasActivity, url)
            uploadTask.setDataFile(fileUri)
            uploadTask.setFileName(getNameFile(fileUri))
            uploadTask.setDataKey(params.toString())
            uploadTask.send()

            runOnUiThread {
                val response = uploadTask.getResponse()?.let { JSONObject(it) }
                when (val code = uploadTask.getStatusCode()) {
                    200 -> {
                        try {
                            // refresh
                            finish()
                            startActivity(intent)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    422 -> {
                        try {
                            response?.let { alertFail(it.getString("message")) }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    401 -> {
                        try {
                            response?.let { alertFail(it.getString("message")) }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    else -> {
                        Toast.makeText(this@TugasActivity, "Error $code, ${response?.getString("message")} (Max File 10MB)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()
    }

    @SuppressLint("Range")
    private fun getNameFile(fileUri: Uri): String {
        val uriString: String = fileUri.toString()
        var fileName: String? = "example.pdf"

        if (uriString.startsWith("content://")) {
            var myCursor: Cursor? = null

            try {
                myCursor = applicationContext.contentResolver
                    .query(fileUri, null, null, null, null)

                if (myCursor != null && myCursor.moveToFirst()) {
                    fileName = myCursor
                        .getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                myCursor?.close()
            }
        }

        return fileName.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Menghentikan pembaruan waktu saat Activity dihancurkan
        handler.removeCallbacks(updateTimeRunnable("2022-05-05 00:00:00".String2Date("yyyy-MM-dd HH:mm:ss")))
    }

    private fun alertFail(string: String) {
        AlertDialog.Builder(this@TugasActivity)
            .setTitle("Failed")
            .setIcon(R.drawable.ic_warning_24)
            .setMessage(string)
            .setPositiveButton("OK"
            ) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}