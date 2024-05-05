@file:Suppress("DEPRECATION")

package com.example.datto.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.GroupInfo
import com.example.datto.DataClass.MessageRequest
import com.example.datto.DataClass.Notification
import com.example.datto.DataClass.NotificationRequest
import com.example.datto.MainActivity
import com.example.datto.R
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class FirebaseNotification(private val context: Context) {
    companion object {
        private const val TAG = "FirebaseNotificationMgr"
        const val ACTION_SHOW_NOTIFICATION = "ACTION_SHOW_NOTIFICATION"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        const val EXTRA_TOPIC = "EXTRA_TOPIC"
        const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
    }

    private val pendingIntentMap = mutableMapOf<Int, PendingIntent>()
    private val notificationIdCounter = AtomicInteger(0)
    private val dateFormat = SimpleDateFormat("MM:dd:yyyy:HH:mm:ss", Locale.getDefault())

    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to $topic")
                } else {
                    Log.e(TAG, "Subscribed to $topic failed", task.exception)
                }
            }
    }

    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Unsubscribed from $topic")
                } else {
                    Log.e(TAG, "Unsubscribed from $topic failed", task.exception)
                }
            }
    }

    fun compose(topic: String, title: String, body: String, sendAt: String = "now") {
        try {
            val newRequest = MessageRequest(NotificationRequest(topic, sendAt, Notification(title, body)))

            APIService(context).doPost<Any>(
                "notifications",
                newRequest,
                object :
                    APICallback<Any> {
                    override fun onSuccess(data: Any) {
                        Log.d(TAG, "Send message $title successfully: $data")
                        Log.d("API_SERVICE", "Send message $title successfully")
                    }

                    override fun onError(error: Throwable) {
                        Log.e(TAG, "Send message $title fail: ${error.message}")
                        Log.e("API_SERVICE", "Error: ${error.message}")
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Exception occurred: ${e.message}")
        }
    }


}

