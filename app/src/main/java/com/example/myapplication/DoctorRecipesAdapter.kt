package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DoctorRecipesAdapter(
    private val items: List<DoctorReceta>
) : RecyclerView.Adapter<DoctorRecipesAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvFechaRecetaDoc)
        val tvDiagnostico: TextView = view.findViewById(R.id.tvDiagnosticoRecetaDoc)
        val tvMedicamentos: TextView = view.findViewById(R.id.tvMedicamentosRecetaDoc)
        val tvRecomendaciones: TextView = view.findViewById(R.id.tvRecomendacionesRecetaDoc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_doctor, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val r = items[position]
        holder.tvFecha.text = r.fecha
        holder.tvDiagnostico.text = "Diagnóstico: ${r.diagnostico}"
        holder.tvMedicamentos.text = "Medicamentos: ${r.medicamentos.joinToString(", ")}"
        holder.tvRecomendaciones.text = "Recomendaciones: ${r.recomendaciones}"
    }

    override fun getItemCount(): Int = items.size
}
