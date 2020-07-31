package com.dolphhincapie.introviaggiare.provider

import com.dolphhincapie.introviaggiare.model.Drivers
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DriverProvider {

    private var mDatabase: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Usuarios").child("Conductores")

    fun create(
        id: String,
        nombre: String,
        correo: String,
        ciudadNacimiento: String,
        vehiculo: String,
        placa: String
    ): Task<Void> {
        val conductor = Drivers(
            id,
            nombre,
            correo,
            ciudadNacimiento,
            vehiculo,
            placa
        )
        return mDatabase.child(id).setValue(conductor)
    }

    fun update(
        id: String,
        nombre: String,
        image: String,
        vehiculo: String,
        placa: String
    ): Task<Void> {
        val conductor = Drivers(
            id = id,
            nombre = nombre,
            image = image,
            auto = vehiculo,
            placa = placa
        )
        val map: MutableMap<String, Any> = HashMap()
        map["id"] = conductor.id.toString()
        map["nombre"] = conductor.nombre
        map["image"] = conductor.image
        map["auto"] = conductor.auto
        map["placa"] = conductor.placa
        return mDatabase.child(id).updateChildren(map)
    }

    fun getDriver(idDriver: String): DatabaseReference {
        return mDatabase.child(idDriver)
    }

}