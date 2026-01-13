package com.example.foodlink.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * This helper object handles showing system notifications to the user.
 * It is primarily used to show the OTP (Verification Code) during registration.
 */
object NotificationHelper {
    private const val CHANNEL_ID = "foodlink_otp_channel"
    private const val CHANNEL_NAME = "Verification Codes"

    /**
     * This function creates and displays a high-priority notification with the OTP code.
     * In a real app, this would be an email, but here we show it on the phone for ease of use.
     */
    fun showOtpNotification(context: Context, otp: String) {
        // Accessing the Android System's Notification service.
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 (Oreo) and above require a "Notification Channel" to work.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, 
                CHANNEL_NAME, 
                NotificationManager.IMPORTANCE_HIGH // Makes the notification pop up on screen.
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Building the actual notification content.
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email) // Email icon looks professional for OTP.
            .setContentTitle("FoodLink Verification Code")
            .setContentText("Your code is: $otp. Please use this to verify your account.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Removes notification once the user clicks it.
            .build()

        // Sending the notification to the user's status bar.
        notificationManager.notify(1, notification)
    }
}
