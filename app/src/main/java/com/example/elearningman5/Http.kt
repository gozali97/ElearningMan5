package com.example.elearningman5

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class Http(context: Context?, private var url: String?) {
    private var method:String? = "GET"
    private var data:String? = null
    private var response:String? = null
    private var statusCode = 0
    private var token = false
    private var localStorage: LocalStorage

    init {
        localStorage = LocalStorage(context)
    }

    fun setMethod(method: String) {
        this.method = method.uppercase(Locale.getDefault())
    }

    fun setData(data: String) {
        this.data = data
    }

    fun setToken(token: Boolean) {
        this.token = token
    }

    fun getResponse(): String? {
        return response
    }

    fun getStatusCode(): Int {
        return statusCode
    }

    fun send() {
        var connection: HttpURLConnection? = null
        try {
            val sUrl = URL(url)
            connection = sUrl.openConnection() as HttpURLConnection

            connection.requestMethod = method
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest")
            if (token) {
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + localStorage.getToken()
                )
            }
            if (method != "GET") {
                connection.doOutput = true
            }
            if (data != null) {
                val os = connection.outputStream
                os.write(data!!.toByteArray())
                os.flush()
                os.close()
            }
            statusCode = connection.responseCode
            val isr: InputStreamReader = if (statusCode in 200..299) {
                // if success response
                InputStreamReader(connection.inputStream)
            } else {
                InputStreamReader(connection.errorStream)
            }
            val br = BufferedReader(isr)
            val sb = StringBuffer()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            br.close()

            response = sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            // Disconnect koneksi setelah selesai
            connection?.disconnect()
        }
    }
}