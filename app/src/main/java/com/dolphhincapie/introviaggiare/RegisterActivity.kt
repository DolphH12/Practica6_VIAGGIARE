package com.dolphhincapie.introviaggiare

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dolphhincapie.introviaggiare.model.Drivers
import com.dolphhincapie.introviaggiare.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {

    private lateinit var mPref: SharedPreferences
    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        mPref = applicationContext.getSharedPreferences("typeUser", Context.MODE_PRIVATE)

        val selectedUser = mPref.getString("user", "").toString()
        if (selectedUser.equals("client")) {
            et_auto.visibility = View.GONE
            et_placa.visibility = View.GONE
            bt_guardar.setOnClickListener {
                registerClient()
            }
        } else if (selectedUser.equals("driver")) {
            bt_guardar.setOnClickListener {
                registerDriver()
            }
        }


    }

    private fun registerDriver() {
        val nombre = et_nombre.text.toString()
        val correo = et_correo.text.toString()
        val contrasena = et_contrasena.text.toString()
        val repContra = et_repitacontrasena.text.toString()
        val ciudadNacimiento = s_ciudades.selectedItem.toString()
        val vehiculo = et_auto.text.toString()
        val placa = et_placa.text.toString()


        if (nombre.isEmpty()) {
            et_nombre.error = "Campo nombre vacio"
            //Toast.makeText(this, "Campo Nombre Vacio", Toast.LENGTH_LONG).show()
        } else if (correo.isEmpty() || "@" !in correo) {
            et_correo.error = "Campo correo vacio"
            //Toast.makeText(this, "Campo Correo Invalido", Toast.LENGTH_LONG).show()
        } else if (contrasena.isEmpty()) {
            et_contrasena.error = "Campo contraseña vacio"
            //Toast.makeText(this, "Campo Contraseña Vacio", Toast.LENGTH_LONG).show()
        } else if (repContra.isEmpty()) {
            et_repitacontrasena.error = "Campo de repetir contraseña vacio"
            //Toast.makeText(this, "Campo Rep Contraseña Vacio", Toast.LENGTH_LONG).show()
        } else if (vehiculo.isEmpty()) {
            et_repitacontrasena.error = "Campo de Vehiculo vacio"
            //Toast.makeText(this, "Campo Rep Contraseña Vacio", Toast.LENGTH_LONG).show()
        } else if (placa.isEmpty()) {
            et_repitacontrasena.error = "Campo de Placa vacio"
            //Toast.makeText(this, "Campo Rep Contraseña Vacio", Toast.LENGTH_LONG).show()
        } else {
            if (contrasena == repContra) {
                mAuth.createUserWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener(
                        this
                    ) { task ->
                        if (task.isSuccessful) {
                            crearUsuarioeEnBaseDeDatos(
                                nombre,
                                correo,
                                ciudadNacimiento,
                                vehiculo,
                                placa
                            )
                            Toast.makeText(
                                this, "Registro Exítoso",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackPressed()
                        } else {
                            val mensaje = task.exception!!.message.toString()
                            if ("already" in mensaje) {
                                Toast.makeText(
                                    this, "El correo ya se encuentra registrado",
                                    Toast.LENGTH_SHORT
                                ).show()
                                et_correo.error = "Campo Inválido"
                            } else if ("formatted" in mensaje) {
                                Toast.makeText(
                                    this, "El formato del correo es inválido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                et_correo.error = "Campo Inválido"
                            } else {
                                Toast.makeText(
                                    this, "El formato del correo es inválido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                et_contrasena.error = "Campo Inválido"
                                et_repitacontrasena.error = "Campo Inválido"
                            }
                        }

                        // ...
                    }

            } else {
                et_contrasena.error
                et_repitacontrasena.error
                Toast.makeText(this, "Contraseñas diferentes", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun registerClient() {
        val nombre = et_nombre.text.toString()
        val correo = et_correo.text.toString()
        val contrasena = et_contrasena.text.toString()
        val repContra = et_repitacontrasena.text.toString()
        val ciudadNacimiento = s_ciudades.selectedItem.toString()
        val vehiculo = et_auto.text.toString()
        val placa = et_placa.text.toString()


        if (nombre.isEmpty()) {
            et_nombre.error = "Campo nombre vacio"
            //Toast.makeText(this, "Campo Nombre Vacio", Toast.LENGTH_LONG).show()
        } else if (correo.isEmpty() || "@" !in correo) {
            et_correo.error = "Campo correo vacio"
            //Toast.makeText(this, "Campo Correo Invalido", Toast.LENGTH_LONG).show()
        } else if (contrasena.isEmpty()) {
            et_contrasena.error = "Campo contraseña vacio"
            //Toast.makeText(this, "Campo Contraseña Vacio", Toast.LENGTH_LONG).show()
        } else if (repContra.isEmpty()) {
            et_repitacontrasena.error = "Campo de repetir contraseña vacio"
            //Toast.makeText(this, "Campo Rep Contraseña Vacio", Toast.LENGTH_LONG).show()
        } else {
            if (contrasena == repContra) {
                mAuth.createUserWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener(
                        this
                    ) { task ->
                        if (task.isSuccessful) {
                            crearUsuarioeEnBaseDeDatos(
                                nombre,
                                correo,
                                ciudadNacimiento,
                                vehiculo,
                                placa
                            )
                            Toast.makeText(
                                this, "Registro Exítoso",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackPressed()
                        } else {
                            val mensaje = task.exception!!.message.toString()
                            if ("already" in mensaje) {
                                Toast.makeText(
                                    this, "El correo ya se encuentra registrado",
                                    Toast.LENGTH_SHORT
                                ).show()
                                et_correo.error = "Campo Inválido"
                            } else if ("formatted" in mensaje) {
                                Toast.makeText(
                                    this, "El formato del correo es inválido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                et_correo.error = "Campo Inválido"
                            } else {
                                Toast.makeText(
                                    this, "El formato del correo es inválido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                et_contrasena.error = "Campo Inválido"
                                et_repitacontrasena.error = "Campo Inválido"
                            }
                        }

                        // ...
                    }

            } else {
                et_contrasena.error
                et_repitacontrasena.error
                Toast.makeText(this, "Contraseñas diferentes", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun crearUsuarioeEnBaseDeDatos(
        nombre: String,
        correo: String,
        ciudadNacimiento: String,
        vehiculo: String,
        placa: String
    ) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference("Usuarios")
        val id = mAuth.currentUser?.uid.toString()
        val selectedUser = mPref.getString("user", "").toString()
        if (selectedUser.equals("client")) {
            val usuario = Users(
                id,
                nombre,
                correo,
                ciudadNacimiento
            )
            myRef.child("Clientes").child(id).setValue(usuario)
        } else if (selectedUser.equals("driver")) {
            val conductor = Drivers(
                id,
                nombre,
                correo,
                ciudadNacimiento,
                vehiculo,
                placa
            )
            myRef.child("Conductores").child(id).setValue(conductor)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LoginActivity::class.java))
    }

}