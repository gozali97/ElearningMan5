package com.example.elearningman5.ui.profile.changepass

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.elearningman5.alertFail
import com.example.elearningman5.databinding.ActivityChangePasswordBinding

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
    }

    private fun checkValid() {
        binding.oldPasswordContainer.helperText = validOldPassword()
        binding.newPasswordContainer.helperText = validNewPassword()
        binding.confirmPasswordContainer.helperText = validConfirmPassword()

        val validOldPassword = binding.oldPasswordContainer.helperText == null
        val validNewPassword = binding.newPasswordContainer.helperText == null
        val validConfirmPassword = binding.confirmPasswordContainer.helperText == null

        if (validOldPassword && validNewPassword && validConfirmPassword) {
            Log.d(TAG, "checkValid: valid")
            Toast.makeText(this@ChangePasswordActivity,
                "txtOldPassword: ${ binding.txtOldPassword.text }\n\n txtNewPassword: " +
                        "${ binding.txtNewPassword.text }\n\n txtConfirmPassword: ${ binding.txtConfirmPassword.text }",
                Toast.LENGTH_LONG
            ).show()
        } else {
            binding.txtOldPassword.text?.clear()
            binding.txtNewPassword.text?.clear()
            binding.txtConfirmPassword.text?.clear()
            Log.d(TAG, "checkValid: invalid")
            alertFail("Tolong dicek kembali", this@ChangePasswordActivity)
        }
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