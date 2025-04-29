package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AppointmentRecepAdapter(
    private val items: List<Appointment>,
    private val onItemClick: (Appointment, Int) -> Unit
) : RecyclerView.Adapter<AppointmentRecepAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPaciente: TextView = view.findViewById(R.id.tvPacienteName)
        val tvHora: TextView = view.findViewById(R.id.tvHora)
        val tvEstado: TextView = view.findViewById(R.id.tvStatus)

        init {
            view.setOnClickListener {
                onItemClick(items[adapterPosition], adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment_recep, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cita = items[position]

        val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val hora = hourFormat.format(cita.dateTime.toDate())

        holder.tvPaciente.text = "Paciente: ${cita.patientName}"
        holder.tvHora.text = "Hora: $hora"
        holder.tvEstado.text = "Estado: ${cita.status.capitalize()}"
    }

    override fun getItemCount(): Int = items.size
}
