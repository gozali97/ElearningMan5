package com.example.elearningman5.ui.profile.changepass

import android.content.ContentValues.TAG
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.elearningman5.*
import com.example.elearningman5.databinding.ActivityChangePasswordBinding
import com.example.elearningman5.pelengkap.alertFail
import org.json.JSONException
import org.json.JSONObject

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar!!.hide()
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // https://www.youtube.com/watch?v=Gc0sLf91QeM&t=112s
        passwordFocusListener()
        binding.btnUpdate.setOnClickListener { checkValid() }
        binding.btnKembali.setOnClickListener { finish() }

        binding.root.setOnClickListener { view ->
            if (
                view.id != binding.txtOldPassword.id &&
                view.id != binding.txtNewPassword.id &&
                view.id != binding.txtConfirmPassword.id &&
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

    private fun checkValid() {
        binding.oldPasswordContainer.helperText = validOldPassword()
        binding.newPasswordContainer.helperText = validNewPassword()
        binding.confirmPasswordContainer.helperText = validConfirmPassword()

        val validOldPassword = binding.oldPasswordContainer.helperText == null
        val validNewPassword = binding.newPasswordContainer.helperText == null
        val validConfirmPassword = binding.confirmPasswordContainer.helperText == null

        if (validOldPassword && validNewPassword && validConfirmPassword) {
//            updatePassword()
            alertFail("Fungsi belum diaktifkan", this@ChangePasswordActivity)
        } else {
            alertFail("Tolong dicek kembali", this@ChangePasswordActivity)
        }

        binding.txtOldPassword.text?.clear()
        binding.txtNewPassword.text?.clear()
        binding.txtConfirmPassword.text?.clear()
    }

    private fun updatePassword() {
        val params = JSONObject()

        try {
            params.put("id", intent.extras?.getString("id"))
            params.put("email", intent.extras?.getString("email"))
            params.put("password", binding.txtOldPassword.text)
            params.put("new_password", binding.txtNewPassword.text)
            params.put("confirm_password", binding.txtConfirmPassword.text)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val data = params.toString()
        val url = getString(R.string.api_server) + "/profil/updatePassword"

        Thread {
            var response: JSONObject? = null
            val http = Http(this@ChangePasswordActivity, url)
            http.setMethod("post")
            http.setData(data)
            http.send()

            runOnUiThread {
                try {
                    response = http.getResponse()?.let { JSONObject(it) }
                    when (val code = http.getStatusCode()) {
                        200 -> {
                            try {
                                Toast.makeText(this@ChangePasswordActivity, response?.getString("data"), Toast.LENGTH_SHORT).show()
                                finish()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        422 -> {
                            try {
                                Toast.makeText(this@ChangePasswordActivity, response?.getString("message"), Toast.LENGTH_SHORT).show()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        401 -> {
                            try {
                                Toast.makeText(this@ChangePasswordActivity, response?.getString("message"), Toast.LENGTH_SHORT).show()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        else -> {
                            Log.d("TAG, Forum: ", "$code ${response?.getString("message").toString()}")
                            Toast.makeText(this@ChangePasswordActivity, response?.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.d(TAG, "getMessage: ${response?.getString("message")}")
                    // kesalahan parsing JSON
                    Toast.makeText(this@ChangePasswordActivity, "Tunggu Sebentar", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun passwordFocusListener() {
        binding.txtOldPassword.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.oldPasswordContainer.helperText = validOldPassword()
            }
        }

        binding.txtNewPassword.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.newPasswordContainer.helperText = validNewPassword()
            }
        }

        binding.txtConfirmPassword.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.confirmPasswordContainer.helperText = validConfirmPassword()
            }
        }
    }

    private fun validConfirmPassword(): String? {
        val newPasswordText = binding.txtNewPassword.text.toString()
        val confirmPasswordText = binding.txtConfirmPassword.text.toString()

        if (confirmPasswordText.isEmpty())
            return "required"
        if (confirmPasswordText != newPasswordText)
            return "password is not the same as new password"
        return null
    }

    private fun validNewPassword(): String? {
        val oldPasswordText = binding.txtOldPassword.text.toString()
        val newPasswordText = binding.txtNewPassword.text.toString()

        if (newPasswordText.isEmpty())
            return "required"
        if (newPasswordText.length < 8)
            return "minimum 8 Character Password"
        if (oldPasswordText == newPasswordText)
            return "password is same as old password"
        return null
    }

    private fun validOldPassword(): String? {
        val passwordText = binding.txtOldPassword.text.toString()
        if (passwordText.isEmpty())
            return "required"
        if (passwordText.length < 8)
            return "minimum 8 Character Password"
        return null
    }
}