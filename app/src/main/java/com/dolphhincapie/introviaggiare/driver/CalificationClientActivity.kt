package com.dolphhincapie.introviaggiare.driver

import android.content.Intent
import android.os.Bundle
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.activities.MainDriverActivity
import com.dolphhincapie.introviaggiare.model.ClientBooking
import com.dolphhincapie.introviaggiare.model.HistoryBooking
import com.dolphhincapie.introviaggiare.provider.ClientBookingProvider
import com.dolphhincapie.introviaggiare.provider.HistoryBokkingProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_calification_client.*
import java.util.*

class CalificationClientActivity : AppCompatActivity() {

    private var mClientBookingProvider: ClientBookingProvider =
        ClientBookingProvider()
    private var mExtraClientId: String = ""
    private var mExtraPrice: String = ""
    private var mHistoryBookingProvider: HistoryBokkingProvider =
        HistoryBokkingProvider()
    private lateinit var mHistoryBooking: HistoryBooking
    private var mCalification: Float = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calification_client)

        mExtraClientId = intent.getStringExtra("idClient").toString()

        rb_CalificationClient.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, calificacion, b ->
                mCalification = calificacion
            }

        bt_CalificationClient.setOnClickListener {
            calificate()
        }

        getClientBooking()

    }

    private fun getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val clientBooking: ClientBooking? =
                            snapshot.getValue(ClientBooking::class.java)
                        tv_originCalificationDrive.text = clientBooking?.origin.toString()
                        tv_destinationCalificationDrive.text = clientBooking?.destination.toString()
                        tv_pricetoDriver.text = "${clientBooking?.precio.toString()}$ COP"
                        mHistoryBooking = HistoryBooking(
                            clientBooking!!.idHistoryBooking,
                            clientBooking.idClient,
                            clientBooking.idDriver,
                            clientBooking.destination,
                            clientBooking.origin,
                            clientBooking.time,
                            clientBooking.km,
                            clientBooking.status,
                            clientBooking.originLat,
                            clientBooking.originLng,
                            clientBooking.destinationLat,
                            clientBooking.destinationLng
                        )
                    }
                }

            })
    }

    private fun calificate() {
        if (mCalification > 0) {
            mHistoryBooking.calificationClient = mCalification.toDouble()
            mHistoryBooking.timestamp = Date().time
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.idHistoryBooking)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            mHistoryBookingProvider.updateCalificationClient(
                                mHistoryBooking.idHistoryBooking,
                                mCalification
                            ).addOnSuccessListener {
                                mClientBookingProvider.delete(mExtraClientId)
                                Toast.makeText(
                                    this@CalificationClientActivity,
                                    "La calificacion se envio correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(
                                    Intent(
                                        this@CalificationClientActivity,
                                        MainDriverActivity::class.java
                                    )
                                )
                                finish()
                            }
                        } else {
                            mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener {
                                Toast.makeText(
                                    this@CalificationClientActivity,
                                    "La calificacion se envio correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(
                                    Intent(
                                        this@CalificationClientActivity,
                                        MainDriverActivity::class.java
                                    )
                                )
                                finish()
                            }
                        }
                    }

                })

        } else {
            Toast.makeText(this, "Debes ingresar una Calificacion", Toast.LENGTH_SHORT).show()
        }
    }
}