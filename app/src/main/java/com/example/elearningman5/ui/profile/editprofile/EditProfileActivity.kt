package com.example.elearningman5.ui.profile.editprofile

import android.annotation.SuppressLint
import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.elearningman5.alertFail
import com.example.elearningman5.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar!!.hide()

        super.onCreate(savedInstanceState)
        binding =  ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inputFocusListener()
        binding.btnUpdate.setOnClickListener { checkValid() }
    }

    private fun checkValid() {
        binding.namaContainer.helperText = validNama()
        binding.noHpContainer.helperText = validHp()

        val nama = binding.namaContainer.helperText == null
        val hp = binding.noHpContainer.helperText == null

        if (nama && hp) {
            Log.d(ContentValues.TAG, "checkValid: valid")
            Toast.makeText(this@EditProfileActivity,
                "txtNama: ${ binding.txtNama.text }\n\n txtNoHP: ${ binding.txtNoHP.text }",
                Toast.LENGTH_LONG).show()
        } else {
            binding.txtNama.text?.clear()
            binding.txtNoHP.text?.clear()
            Log.d(ContentValues.TAG, "checkValid: invalid")
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