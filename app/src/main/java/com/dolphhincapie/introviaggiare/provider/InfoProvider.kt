package com.dolphhincapie.introviaggiare.provider

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class InfoProvider {

    private var mDatabase: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("info")

    fun getInfo(): DatabaseReference {
        return mDatabase
    }

}