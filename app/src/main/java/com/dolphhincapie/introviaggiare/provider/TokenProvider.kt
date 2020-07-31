package com.dolphhincapie.introviaggiare.provider

import com.dolphhincapie.introviaggiare.model.Token
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId


class TokenProvider {

    private var mDatabase: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("Tokens")

    fun create(idUser: String?) {
        if (idUser == null) return
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener { instanceIdResult ->
                val token =
                    Token(instanceIdResult.token)
                mDatabase.child(idUser.toString()).setValue(token)
            }
    }

    fun getToken(idUser: String): DatabaseReference {
        return mDatabase.child(idUser)
    }
}