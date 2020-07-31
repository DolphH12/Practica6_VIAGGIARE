package com.dolphhincapie.introviaggiare.provider

import com.dolphhincapie.introviaggiare.model.HistoryBooking
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class HistoryBokkingProvider {

    private var mDatabase: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("HistoryBooking")

    fun create(historyBooking: HistoryBooking): Task<Void> {
        return mDatabase.child(historyBooking.idHistoryBooking).setValue(historyBooking)
    }

    fun updateCalificationClient(idHistoryBooking: String, calificationClient: Float): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["calificationClient"] = calificationClient.toDouble()
        return mDatabase.child(idHistoryBooking).updateChildren(map)
    }

    fun updateCalificationDriver(idHistoryBooking: String, calificationDriver: Float): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["calificationDriver"] = calificationDriver.toDouble()
        return mDatabase.child(idHistoryBooking).updateChildren(map)
    }

    fun getHistoryBooking(idHistoryBooking: String): DatabaseReference {
        return mDatabase.child(idHistoryBooking)
    }

}