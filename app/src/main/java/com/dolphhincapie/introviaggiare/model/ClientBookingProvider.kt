package com.dolphhincapie.introviaggiare.model

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ClientBookingProvider {

    private var mDatabase: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("ClientBooking")

    fun create(clientBooking: ClientBooking): Task<Void> {
        return mDatabase.child(clientBooking.idClient).setValue(clientBooking)
    }


}