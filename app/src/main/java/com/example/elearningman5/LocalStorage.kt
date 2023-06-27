package com.example.elearningman5

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class LocalStorage(context: Context?) {
    private var sharedPreferences: SharedPreferences =
        context!!.getSharedPreferences("STORAGE_LOGIN_API", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()
    private var token: String? = null
    private var email: String? = null
    private var nis: String? = null
    private var sesiBerakhir: String? = null
    private var jsonSubTopic: JSONObject = JSONObject()

    fun getToken(): String? {
        return sharedPreferences.getString("access_token", "")
    }

    fun setToken(token: String?) {
        editor.putString("access_token", token)
        editor.commit()
        this.token = token
    }

    fun getEmail(): String? {
        return sharedPreferences.getString("email", "")
    }

    fun setEmail(email: String) {
        editor.putString("email", email).commit()
        this.email = email
    }

    fun getNis(): String? {
        return sharedPreferences.getString("nis", "")
    }

    fun setNis(nis: String) {
        editor.putString("nis", nis).commit()
        this.nis = nis
    }

    fun getSesi(): String? {
        return sharedPreferences.getString("sesiBerakhir", "")
    }

    fun setSesi(sesi_berakhir: String) {
        editor.putString("sesiBerakhir", sesi_berakhir).commit()
        this.sesiBerakhir = sesi_berakhir
    }

    fun getJsonSubTopic(): JSONObject {
        val jsonStr = sharedPreferences.getString("jsonSubTopic", null)
        return if (jsonStr != null) {
            JSONObject(jsonStr)
        } else {
            JSONObject()
        }
    }

    fun setJsonSubTopic(json: JSONObject) {
        editor.putString("jsonSubTopic", json.toString()).commit()
        jsonSubTopic = json
    }

    fun removeDataFromJsonSubTopic(key: String) {
        val json = getJsonSubTopic()
        json.remove(key)
        setJsonSubTopic(json)
    }

    fun clearJsonSubTopic() {
        editor.remove("jsonSubTopic").commit()
        jsonSubTopic = JSONObject()
    }
}