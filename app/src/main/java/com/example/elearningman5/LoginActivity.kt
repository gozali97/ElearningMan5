package com.example.elearningman5

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.example.elearningman5.databinding.ActivityLoginBinding
import com.example.elearningman5.pelengkap.*
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// Desain UI: https://badoystudio.com/membuat-login-ui-design-menarik-android/
class LoginActivity : AppCompatActivity() {
    private lateinit var localStorage: LocalStorage
    private lateinit var binding: ActivityLoginBinding

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @Suppress("DEPRECATION")
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(R.layout.abs_layout)
        }
        findViewById<AppCompatTextView>(R.id.toolbarTitle).text = "Madrasah Aliyah Negeri 5 Sleman"

        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding =  ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        localStorage = LocalStorage(this@LoginActivity)

        val builder = this@LoginActivity.let { AlertDialog.Builder(it) }
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()

        if (localStorage.getEmail()?.isNotEmpty()!! &&
            localStorage.getNis()?.isNotEmpty()!! &&
            localStorage.getSesi()?.isNotEmpty()!! &&
            localStorage.getToken()?.isNotEmpty()!!
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
                localStorage.setToken("")
                dialog.dismiss()
                Toast.makeText(this@LoginActivity, "Sessi anda sudah habis, silahkan login kembali", Toast.LENGTH_LONG).show()
            }
        }

        emailFocusListener()
        passwordFocusListener()
        binding.btnLogin.setOnClickListener { checkLogin(dialog) }

        binding.root.setOnClickListener { view ->
            if (
                view.id != binding.txtEmail.id &&
                view.id != binding.txtPassword.id &&
                view.id != binding.btnLogin.id
            ) {
                val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val currentFocus = currentFocus

                if (currentFocus != null) {
                    inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }
        }
    }

//    https://www.youtube.com/watch?v=Gc0sLf91QeM&t=4s
    private fun emailFocusListener() {
        binding.txtEmail.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.emailContainer.helperText = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val emailText = binding.txtEmail.text.toString()
        if (emailText.isEmpty())
            return "required"
        if (! Patterns.EMAIL_ADDRESS.matcher(emailText).matches())
            return "invalid Email Address"
        return null
    }

    private fun passwordFocusListener() {
        binding.txtPassword.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.passwordContainer.helperText = validPassword()
            }
        }
    }

    private fun validPassword(): String? {
        val passwordText = binding.txtPassword.text.toString()
        if (passwordText.isEmpty())
            return "required"
        if (passwordText.length < 8)
            return "minimum 8 Character Password"
        return null
    }

    private fun checkLogin(dialog: AlertDialog) {
        binding.emailContainer.helperText = validEmail()
        binding.passwordContainer.helperText = validPassword()

        val validEmail = binding.emailContainer.helperText == null
        val validPassword = binding.passwordContainer.helperText == null

        if (validEmail && validPassword) {
            dialog.show()
            sendLogin(binding.txtEmail.text.toString(), binding.txtPassword.text.toString(), dialog)
        } else {
            alertFail("Tolong dicek kembali", this@LoginActivity)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun sendLogin(email: String, password: String, dialog: AlertDialog) {
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
//                            localStorage.setToken(response.getJSONObject("siswa").getString("token"))
                            localStorage.setToken("cek")
                            localStorage.setSesi(LocalDateTime.now().plusDays(5).format(formatter).toString())

                            startActivity(Intent(this@LoginActivity, UserActivity::class.java))
                            finish()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    422 -> {
                        try {
                            response?.let { alertFail(it.getString("message"), this@LoginActivity) }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    401 -> {
                        kode401(response!!.getString("message"), this)
                        localStorage.setToken("")

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    else -> {
                        Log.d("TAG, sendLogin: ", code.toString())
                        alertFail(response?.getString("message").toString(), this@LoginActivity)
                    }
                }
                dialog.dismiss()
                binding.txtEmail.text?.clear()
                binding.txtPassword.text?.clear()
            }
        }.start()
    }
}