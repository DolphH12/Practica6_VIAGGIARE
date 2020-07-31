package com.dolphhincapie.introviaggiare.provider

import com.dolphhincapie.introviaggiare.model.Users
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ClientProvider {

    private var mDatabase: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Usuarios").child("Clientes")

    fun create(
        id: String,
        nombre: String,
        correo: String,
        ciudadNacimiento: String
    ): Task<Void> {
        val usuario = Users(
            id,
            nombre,
            correo,
            ciudadNacimiento
        )
        return mDatabase.child(id).setValue(usuario)
    }

    fun update(
        id: String,
        nombre: String,
        image: String
    ): Task<Void> {
        val usuario = Users(
            id = id,
            nombre = nombre,
            image = image
        )
        val map: MutableMap<String, Any> = HashMap()
        map["id"] = usuario.id.toString()
        map["nombre"] = usuario.nombre
        map["image"] = usuario.image
        return mDatabase.child(id).updateChildren(map)
    }

    fun getClient(idClient: String): DatabaseReference {
        return mDatabase.child(idClient)
    }

}