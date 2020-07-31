package com.dolphhincapie.introviaggiare.receives

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dolphhincapie.introviaggiare.driver.MapDriverBookingActivity
import com.dolphhincapie.introviaggiare.provider.ClientBookingProvider
import com.dolphhincapie.introviaggiare.provider.GeofireProvider
import com.google.firebase.auth.FirebaseAuth


class AcceptReceiver : BroadcastReceiver() {

    private var mClientBookingProvider: ClientBookingProvider =
        ClientBookingProvider()
    private var mGeofireProvider: GeofireProvider =
        GeofireProvider("active_drivers")
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onReceive(context: Context?, intent: Intent?) {
        val idClient: String = intent?.extras?.getString("idClient").toString()
        mClientBookingProvider.updateStatus(idClient, "accept")
        mGeofireProvider.removeLocation(mAuth.currentUser?.uid)

        val manager: NotificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)

        val intent2: Intent = Intent(context, MapDriverBookingActivity::class.java)
        intent2.putExtra("ban", true)
        intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent2.action = Intent.ACTION_RUN
        intent2.putExtra("idClient", idClient)
        context.startActivity(intent2)
    }

}