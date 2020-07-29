package com.dolphhincapie.introviaggiare

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dolphhincapie.introviaggiare.fragments.MapsDriverFragment
import com.dolphhincapie.introviaggiare.model.GeofireProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainDriverActivity : AppCompatActivity() {

    private var mGeofireProvider: GeofireProvider = GeofireProvider()
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val active: MapsDriverFragment = MapsDriverFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_driver)
        val navView: BottomNavigationView = findViewById(R.id.nav_view_driver)

        val navController = findNavController(R.id.nav_host_fragment_driver)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_maps_driver,
                R.id.navigation_places_driver,
                R.id.navigation_user_driver
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.m_cerrar) {
            val user = mAuth.currentUser
            if (user != null) {
                active.setBan()
                mGeofireProvider.removeLocation(user.uid)
            }
            FirebaseAuth.getInstance().signOut()
            goToLoginActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}