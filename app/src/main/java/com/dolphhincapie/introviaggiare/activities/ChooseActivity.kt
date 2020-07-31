package com.dolphhincapie.introviaggiare.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.provider.ClientProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_choose.*

class ChooseActivity : AppCompatActivity() {

    private lateinit var mPref: SharedPreferences
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mClientPrvider: ClientProvider = ClientProvider()
    private var ban: Int = 2

    override fun onStart() {
        super.onStart()
        pb_choose.visibility = View.VISIBLE
        val user = mAuth.currentUser
        if (user != null) {
            mClientPrvider.getClient(user.uid.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            pb_choose.visibility = View.GONE
                            goToMainActivity()
                        } else {
                            pb_choose.visibility = View.GONE
                            goToMainDriverActivity()
                        }
                    }

                })
        } else {
            pb_choose.visibility = View.GONE
        }


    }

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

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToMainDriverActivity() {
        startActivity(Intent(this, MainDriverActivity::class.java))
        finish()
    }

}