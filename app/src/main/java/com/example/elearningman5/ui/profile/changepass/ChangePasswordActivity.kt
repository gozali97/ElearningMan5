package com.example.elearningman5.ui.profile.changepass

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.elearningman5.R

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar!!.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
    }
}