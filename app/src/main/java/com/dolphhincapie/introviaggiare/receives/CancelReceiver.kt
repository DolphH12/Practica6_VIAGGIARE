package com.dolphhincapie.introviaggiare.receives

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dolphhincapie.introviaggiare.activities.MainDriverActivity
import com.dolphhincapie.introviaggiare.provider.ClientBookingProvider

class CancelReceiver : BroadcastReceiver() {

    private var mClientBookingProvider: ClientBookingProvider =
        ClientBookingProvider()

    override fun onReceive(context: Context?, intent: Intent?) {
        val idClient: String = intent?.extras?.getString("idClient").toString()
        mClientBookingProvider.updateStatus(idClient, "cancel")

        val manager: NotificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)
        val intent2: Intent = Intent(context, MainDriverActivity::class.java)
        context.startActivity(intent2)
    }
}