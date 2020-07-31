package com.dolphhincapie.introviaggiare.activities

import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.dolphhincapie.introviaggiare.R
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.concurrent.timerTask

class SplashActivity : AppCompatActivity() {

    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_splash)

        val timer = Timer()
        timer.schedule(
            timerTask {
                goToChooseActivity()
            }, 1000
        )


    }

    private fun goToChooseActivity() {
        val intent = Intent(this, ChooseActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}