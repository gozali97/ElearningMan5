package com.example.elearningman5.firebase

import android.content.Context
import android.util.Log
import com.example.elearningman5.LocalStorage
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

class FirebaseMessagingHelper(context: Context) {
    private val TAG = "FirebaseMessagingHelper"
    private var localStorage: LocalStorage
    private var jsonSubTopic: JSONObject? = null

    init {
        localStorage = LocalStorage(context)
        jsonSubTopic = localStorage.getJsonSubTopic()
    }

    private fun subscribe(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribed to topic $topic success"
                if (!task.isSuccessful) {
                    msg = "Subscribe to topic $topic failed"
                    jsonSubTopic?.put(topic, false)?.let { localStorage.setJsonSubTopic(it) }
                }
                Log.d(TAG, msg)
            }
    }

    private fun unsubscribe(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                var msg = ""
                if (!task.isSuccessful) {
                    msg = "Unsubscribe to topic $topic failed"
                } else {
                    msg = "Unsubscribe to topic $topic success"
                    localStorage.removeDataFromJsonSubTopic(topic)
                }
                Log.d(TAG, msg)
            }
    }

    fun subscribeTopics(topic: String) {
        if (jsonSubTopic?.has(topic) == true) { // ada topic-nya
            if (jsonSubTopic?.optBoolean(topic)!!) { // jika sudah sub
                Log.d(TAG, "subscribeTopics: ($topic) True")
            } else { // jika belum
                Log.d(TAG, "subscribeTopics: ($topic) false")
                jsonSubTopic?.put(topic, true)?.let { localStorage.setJsonSubTopic(it) }
                subscribe(topic)
            }
        } else { // jika ada topic baru
            Log.d(TAG, "subscribeTopics: ($topic) baru")
            jsonSubTopic?.put(topic, true)?.let { localStorage.setJsonSubTopic(it) }
            subscribe(topic)
        }
        Log.d(TAG, "subscribeTopics: ${ localStorage.getJsonSubTopic() }")
    }

    fun unsubscribeTopics() {
        val keys = jsonSubTopic?.keys()

        while (keys?.hasNext()!!) {
            unsubscribe(keys.next())
        }
    }
}