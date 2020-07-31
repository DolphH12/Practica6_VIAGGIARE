package com.dolphhincapie.introviaggiare.channel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.dolphhincapie.introviaggiare.R

class NotificationHelper(base: Context?) : ContextWrapper(base) {

    private val CHANNEL_ID: String = "com.dolphhincapie.introviaggiare"
    private val CHANNEL_NAME: String = "VIAGGIARE"
    private var manager: NotificationManager? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels()
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannels() {
        val notificacionChannel: NotificationChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificacionChannel.enableLights(true)
        notificacionChannel.enableVibration(true)
        notificacionChannel.lightColor = Color.CYAN
        notificacionChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        getManager().createNotificationChannel(notificacionChannel)
    }

    fun getManager(): NotificationManager {
        if (manager == null) {
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return manager!!
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotification(
        title: String,
        body: String,
        intent: PendingIntent,
        soundUri: Uri
    ): Notification.Builder {
        return Notification.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(intent)
            .setSmallIcon(R.drawable.ic_car)
            .setStyle(Notification.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotificationAction(
        title: String,
        body: String,
        soundUri: Uri,
        acceptAction: Notification.Action,
        cancelAction: Notification.Action
    ): Notification.Builder {
        return Notification.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setSmallIcon(R.drawable.ic_car)
            .addAction(acceptAction)
            .addAction(cancelAction)
            .setStyle(Notification.BigTextStyle().bigText(body).setBigContentTitle(title))
    }


    fun getNotificationOldApi(
        title: String,
        body: String,
        intent: PendingIntent,
        soundUri: Uri
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(intent)
            .setSmallIcon(R.drawable.ic_car)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    fun getNotificationOldApiActions(
        title: String,
        body: String,
        soundUri: Uri,
        acceptAction: NotificationCompat.Action,
        cancelAction: NotificationCompat.Action
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setSmallIcon(R.drawable.ic_car)
            .addAction(acceptAction)
            .addAction(cancelAction)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

}