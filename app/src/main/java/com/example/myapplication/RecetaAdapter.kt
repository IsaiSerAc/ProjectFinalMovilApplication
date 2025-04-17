package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class RecetasAdapter(private val items: List<Receta>) :
    RecyclerView.Adapter<RecetasAdapter.RecetaViewHolder>() {

    inner class RecetaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView          = view.findViewById(R.id.tvFechaReceta)
        val tvDoctor: TextView         = view.findViewById(R.id.tvDoctorReceta)
        val tvDiagnostico: TextView    = view.findViewById(R.id.tvDiagnostico)
        val tvMedicamentos: TextView   = view.findViewById(R.id.tvMedicamentos)
        val tvRecomendaciones: TextView= view.findViewById(R.id.tvRecomendaciones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receta, parent, false)
        return RecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        val r = items[position]
        holder.tvFecha.text           = r.fecha
        holder.tvDoctor.text          = "Dr. ${r.idDoctor}"
        holder.tvDiagnostico.text     = "Diagnóstico: ${r.diagnostico}"
        holder.tvMedicamentos.text    = "Medicamentos: ${r.medicamentos.joinToString(", ")}"
        holder.tvRecomendaciones.text = "Recomendaciones: ${r.recomendaciones}"
    }

    override fun getItemCount(): Int = items.size
}
