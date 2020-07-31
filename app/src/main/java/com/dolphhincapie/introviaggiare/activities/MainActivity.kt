package com.dolphhincapie.introviaggiare.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dolphhincapie.introviaggiare.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    var usuario =""
    var contrasena = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val datosRecibidos = intent.extras

        usuario = datosRecibidos?.getString("usuario").toString()
        contrasena = datosRecibidos?.getString("contraseña").toString()

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_historyBookingClient -> navView.visibility = View.GONE
                R.id.navigation_historyBookingDetailClient -> navView.visibility = View.GONE
                R.id.navigation_detailRequest -> navView.visibility = View.GONE
                R.id.navigation_mapClientBooking -> navView.visibility = View.GONE
                R.id.navigation_calificationDriver -> navView.visibility = View.GONE
                else -> navView.visibility = View.VISIBLE
            }
            Log.d("destination", destination.id.toString())
        }

    }

    private fun goToChooseActivity() {
        val intent = Intent(this, ChooseActivity::class.java)
        startActivity(intent)
        finish()
    }

}