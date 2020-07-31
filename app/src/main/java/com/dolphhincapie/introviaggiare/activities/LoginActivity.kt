package com.dolphhincapie.introviaggiare.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.provider.ClientProvider
import com.dolphhincapie.introviaggiare.provider.DriverProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    var user = ""
    var password = ""
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var ban: Int = 2
    private var mDriverProvider: DriverProvider = DriverProvider()
    private var mClientPrvider: ClientProvider = ClientProvider()
    private lateinit var mPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mPref = applicationContext.getSharedPreferences("typeUser", Context.MODE_PRIVATE)

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
                            val user: String = mPref.getString("user", "").toString()
                            if (user == "client") {
                                val client = mAuth.currentUser
                                mClientPrvider.getClient(client?.uid.toString())
                                    .addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onCancelled(error: DatabaseError) {
                                        }

                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                progressBar.visibility = View.GONE
                                                goToMainActivity()
                                            } else {
                                                FirebaseAuth.getInstance().signOut()
                                                progressBar.visibility = View.GONE
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Su cuenta no es de cliente",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                goToChooseActivity()
                                            }
                                        }

                                    })
                            } else {
                                val driver = mAuth.currentUser
                                mDriverProvider.getDriver(driver?.uid.toString())
                                    .addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onCancelled(error: DatabaseError) {
                                        }

                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                progressBar.visibility = View.GONE
                                                goToMainDriverActivity()
                                            } else {
                                                FirebaseAuth.getInstance().signOut()
                                                progressBar.visibility = View.GONE
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Su cuenta no es de Conductor",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                goToChooseActivity()
                                            }
                                        }

                                    })
                            }
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

    private fun goToMainDriverActivity() {
        startActivity(Intent(this, MainDriverActivity::class.java))
        finish()
    }

    private fun goToResgistro() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun goToChooseActivity() {
        val intent = Intent(this, ChooseActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}