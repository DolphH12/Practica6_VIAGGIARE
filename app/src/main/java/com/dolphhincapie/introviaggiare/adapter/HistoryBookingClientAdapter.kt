package com.dolphhincapie.introviaggiare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.model.HistoryBooking
import com.dolphhincapie.introviaggiare.provider.DriverProvider
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_history_booking.view.*

class HistoryBookingClientAdapter(
    options: FirebaseRecyclerOptions<HistoryBooking>,
    private val onItemClickListener: OnItemClickListener
) : FirebaseRecyclerAdapter<HistoryBooking, HistoryBookingClientAdapter.ViewHolder>(options) {

    private var mDriverProvider: DriverProvider = DriverProvider()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_history_booking, parent, false)
        return ViewHolder(
            itemView,
            onItemClickListener
        )
    }


    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        historyBooking: HistoryBooking
    ) {

        val id: String = getRef(position).key.toString()
        holder.historyData(historyBooking, id)
    }

    class ViewHolder(itemView: View, var onItemClickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var mDriverProvider: DriverProvider = DriverProvider()
        private var historyBooking: HistoryBooking = HistoryBooking()
        private var mId: String = ""

        fun historyData(
            historyBooking: HistoryBooking,
            id: String
        ) {
            mId = id
            itemView.tv_originHistory.text = historyBooking.origin
            itemView.tv_destinationHistory.text = historyBooking.destination
            itemView.tv_calificationHistory.text = historyBooking.calificationClient.toString()
            mDriverProvider.getDriver(historyBooking.idDriver)
                .addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val name = snapshot.child("nombre").value.toString()
                            itemView.tv_nameHistory.text = name
                            if (snapshot.hasChild("image")) {
                                val image = snapshot.child("image").value.toString()
                                Picasso.get().load(image).into(itemView.iv_historyBooking)
                            }

                        }
                    }
                })
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onItemClickListener.onItemClick(historyBooking, mId)
        }

    }

    interface OnItemClickListener {
        fun onItemClick(historyBooking: HistoryBooking, id: String)
    }

}