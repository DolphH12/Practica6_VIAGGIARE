package com.dolphhincapie.introviaggiare.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.model.PlacesDeter
import com.dolphhincapie.introviaggiare.model.Users
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_user.*

class UserFragment : Fragment() {

    //private lateinit var dashboardViewModel: DashboardViewModel
    var idUserFirebase: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_user, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ocultar_agregardir()

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("usuarios")

        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Users::class.java)
                    tv_nombre.text = user?.nombre
                    tv_correo.text = user?.correo
                    idUserFirebase = user?.id

                }
            }

        }
        myRef.addListenerForSingleValueEvent(postListener)

        bt_aggdir.setOnClickListener {
            mostrar_agregardir()
        }

        bt_ingresardir.setOnClickListener {
            val lugar = et_lugar.text.toString()
            val direccion = et_direccion.text.toString()
            crearLugaresFrecuentes(lugar, direccion)
            ocultar_agregardir()
        }


    }

    private fun mostrar_agregardir() {
        et_lugar.visibility = View.VISIBLE
        et_direccion.visibility = View.VISIBLE
        bt_ingresardir.visibility = View.VISIBLE
    }

    private fun ocultar_agregardir() {
        et_lugar.visibility = View.GONE
        et_direccion.visibility = View.GONE
        bt_ingresardir.visibility = View.GONE
    }

    private fun crearLugaresFrecuentes(
        lugar: String,
        direccion: String
    ) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: DatabaseReference = database.getReference(idUserFirebase.toString())
        val id: String? = myRef.push().key

        val lugar_frecuente = PlacesDeter(
            id,
            lugar,
            direccion
        )
        myRef.child(id!!).setValue(lugar_frecuente)
    }

}