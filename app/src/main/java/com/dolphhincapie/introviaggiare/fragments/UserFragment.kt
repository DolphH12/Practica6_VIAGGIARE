package com.dolphhincapie.introviaggiare.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.model.FavoritosRVAdapter
import com.dolphhincapie.introviaggiare.model.PlacesDeter
import com.dolphhincapie.introviaggiare.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_user.*

class UserFragment : Fragment() {

    //private lateinit var dashboardViewModel: DashboardViewModel
    var idUserFirebase: String? = ""
    private var favoritosList: MutableList<PlacesDeter> = mutableListOf()
    private lateinit var favoritosAdapter: FavoritosRVAdapter
    private lateinit var myRef: DatabaseReference

    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val user: FirebaseUser? = mAuth.currentUser
    val correo = user?.uid.toString()

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

        cargaDatosUser()

        bt_aggdir.setOnClickListener {
            mostrar_agregardir()
        }

        bt_ingresardir.setOnClickListener {
            val lugar = et_lugar.text.toString()
            val direccion = et_direccion.text.toString()
            if (lugar.isNullOrEmpty()) {
                et_lugar.error = "Campo vacio"
            } else if (direccion.isNullOrEmpty()) {
                et_direccion.error = "Campo Vacio"
            } else {
                crearLugaresFrecuentes(lugar, direccion)
            }
        }

        cargarFavoritos()
        rv_lugfav.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        favoritosAdapter = FavoritosRVAdapter(favoritosList as ArrayList<PlacesDeter>)
        rv_lugfav.adapter = favoritosAdapter


    }

    private fun cargaDatosUser() {
        val database = FirebaseDatabase.getInstance()
        myRef = database.getReference("usuarios")

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

    }

    private fun cargarFavoritos() {
        val database = FirebaseDatabase.getInstance()
        myRef = database.getReference(correo)

        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val fav = datasnapshot.getValue(PlacesDeter::class.java)
                    favoritosList.add(fav!!)
                    Log.d("Favoritos", fav.lugar)
                }
                favoritosAdapter.notifyDataSetChanged()

            }

        }
        myRef.addListenerForSingleValueEvent(postListener)
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
        val myRef: DatabaseReference = database.getReference(correo)
        val id: String? = myRef.push().key

        val lugar_frecuente = PlacesDeter(
            id,
            lugar,
            direccion
        )
        myRef.child(id!!).setValue(lugar_frecuente)
    }
}