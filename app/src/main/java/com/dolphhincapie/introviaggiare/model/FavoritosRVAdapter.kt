package com.dolphhincapie.introviaggiare.model

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolphhincapie.introviaggiare.R
import kotlinx.android.synthetic.main.favoritos_item.view.*

class FavoritosRVAdapter(
    var favoritosList: ArrayList<PlacesDeter>
) : RecyclerView.Adapter<FavoritosRVAdapter.FavoritosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritosViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.favoritos_item, parent, false)
        return FavoritosViewHolder(itemView)
    }

    override fun getItemCount(): Int = favoritosList.size

    override fun onBindViewHolder(holder: FavoritosViewHolder, position: Int) {
        val favoritos: PlacesDeter = favoritosList[position]
        holder.bindFavoritos(favoritos)
    }

    class FavoritosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindFavoritos(favoritos: PlacesDeter) {
            itemView.tv_lugar.text = favoritos.lugar
            itemView.tv_direccion.text = favoritos.direccion
            Log.d("MUESTRA", favoritos.lugar)
        }
    }
}