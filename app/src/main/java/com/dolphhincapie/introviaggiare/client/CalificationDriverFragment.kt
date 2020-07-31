package com.dolphhincapie.introviaggiare.client

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.activities.MainActivity
import com.dolphhincapie.introviaggiare.model.ClientBooking
import com.dolphhincapie.introviaggiare.model.HistoryBooking
import com.dolphhincapie.introviaggiare.provider.ClientBookingProvider
import com.dolphhincapie.introviaggiare.provider.HistoryBokkingProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_calification_driver.*
import java.util.*

class CalificationDriverFragment : Fragment() {

    private var mClientBookingProvider: ClientBookingProvider =
        ClientBookingProvider()
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mHistoryBookingProvider: HistoryBokkingProvider =
        HistoryBokkingProvider()
    private lateinit var mHistoryBooking: HistoryBooking
    private var mCalification: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calification_driver, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rb_CalificationDriver.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, calificacion, b ->
                mCalification = calificacion
            }

        bt_CalificationDriver.setOnClickListener {
            calificate()
        }

        getClientBooking()

    }

    private fun getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuth.currentUser?.uid.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val clientBooking: ClientBooking? =
                            snapshot.getValue(ClientBooking::class.java)
                        tv_originCalificationClient.text = clientBooking?.origin.toString()
                        tv_destinationCalificationClient.text =
                            clientBooking?.destination.toString()
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
            mHistoryBooking.CalificationDriver = mCalification.toDouble()
            mHistoryBooking.timestamp = Date().time
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.idHistoryBooking)
                .addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            mHistoryBookingProvider.updateCalificationDriver(
                                mHistoryBooking.idHistoryBooking,
                                mCalification
                            ).addOnSuccessListener {
                                mClientBookingProvider.delete(mAuth.currentUser?.uid.toString())
                                Toast.makeText(
                                    requireContext(),
                                    "La calificacion se envio correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                            }
                        } else {
                            mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "La calificacion se envio correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                            }
                        }
                    }

                })

        } else {
            Toast.makeText(requireContext(), "Debes ingresar una Calificacion", Toast.LENGTH_SHORT)
                .show()
        }
    }
}