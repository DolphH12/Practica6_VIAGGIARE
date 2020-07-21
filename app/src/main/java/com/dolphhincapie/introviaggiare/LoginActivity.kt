package com.dolphhincapie.introviaggiare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    var user = ""
    var password = ""
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser
        if (user != null)
            goToMainActivity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bt_passregister.setOnClickListener {
            goToResgistro()
            finish()
        }

        bt_ingresar.setOnClickListener {
            val correo = te_usuario.text.toString()
            val contrasena = te_contrasena.text.toString()
            progressBar.visibility = View.VISIBLE
            if (correo.isNullOrEmpty()) {
                te_usuario.error = "Campo Vacio"
            } else if (contrasena.isNullOrEmpty()) {
                te_contrasena.error = "Campo vacio"
            } else {
                mAuth.signInWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener(
                        this
                    ) { task ->
                        if (task.isSuccessful) {
                            progressBar.visibility = View.GONE
                            goToMainActivity()
                        } else {
                            val mensaje = task.exception!!.message.toString()
                            if ("password" in mensaje) {
                                Toast.makeText(
                                    this, "Contraseña incorrecta",
                                    Toast.LENGTH_LONG
                                ).show()
                                te_contrasena.error = "Campo Incorrecto"
                            } else {
                                Toast.makeText(
                                    this, "Correo Inválido o no Registrado",
                                    Toast.LENGTH_LONG
                                ).show()
                                te_usuario.error = "Campo Incorrecto"
                            }
                            progressBar.visibility = View.GONE
                            Log.w("TAG", "signInWithEmail:failure", task.exception)
                        }
                    }
            }
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToResgistro() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}