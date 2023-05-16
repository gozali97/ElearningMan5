package com.example.elearningman5

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@Suppress("DEPRECATION")
class UTask(
    context: Context?,
    private var url: String?)
{
    private var context: Context?
    private lateinit var fileUri: Uri
    private var dataKey: String? = null
    private var response: String? = null
    private var statusCode = 0
    private var fileName: String = "example.pdf"

    init {
        this.context = context
    }

    fun setDataFile(fileUri: Uri) {
        this.fileUri = fileUri
    }

    fun setDataKey(dataKey: String) {
        this.dataKey = dataKey
    }

    fun setFileName(fileName: String) {
        this.fileName = fileName
    }

    fun getResponse(): String? {
        return response
    }

    fun getStatusCode(): Int {
        return statusCode
    }

    fun send() {
        try {
            // boundary untuk memisahkan setiap bagian dari permintaan
            val boundary = "----Boundary" + System.currentTimeMillis()

            // membuat permintaan
            val sUrl = URL(url)
            val connection = sUrl.openConnection() as HttpURLConnection

            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            // membuka stream keluaran untuk menulis ke permintaan
            val outputStream = DataOutputStream(connection.outputStream)

            // file PDF
            outputStream.writeBytes("--$boundary\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
            outputStream.writeBytes("Content-Type: application/pdf\r\n\r\n")

            val inputStream = context?.contentResolver?.openInputStream(fileUri)
            val buffer = ByteArray(10485760) // 10 MB (10 x 1024 x 1024 = 10485760 byte)
            var bytesRead = -1
            var totalBytesRead = 0

            while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                if (totalBytesRead > (10 * 1024 * 1024)) { // Melebihi 10 MB
                    throw Exception("File terlalu besar")
                }
            }
            outputStream.writeBytes("\r\n")

            val jsonObject = dataKey?.let { JSONObject(it) }

            if (jsonObject != null) for (key in jsonObject.keys()) {
                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"$key\"\r\n\r\n")
                outputStream.writeBytes("${jsonObject.getString(key)}\r\n")
            }

            // menutup permintaan
            outputStream.writeBytes("--$boundary--\r\n")
            inputStream.close()
            outputStream.flush()
            outputStream.close()

            // mendapatkan respons dari server
            statusCode = connection.responseCode
            val responseStream =
                if (statusCode >= 400) connection.errorStream else connection.inputStream
            val `in` = BufferedReader(InputStreamReader(responseStream))

            var inputLine: String?
            val sb = StringBuffer()
            while (`in`.readLine().also { inputLine = it } != null) {
                sb.append(inputLine)
            }
            `in`.close()

            response = sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}