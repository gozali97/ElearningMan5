package com.example.elearningman5.pelengkap

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.elearningman5.R

class alert {
}

fun alertFail(s: String, context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Failed")
        .setIcon(R.drawable.ic_warning_24)
        .setMessage(s)
        .setPositiveButton("OK"
        ) { dialog, _ -> dialog.dismiss() }
        .show()
}