package com.dolphhincapie.introviaggiare.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.dolphhincapie.introviaggiare.channel.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingClient : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val notification: RemoteMessage.Notification? = remoteMessage.notification
        val data: Map<String, String> = remoteMessage.data
        val title: String = data["title"].toString()
        val body: String = data["body"].toString()

        if (title != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showNotificationApiOreo(title, body)
            } else {
                showNotification(title, body)
            }
        }

    }

    private fun showNotification(title: String, body: String) {
        val intent: PendingIntent =
            PendingIntent.getActivity(baseContext, 0, Intent(), PendingIntent.FLAG_ONE_SHOT)
        val sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper: NotificationHelper = NotificationHelper(baseContext)
        val builder: NotificationCompat.Builder =
            notificationHelper.getNotificationOldApi(title, body, intent, sound)
        notificationHelper.getManager().notify(1, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotificationApiOreo(title: String, body: String) {
        val intent: PendingIntent =
            PendingIntent.getActivity(baseContext, 0, Intent(), PendingIntent.FLAG_ONE_SHOT)
        val sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper: NotificationHelper = NotificationHelper(baseContext)
        val builder: Notification.Builder =
            notificationHelper.getNotification(title, body, intent, sound)
        notificationHelper.getManager().notify(1, builder.build())
    }
}
