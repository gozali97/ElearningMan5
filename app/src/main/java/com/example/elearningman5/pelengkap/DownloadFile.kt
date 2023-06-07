package com.example.elearningman5.pelengkap

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast

class DownloadFile {
    companion object {
        private var downloadCompleteReceiver: BroadcastReceiver? = null

        fun download(context: Context, url: String, filename: String) {
            val request = DownloadManager.Request(Uri.parse(url))
                .setDescription("Downloading...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = dm.enqueue(request)

            downloadCompleteReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val action = intent?.action
                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor = dm.query(query)
                        if (cursor.moveToFirst()) {
                            val columnIndex =
                                cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            val status = cursor.getInt(columnIndex)
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                // Download succeeded
                                Toast.makeText(context, "Download Succeeded", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                // Download failed
                                Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            cursor.close()
                        }
                    }
                }
            }

            val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            context.registerReceiver(downloadCompleteReceiver, intentFilter)
        }

        fun unregisterDownloadCompleteReceiver(context: Context) {
            downloadCompleteReceiver?.let {
                context.unregisterReceiver(it)
                downloadCompleteReceiver = null
            }
        }
    }
}