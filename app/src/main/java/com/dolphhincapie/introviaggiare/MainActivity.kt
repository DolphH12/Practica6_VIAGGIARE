package com.dolphhincapie.introviaggiare

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var datosRecibidos = intent.extras

        tv_user.text = datosRecibidos?.getString("usuario")
        tv_contra.text = datosRecibidos?.getString("contraseña")




    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.m_cerrar) {
            startActivity(Intent(this, LoginActivity::class.java))
            intent.putExtra("usuario", tv_user.text.toString())
            intent.putExtra("contraseña", tv_contra.text.toString())
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}