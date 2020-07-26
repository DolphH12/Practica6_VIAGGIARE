package com.dolphhincapie.introviaggiare

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_choose.*

class ChooseActivity : AppCompatActivity() {

    private lateinit var mPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)

        mPref = applicationContext.getSharedPreferences("typeUser", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = mPref.edit()

        bt_cliente.setOnClickListener {
            editor.putString("user", "client")
            editor.apply()
            goToLoginActivity()
        }

        bt_conductor.setOnClickListener {
            editor.putString("user", "driver")
            editor.apply()
            goToLoginActivity()
        }

    }

    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}