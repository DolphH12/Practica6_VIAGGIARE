package com.dolphhincapie.introviaggiare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.model.Turist
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.turistico_item.view.*

class TurismoRVAdapter(
    var turismoList: ArrayList<Turist>
) : RecyclerView.Adapter<TurismoRVAdapter.TurismoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TurismoViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.turistico_item, parent, false)
        return TurismoViewHolder(
            itemView
        )
    }

    override fun getItemCount(): Int = turismoList.size

    override fun onBindViewHolder(holder: TurismoViewHolder, position: Int) {
        val turistico: Turist = turismoList[position]
        holder.bindTuristico(turistico)
    }

    class TurismoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindTuristico(turistico: Turist) {
            itemView.tv_nombret.text = turistico.nombre_lugar
            itemView.tv_direcciont.text = turistico.direccion
            if (!turistico.urlImage.isNullOrEmpty())
                Picasso.get().load(turistico.urlImage).into(itemView.iv_fotot)

        }
    }
}