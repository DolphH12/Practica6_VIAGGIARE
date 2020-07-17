package com.dolphhincapie.introviaggiare

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

            mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        goToMainActivity()
                    } else {
                        Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                    }
                }
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
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