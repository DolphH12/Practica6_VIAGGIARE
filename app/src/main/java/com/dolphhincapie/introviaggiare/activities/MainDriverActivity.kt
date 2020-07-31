package com.dolphhincapie.introviaggiare.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.driver.MapsDriverFragment
import com.dolphhincapie.introviaggiare.provider.GeofireProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainDriverActivity : AppCompatActivity() {

    private var mGeofireProvider: GeofireProvider =
        GeofireProvider("active_drivers")
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val active: MapsDriverFragment =
        MapsDriverFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_driver)

        val navView: BottomNavigationView = findViewById(R.id.nav_view_driver)

        val navController = findNavController(R.id.nav_host_fragment_driver)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_historyBookingDriver -> navView.visibility = View.GONE
                R.id.navigation_historyBookingDetailDriver -> navView.visibility = View.GONE
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