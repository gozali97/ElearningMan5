package com.example.elearningman5

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class LoginActivity : AppCompatActivity() {
    private var email: String? = null
    private var password: String? = null
    private var etEmail: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private lateinit var localStorage: LocalStorage

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(R.layout.abs_layout)
        }
        findViewById<AppCompatTextView>(R.id.toolbarTitle).text = "Madrasah Aliyah Negeri 5 Sleman"

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        localStorage = LocalStorage(this@LoginActivity)

        etEmail = findViewById(R.id.txtEmail)
        etPassword = findViewById(R.id.txtPassword)
        btnLogin = findViewById(R.id.btnLogin)

        val builder = this@LoginActivity.let { AlertDialog.Builder(it) }
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()

        if (localStorage.getEmail()?.isNotEmpty()!! &&
            localStorage.getNis()?.isNotEmpty()!! &&
            localStorage.getSesi()?.isNotEmpty()!!
        ) {
            dialog.show()
            if (LocalDateTime.now()
                    .format(formatter)
                    .String2Date("yyyy-MM-dd")?.before(localStorage.getSesi()
                        .String2Date("yyyy-MM-dd"))!!) {

                dialog.dismiss()
                startActivity(Intent(this@LoginActivity, UserActivity::class.java))
                finish()
            } else {
                localStorage.setEmail("")
                localStorage.setNis("")
                localStorage.setSesi("")
                dialog.dismiss()
                Toast.makeText(this, "Sessi anda sudah habis, silahkan login kembali", Toast.LENGTH_LONG).show()
            }
        }

        with(btnLogin) {
            this?.setOnClickListener { checkLogin(dialog) }
        }
    }

    private fun checkLogin(dialog: AlertDialog) {
        email = etEmail!!.text.toString()
        password = etPassword!!.text.toString()
        if (email!!.isEmpty() || password!!.isEmpty()) {
            alertFail("Email and Password is Required")
        } else if (password!!.length < 8) {
            alertFail("Password are not long enough")
        } else {
            dialog.show()
            sendLogin(dialog)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun sendLogin(dialog: AlertDialog) {
        val params = JSONObject()

        try {
            params.put("email", email)
            params.put("password", password)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val data = params.toString()
        val url = getString(R.string.api_server) + "/login"

        Thread {
            val http = Http(this@LoginActivity, url)
            http.setMethod("post")
            http.setData(data)
            http.send()

            runOnUiThread {
                var response = http.getResponse()?.let { JSONObject(it) }
                when (val code = http.getStatusCode()) {
                    200 -> {
                        try {
                            response = response?.getJSONObject("data") as JSONObject

                            localStorage.setEmail(response.getString("email"))
                            localStorage.setNis(response.getJSONObject("siswa").getString("nis"))
                            localStorage.setSesi(LocalDateTime.now().plusDays(5).format(formatter).toString())

                            startActivity(Intent(this@LoginActivity, UserActivity::class.java))
                            finish()
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
                        Log.d("TAG, sendLogin: ", code.toString())
                        alertFail(response?.getString("message").toString())
                    }
                }
                findViewById<EditText>(R.id.txtPassword).text.clear()
                dialog.dismiss()
            }
        }.start()
    }

    private fun alertFail(s: String) {
        AlertDialog.Builder(this)
            .setTitle("Failed")
            .setIcon(R.drawable.ic_warning_24)
            .setMessage(Html.fromHtml("<font color='#AC1212'>$s</font>"))
            .setPositiveButton("OK"
            ) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}