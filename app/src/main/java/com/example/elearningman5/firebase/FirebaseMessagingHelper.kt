package com.example.elearningman5.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

class FirebaseMessagingHelper() {///kurang unsub
    private val TAG = "FirebaseMessagingHelper"

    fun subscribeTopics(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribed to topic $topic"
                if (!task.isSuccessful) {
                    msg = "Subscribe to topic $topic failed"
                }
                Log.d(TAG, msg)
            }
    }
}