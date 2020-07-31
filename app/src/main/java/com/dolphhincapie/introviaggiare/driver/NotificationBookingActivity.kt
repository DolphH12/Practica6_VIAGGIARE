package com.dolphhincapie.introviaggiare.driver

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.activities.MainDriverActivity
import com.dolphhincapie.introviaggiare.provider.ClientBookingProvider
import com.dolphhincapie.introviaggiare.provider.GeofireProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_notification_booking.*
import kotlinx.coroutines.Runnable

class NotificationBookingActivity : AppCompatActivity() {

    private var mClientBookingProvider: ClientBookingProvider =
        ClientBookingProvider()
    private var mGeofireProvider: GeofireProvider =
        GeofireProvider("active_drivers")
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mExtraIdClient: String = ""
    private var mExtraOrigin: String = ""
    private var mExtraDestination: String = ""
    private var mExtraMin: String = ""
    private var mExtraDistance: String = ""
    private var mExtraPrice: String = ""
    private var mListener: ValueEventListener? = null

    private var mediaPlayer: MediaPlayer? = null


    private var mHandler: Handler? = null
    private var mCounter: Int = 10
    private var runnable: Runnable = Runnable {
        mCounter -= 1
        tv_counter.text = mCounter.toString()
        if (mCounter > 0) {
            initTimer()
        } else {
            cancelBooking()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_booking)

        mExtraIdClient = intent.getStringExtra("idClient").toString()
        mExtraOrigin = intent.getStringExtra("origin").toString()
        mExtraDestination = intent.getStringExtra("destination").toString()
        mExtraMin = intent.getStringExtra("time").toString()
        mExtraDistance = intent.getStringExtra("km").toString()
        mExtraPrice = intent.getStringExtra("precio").toString()

        tv_Ocliente.text = mExtraOrigin
        tv_Dcliente.text = mExtraDestination
        tv_timeClient.text = mExtraMin
        tv_distanceClient.text = mExtraDistance
        tv_valor.text = "$mExtraPrice$ COP"

        mediaPlayer = MediaPlayer.create(this, R.raw.ring)
        mediaPlayer?.isLooping = true

        initTimer()
        checkIfClientCancelBooking()

        bt_acceptBooking.setOnClickListener {
            acceptBooking()
        }

        bt_cancelBooking.setOnClickListener {
            cancelBooking()
        }

    }

    private fun checkIfClientCancelBooking() {
        mListener = mClientBookingProvider.getClientBooking(mExtraIdClient)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(
                            this@NotificationBookingActivity,
                            "El cliente cancelo el viaje",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (mHandler != null) mHandler?.removeCallbacks(runnable)
                        startActivity(
                            Intent(
                                this@NotificationBookingActivity,
                                MainDriverActivity::class.java
                            )
                        )
                        finish()
                    }
                }

            })
    }

    private fun initTimer() {
        mHandler = Handler()
        mHandler?.postDelayed(runnable, 1000)
    }

    private fun acceptBooking() {
        if (mHandler != null) mHandler?.removeCallbacks(runnable)
        mClientBookingProvider.updateStatus(mExtraIdClient, "accept")
        mGeofireProvider.removeLocation(mAuth.currentUser?.uid)

        val manager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)

        val intent2: Intent = Intent(this, MapDriverBookingActivity::class.java)
        intent2.putExtra("ban", true)
        intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent2.action = Intent.ACTION_RUN
        intent2.putExtra("idClient", mExtraIdClient)
        startActivity(intent2)
    }

    private fun cancelBooking() {
        if (mHandler != null) mHandler?.removeCallbacks(runnable)
        mClientBookingProvider.updateStatus(mExtraIdClient, "cancel")

        val manager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)
        val intent = Intent(this, MainDriverActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlayer != null) {
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mListener != null) {
            mClientBookingProvider.getStatus(mExtraIdClient).removeEventListener(mListener!!)
        }
        if (mHandler != null) mHandler?.removeCallbacks(runnable)
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.release()
            }
        }
    }

}