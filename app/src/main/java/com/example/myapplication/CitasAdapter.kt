package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class CitasAdapter(private val items: List<Cita>) :
    RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    inner class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFechaHora: TextView = view.findViewById(R.id.tvFechaHora)
        val tvDoctor: TextView   = view.findViewById(R.id.tvDoctor)
        val tvMotivo: TextView   = view.findViewById(R.id.tvMotivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = items[position]
        holder.tvFechaHora.text = "${cita.fecha}, ${cita.hora}"
        holder.tvDoctor.text   = "Dr: ${cita.idDoctor}"
        holder.tvMotivo.text   = "Motivo: ${cita.motivo}"
    }

    override fun getItemCount(): Int = items.size
}