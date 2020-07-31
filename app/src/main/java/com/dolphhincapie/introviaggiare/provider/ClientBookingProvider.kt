package com.dolphhincapie.introviaggiare.provider

import com.dolphhincapie.introviaggiare.model.ClientBooking
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ClientBookingProvider {

    private var mDatabase: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("ClientBooking")

    fun create(clientBooking: ClientBooking): Task<Void> {
        return mDatabase.child(clientBooking.idClient).setValue(clientBooking)
    }

    fun updateStatus(idClient: String, status: String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["status"] = status
        return mDatabase.child(idClient).updateChildren(map)
    }

    fun updateIdHistoryBook(idClientBooking: String): Task<Void> {
        val idPush = mDatabase.push().key.toString()
        val map: MutableMap<String, Any> = HashMap()
        map["idHistoryBooking"] = idPush
        return mDatabase.child(idClientBooking).updateChildren(map)
    }

    fun getStatus(idClientBooking: String): DatabaseReference {
        return mDatabase.child(idClientBooking).child("status")
    }

    fun getClientBooking(idClientBooking: String): DatabaseReference {
        return mDatabase.child(idClientBooking)
    }

    fun delete(idClientBooking: String): Task<Void> {
        return mDatabase.child(idClientBooking).removeValue()
    }


}