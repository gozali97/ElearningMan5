package com.example.elearningman5

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar!!.title = "LOGIN PAGE"
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        localStorage = LocalStorage(this@LoginActivity)

        etEmail = findViewById(R.id.getEmail)
        etPassword = findViewById(R.id.getPassword)
        btnLogin = findViewById(R.id.btnLogin)

        if (localStorage.getEmail()?.isNotEmpty()!! &&
            localStorage.getNis()?.isNotEmpty()!! &&
            localStorage.getSesi()?.isNotEmpty()!!
        ) {
            if (LocalDateTime.now()
                    .format(formatter)
                    .String2Date("yyyy-MM-dd")?.before(localStorage.getSesi()
                        .String2Date("yyyy-MM-dd"))!!) {

                startActivity(Intent(this@LoginActivity, UserActivity::class.java))
                finish()
            } else {
                localStorage.setEmail("")
                localStorage.setNis("")
                localStorage.setSesi("")
                Toast.makeText(this, "Sessi anda sudah habis, silahkan login kembali", Toast.LENGTH_LONG).show()
            }
        }

        with(btnLogin) {
            this?.setOnClickListener { checkLogin() }
        }
    }

    private fun checkLogin() {
        email = etEmail!!.text.toString()
        password = etPassword!!.text.toString()
        if (email!!.isEmpty() || password!!.isEmpty()) {
            alertFail("Email and Password is Required")
        } else if (password!!.length < 8) {
            alertFail("Password are not long enough")
        } else {
            sendLogin()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun sendLogin() {
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
            }
        }.start()
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