package com.example.elearningman5.ui.home.chat

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.example.elearningman5.*
import java.util.*


class ForumActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "SetTextI18n")

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(R.layout.abs_layout)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        }
        findViewById<AppCompatTextView>(R.id.toolbarTitle).text = "Forum Diskusi Materi"

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)

        val txtMessage = findViewById<EditText>(R.id.txtMessage)

        findViewById<CardView>(R.id.btnSendChat).setOnClickListener {
            if (txtMessage.text.isNotEmpty()) {
                val message = Message(
                    "app.email",
                    "App.name",
                    txtMessage.text.toString(),
                    "students",
                    Calendar.getInstance().timeInMillis.toString()
                )
                Log.d("TAG, onCreate: ", Calendar.getInstance().timeInMillis.toString())
                Log.d("TAG, onCreate: ", message.toString())
            }
        }
    }
}