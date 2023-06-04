package com.example.elearningman5.ui.profile.editprofile

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.elearningman5.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar!!.hide()
//        findViewById<AppCompatTextView>(R.id.toolbarTitle).text = "Edit Profile ${intent.extras?.getString("name")}"

        super.onCreate(savedInstanceState)
        binding =  ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}