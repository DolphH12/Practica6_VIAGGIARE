package com.dolphhincapie.introviaggiare.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.channel.NotificationHelper
import com.dolphhincapie.introviaggiare.driver.NotificationBookingActivity
import com.dolphhincapie.introviaggiare.receives.AcceptReceiver
import com.dolphhincapie.introviaggiare.receives.CancelReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingClient : FirebaseMessagingService() {

    private var NOTIFICATION_CODE = 100
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val notification: RemoteMessage.Notification? = remoteMessage.notification
        val data: Map<String, String> = remoteMessage.data
        val title: String = data["title"].toString()
        val body: String = data["body"].toString()
        val idClient: String = data["idClient"].toString()

        if (title != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    val idClient: String = data["idClient"].toString()
                    val origin: String = data["origin"].toString()
                    val destination: String = data["destination"].toString()
                    val min: String = data["time"].toString()
                    val distance: String = data["km"].toString()
                    val precio: String = data["precio"].toString()
                    showNotificationApiOreoActions(title, body, idClient)
                    showNotificationActivity(idClient, origin, destination, min, distance, precio)
                } else if (title.contains("VIAJE CANCELADO")) {
                    val manager: NotificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(2)
                    showNotificationApiOreo(title, body)
                } else {
                    showNotificationApiOreo(title, body)
                }

            } else {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    val idClient: String = data["idClient"].toString()
                    val origin: String = data["origin"].toString()
                    val destination: String = data["destination"].toString()
                    val min: String = data["time"].toString()
                    val distance: String = data["km"].toString()
                    val precio: String = data["precio"].toString()
                    showNotificationActions(title, body, idClient)
                    showNotificationActivity(idClient, origin, destination, min, distance, precio)
                } else if (title.contains("VIAJE CANCELADO")) {
                    val manager: NotificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(2)
                    showNotification(title, body)
                } else {
                    showNotification(title, body)
                }
            }
        }

    }

    private fun showNotificationActivity(
        idClient: String,
        origin: String,
        destination: String,
        min: String,
        distance: String,
        precio: String
    ) {
        val pm: PowerManager = baseContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn: Boolean = pm.isScreenOn
        if (!isScreenOn) {
            val wakeLock: PowerManager.WakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "Appname:MyLock"

            )
            wakeLock.acquire(10000)
        }
        val intent: Intent = Intent(baseContext, NotificationBookingActivity::class.java)
        intent.putExtra("idClient", idClient)
        intent.putExtra("origin", origin)
        intent.putExtra("destination", destination)
        intent.putExtra("time", min)
        intent.putExtra("km", distance)
        intent.putExtra("precio", precio)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
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

    private fun showNotificationActions(title: String, body: String, idClient: String) {
        //ACCEPT
        val acceptIntent: Intent = Intent(this, AcceptReceiver::class.java)
        acceptIntent.putExtra("idClient", idClient)
        val acceptPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            this,
            NOTIFICATION_CODE,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val acceptAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            "Aceptar",
            acceptPendingIntent
        ).build()
        //CANCEL
        val cancelIntent: Intent = Intent(this, CancelReceiver::class.java)
        cancelIntent.putExtra("idClient", idClient)
        val cancelPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            this,
            NOTIFICATION_CODE,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            "Rechazar",
            cancelPendingIntent
        ).build()

        val intent: PendingIntent =
            PendingIntent.getActivity(baseContext, 0, Intent(), PendingIntent.FLAG_ONE_SHOT)
        val sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper: NotificationHelper = NotificationHelper(baseContext)
        val builder: NotificationCompat.Builder =
            notificationHelper.getNotificationOldApiActions(
                title,
                body,
                sound,
                acceptAction,
                cancelAction
            )
        notificationHelper.getManager().notify(2, builder.build())
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotificationApiOreoActions(title: String, body: String, idClient: String) {
        //ACCEPT
        val acceptIntent: Intent = Intent(this, AcceptReceiver::class.java)
        acceptIntent.putExtra("idClient", idClient)
        val acceptPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            this,
            NOTIFICATION_CODE,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val acceptAction: Notification.Action = Notification.Action.Builder(
            R.mipmap.ic_launcher,
            "Aceptar",
            acceptPendingIntent
        ).build()
        //CANCEL
        val cancelIntent: Intent = Intent(this, CancelReceiver::class.java)
        cancelIntent.putExtra("idClient", idClient)
        val cancelPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            this,
            NOTIFICATION_CODE,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelAction: Notification.Action = Notification.Action.Builder(
            R.mipmap.ic_launcher,
            "Rechazar",
            cancelPendingIntent
        ).build()
        val sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper: NotificationHelper = NotificationHelper(baseContext)
        val builder: Notification.Builder =
            notificationHelper.getNotificationAction(title, body, sound, acceptAction, cancelAction)
        notificationHelper.getManager().notify(2, builder.build())
    }
}
