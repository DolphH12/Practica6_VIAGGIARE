package com.dolphhincapie.introviaggiare

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*

class LoginActivity : AppCompatActivity() {

    var user = ""
    var  password = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val datosRecibidos = intent.extras

        user = datosRecibidos?.getString("usuario").toString()
        password = datosRecibidos?.getString("contraseña").toString()

        bt_passregister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivityForResult(intent,1234)
        }

        bt_ingresar.setOnClickListener {
            if (te_usuario.text.toString() == user && te_contrasena.text.toString() == password){
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("usuario", te_usuario.text.toString())
                intent.putExtra("contraseña", te_contrasena.text.toString())
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this, "Usuario o Contraseña incorrecta", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234 && resultCode == Activity.RESULT_OK){
            user = data?.extras?.getString("usuario").toString()
            password = data?.extras?.getString("contraseña").toString()
        }
    }

}