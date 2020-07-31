package com.dolphhincapie.introviaggiare.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.adapter.TurismoRVAdapter
import com.dolphhincapie.introviaggiare.model.Turist
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_places.*

class PlacesFragment : Fragment() {

    private var turismoList: MutableList<Turist> = mutableListOf()
    private lateinit var turistaAdapter: TurismoRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_places, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarTurismo()

        rv_turismo.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        turistaAdapter =
            TurismoRVAdapter(turismoList as ArrayList<Turist>)
        rv_turismo.adapter = turistaAdapter

    }

    private fun cargarTurismo() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("turisticos")

        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val turist = datasnapshot.getValue(Turist::class.java)
                    turismoList.add(turist!!)
                }
                turistaAdapter.notifyDataSetChanged()

            }

        }
        myRef.addValueEventListener(postListener)
    }

}