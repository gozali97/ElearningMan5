package com.example.elearningman5.pelengkap

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.elearningman5.R
import org.json.JSONException

class alert {
}

fun alertFail (s: String, context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Failed")
        .setIcon(R.drawable.ic_warning_24)
        .setMessage(s)
        .setPositiveButton("OK"
        ) { dialog, _ -> dialog.dismiss() }
        .show()
}

fun kode401 (s: String, context: Context) {
    try {
        Toast.makeText(context, "$s Silahkan Login Kembali!", Toast.LENGTH_LONG ).show()
    } catch (e: JSONException) {
        e.printStackTrace()
    }
}