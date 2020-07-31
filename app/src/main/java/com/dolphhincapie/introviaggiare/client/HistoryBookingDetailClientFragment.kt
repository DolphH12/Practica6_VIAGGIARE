package com.dolphhincapie.introviaggiare.client

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.model.HistoryBooking
import com.dolphhincapie.introviaggiare.provider.DriverProvider
import com.dolphhincapie.introviaggiare.provider.HistoryBokkingProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_history_booking_detail_client.*

class HistoryBookingDetailClientFragment : Fragment() {

    private var mHistoryBookingProvider: HistoryBokkingProvider = HistoryBokkingProvider()
    private var mExtraId: String = ""
    private var mDriverProvider: DriverProvider = DriverProvider()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_booking_detail_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mExtraId = arguments?.getString("idHistoryBooking").toString()
        Log.d("mExtraId", mExtraId)

        getHistoryBooking()

        iv_back.setOnClickListener {
            activity?.onBackPressed()
        }

    }

    private fun getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val historyBooking: HistoryBooking? =
                            snapshot.getValue(HistoryBooking::class.java)
                        tv_originDetailClient.text = historyBooking!!.origin
                        tv_destinationDetailClient.text = historyBooking.destination
                        tv_calificationDetailClient.text =
                            "Tu Calificacion: " + historyBooking.CalificationDriver.toString()
                        if (snapshot.hasChild("calificationClient")) {
                            rb_calificationDetailClient.rating =
                                historyBooking.calificationClient.toFloat()
                        }
                        mDriverProvider.getDriver(historyBooking.idDriver)
                            .addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {
                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val name = snapshot.child("nombre").value.toString()
                                        tv_nameDetailClient.text = name.toUpperCase()
                                        if (snapshot.hasChild("image")) {
                                            val image = snapshot.child("image").value.toString()
                                            if (image.isNotEmpty()) {
                                                Picasso.get().load(image)
                                                    .into(iv_driverDetailClient)
                                            }
                                        }

                                    }
                                }
                            })
                    }
                }

            })


    }

}