package com.dolphhincapie.introviaggiare.client

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.adapter.HistoryBookingClientAdapter
import com.dolphhincapie.introviaggiare.model.HistoryBooking
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import kotlinx.android.synthetic.main.fragment_history_booking_client.*


class HistoryBookingClientFragment : Fragment(), HistoryBookingClientAdapter.OnItemClickListener {

    private lateinit var mAdapter: HistoryBookingClientAdapter
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_booking_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(requireContext())
        rv_historyBooking.layoutManager = linearLayoutManager

        iv_back.setOnClickListener {
            activity?.onBackPressed()
        }

    }

    override fun onStart() {
        super.onStart()
        val query: Query = FirebaseDatabase.getInstance().reference
            .child("HistoryBooking")
            .orderByChild("idClient")
            .equalTo(mAuth.currentUser?.uid.toString())
        val options: FirebaseRecyclerOptions<HistoryBooking> =
            FirebaseRecyclerOptions.Builder<HistoryBooking>()
                .setQuery(query, HistoryBooking::class.java)
                .build()
        mAdapter = HistoryBookingClientAdapter(options, this)

        rv_historyBooking.adapter = mAdapter
        mAdapter.startListening()

    }

    override fun onStop() {
        super.onStop()
        mAdapter.startListening()
    }

    override fun onItemClick(historyBooking: HistoryBooking, id: String) {
        val bundle: Bundle = Bundle()
        Log.d("id", id)
        bundle.putString("idHistoryBooking", id)
        findNavController().navigate(
            R.id.action_navigation_historyBookingClient_to_navigation_historyBookingDetailClient,
            bundle
        )

    }

}