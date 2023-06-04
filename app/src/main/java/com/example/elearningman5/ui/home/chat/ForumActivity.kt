package com.example.elearningman5.ui.home.chat

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elearningman5.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONException
import org.json.JSONObject
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.time.*
import java.util.*

class ForumActivity : AppCompatActivity(), ItemClickListener {
    private lateinit var localStorage: LocalStorage
    private lateinit var adapter: MessageAdapter
    private lateinit var messageList: RecyclerView
    private lateinit var visibilityElement: FloatingActionButton
    private lateinit var dialog: AlertDialog

    private val pattern = "yyyy-MM-dd'T'00:00:00"
    private val format = DateTimeFormatter.ofPattern(pattern)
    private val kemarin = LocalDateTime.now().format(format).String2Date(pattern)
    private val lusa = LocalDateTime.now().minusDays(1).format(format).String2Date(pattern)

    private var cekHari: Date? = lusa
    private var keyMessage = 0
    private var cekAwal = true

    private var retryAttempts = 1
    private val maxRetryAttempts = 4
    private var initialBackoffDelay = 1000L // 1 second
    private val handler = Handler()

    private var cekUpdate = false
    private var isYesteday = true
    private var isToday = true

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(R.layout.abs_layout)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        }
        findViewById<AppCompatTextView>(R.id.toolbarTitle).text = "Forum Diskusi (${intent.extras?.getString("nama_materi")})"

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)

        localStorage = LocalStorage(this@ForumActivity)
        val txtMessage = findViewById<EditText>(R.id.txtMessage)
        visibilityElement = findViewById(R.id.arrow_bottom)
        messageList = findViewById(R.id.messageList)

        // Membuat dialog progress
        val builder = AlertDialog.Builder(this@ForumActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        dialog = builder.create()
        dialog.show()

        messageList.layoutManager = LinearLayoutManager(this@ForumActivity)
        adapter = MessageAdapter(this@ForumActivity)

        adapter.setItemClickListener(this)
        messageList.adapter = adapter

        visibilityElement.setOnClickListener {
            visibilityElement.visibility = View.INVISIBLE
            messageList.smoothScrollToPosition(adapter.itemCount - 1)
        }

        findViewById<CardView>(R.id.btnSendChat).setOnClickListener {
            if (txtMessage.text.isNotEmpty()) {
                hideKeyboard()
                scrolledToBottom()

//               untuk created_at, updated_at
//                val sekarang = LocalDateTime.now().format(DateTimeFormatter
//                    .ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                val indonesiaDateTime = LocalDateTime.now()
                val utcDateTime = indonesiaDateTime.atZone(ZoneId.of("Asia/Jakarta")).withZoneSameInstant(
                    ZoneOffset.UTC)

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                val sekarang = utcDateTime.format(formatter)
                
                setMessage(txtMessage, sekarang)
            } else {
                Toast.makeText(applicationContext,"Message should not be empty", Toast.LENGTH_SHORT).show()
            }
        }
        handler.post(updateTimeRunnable)
    }

    override fun onItemClick(position: Int) {
        // Aksi yang ingin dilakukan saat item diklik
        Log.d(TAG, "onItemClick: klik RecyclerView")
        hideKeyboard()
    }

    @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
    private fun getMessage() {
        val urlForum = getString(R.string.api_server) + "/materi/diskusi"
        val paramsForum = JSONObject()

        try {
            paramsForum.put("materi_id", intent.extras?.getString("key_chat"))
            paramsForum.put("message_id", keyMessage)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val data = paramsForum.toString()

        Thread {
            var response: JSONObject? = null
            val http = Http(this@ForumActivity, urlForum)
            http.setMethod("post")
            http.setData(data)
            http.send()

            runOnUiThread {
                try {
                    response = http.getResponse()?.let { JSONObject(it) }
                    when (val code = http.getStatusCode()) {
                        200 -> {
                            try {
                                if (response?.getJSONArray("data").toString() != "[]") {
                                    for (i in 0 until (response?.getJSONArray("data")?.length() ?: 0)) {
                                        val item = response?.getJSONArray("data")?.getJSONObject(i)
                                        cekUpdate = true

                                        keyMessage = item!!.getString("id_diskusi").toInt()
                                        val waktu = utcToWib(item.getString("created_at"))
//                                        Log.d(TAG, "$keyMessage getMessage: $item")

                                        if(waktu!!.before(lusa)) {
                                            if (cekAwal) {
                                                cekAwal = false
                                                addMessage(pattern, waktu)
                                                Toast.makeText(this@ForumActivity, "Mohon Tunggu Sebentar", Toast.LENGTH_LONG).show()
                                            }

                                            if (! waktu.before(cekHari) ) {
                                                addMessage(pattern, waktu)
                                            }
                                        } else if(waktu.before(kemarin)) {
                                            if (isYesteday) {
                                                isYesteday = false
                                                addMessage("Yesterday")
                                            }
                                        }
                                        else {
                                            if (isToday) {
                                                isToday = false
                                                addMessage("Today")
                                            }
                                        }
                                        addMessage(waktu = waktu, item = item)
                                    }
                                } else if (cekUpdate) {
                                    adapter.notifyDataSetChanged()
                                    scrolledToBottom()
                                    dialog.dismiss()
                                    cekUpdate = false
                                } else if (keyMessage == 0) {
                                    dialog.dismiss()
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        422 -> {
                            try {
                                Toast.makeText(this@ForumActivity, response?.getString("message"), Toast.LENGTH_SHORT).show()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        401 -> {
                            try {
                                Toast.makeText(this@ForumActivity, response?.getString("message"), Toast.LENGTH_SHORT).show()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        429 -> {
                            Toast.makeText(this@ForumActivity, response?.getString("message"), Toast.LENGTH_SHORT).show()
                            makeRequestWithExponentialBackoff()
                        }
                        else -> {
                            Log.d("TAG, Forum: ", "$code ${response?.getString("message").toString()}")
                            Toast.makeText(this@ForumActivity, response?.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.d(TAG, "getMessage: ${response?.getString("message")}")
                    // kesalahan parsing JSON
                    Toast.makeText(this@ForumActivity, "Tunggu Sebentar", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    @SuppressLint("SimpleDateFormat")
    private fun addMessage(pattern: String = "null", waktu: Date?= null, item: JSONObject? = null) {
        if (item == null) {
            var formatWaktu: String = pattern

            if (waktu != null && pattern != "null") {
                val formatHari = SimpleDateFormat(pattern)
                formatWaktu = SimpleDateFormat("dd-MM-yyyy").format(waktu).toString()
                val calendar = Calendar.getInstance()

                calendar.time = waktu
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                cekHari = formatHari.format(calendar.time).String2Date(pattern)
            }

            // Tambahkan item baru ke dalam adapter
            adapter.addMessage(
                Message(
                "timeMessage",
                "timeMessage",
                "timeMessage",
                "timeMessage",
                    formatWaktu
            ))
        } else {
            adapter.addMessage(
                Message(
                    item.getString("email").toString(),
                    item.getString("name").toString(),
                    item.getString("isi_pesan").toString(),
                    item.getString("receiver_role").toString(),
                    SimpleDateFormat("hh:mm a").format(waktu!!).toString()
            ))
        }
    }

    private val updateTimeRunnable: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            getMessage()
            visibilityFloatingActionButton()

            // Mengulangi pembaruan waktu setiap detik
            Log.d(TAG, "run: handeler $initialBackoffDelay")
            handler.postDelayed(this, initialBackoffDelay)
        }
    }

    private fun makeRequestWithExponentialBackoff() {
        if (retryAttempts < maxRetryAttempts) {
            initialBackoffDelay *= retryAttempts.toLong()
            retryAttempts++

            // Wait for the backoff delay before retrying the request
            Thread.sleep(2000L)
        } else {
            // Maximum retry attempts reached, handle error or notify the user
            Toast.makeText(this@ForumActivity, "Tolong kembali, lalu buka Chat lagi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setMessage(txtMessage: EditText, sekarang: String) {
        val urlForum = getString(R.string.api_server) + "/materi/addDiskusi"
        val paramsForum = JSONObject()

        try {
            paramsForum.put("materi_id", intent.extras?.getString("key_chat"))
            paramsForum.put("isi_pesan", txtMessage.text.toString())
            paramsForum.put("email", localStorage.getEmail())
            paramsForum.put("waktu_pesan", sekarang)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val data = paramsForum.toString()

        Thread {
            val http = Http(this@ForumActivity, urlForum)
            http.setMethod("post")
            http.setData(data)
            http.send()

            runOnUiThread {
                val response = http.getResponse()?.let { JSONObject(it) }
                when (val code = http.getStatusCode()) {
                    200 -> {
                        try {
                            // Clean text box
                            txtMessage.text.clear()
                            Toast.makeText(this@ForumActivity, "Pesan berhasil dikirim", Toast.LENGTH_SHORT).show()
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
                        Log.d("TAG, Forum: ", code.toString())
                        alertFail(response?.getString("message").toString())
                    }
                }
            }
        }.start()
    }

    private fun scrolledToBottom() {
        // Mengecek apakah fokus tampilan berada di bawah
        if (!messageList.canScrollVertically(1)) {
            // Mengatur posisi scroll ke item terakhir dengan animasi
            messageList.scrollToPosition(adapter.itemCount - 1)
//            smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun visibilityFloatingActionButton() {
        val layoutManager = messageList.layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        if (((adapter.itemCount - 1) - firstVisibleItemPosition) > 16) {
            visibilityElement.visibility = View.VISIBLE
        } else {
            visibilityElement.visibility = View.INVISIBLE
        }
    }

    private fun hideKeyboard() {
        // Hide keyboard
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = currentFocus
//        cek keyboard terbuka tau tidak
        if (currentFocus != null) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Menghentikan pembaruan waktu saat Activity dihancurkan
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun alertFail(s: String) {
        AlertDialog.Builder(this)
            .setTitle("Failed")
            .setIcon(R.drawable.ic_warning_24)
            .setMessage(s)
            .setPositiveButton("OK"
            ) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}