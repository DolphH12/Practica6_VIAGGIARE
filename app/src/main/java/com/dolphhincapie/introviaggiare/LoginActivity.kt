package com.dolphhincapie.introviaggiare

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {

    var usuario = ""
    var  contrasena = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        val datosRecibidos = intent.extras
        if(datosRecibidos != null){
            usuario = datosRecibidos.getString("usuario").toString()
            contrasena = datosRecibidos.getString("contraseña").toString()
        }


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bt_passregister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivityForResult(intent,1234)
        }

        bt_ingresar.setOnClickListener {
            if (te_usuario.text.toString() == usuario && te_contrasena.text.toString() == contrasena){
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("usuario", te_usuario.text.toString())
                intent.putExtra("contraseña", te_contrasena.text.toString())
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Usuario o Contraseña incorrecta", Toast.LENGTH_LONG).show()
            }
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234 && resultCode == Activity.RESULT_OK){
            usuario = data?.extras?.getString("usuario").toString()
            contrasena = data?.extras?.getString("contraseña").toString()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}