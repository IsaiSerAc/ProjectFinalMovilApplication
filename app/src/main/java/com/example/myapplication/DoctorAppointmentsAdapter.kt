package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView


class DoctorAppointmentsAdapter(
    private val citas: List<Cita>,
    private val onClick: (Cita) -> Unit,
    private val onMarkAttended: (Cita) -> Unit,
    private val onCancel: (Cita) -> Unit
) : RecyclerView.Adapter<DoctorAppointmentsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFechaHora       = view.findViewById<TextView>(R.id.tvFechaHora)
        val tvPaciente        = view.findViewById<TextView>(R.id.tvPaciente)
        val tvMotivo          = view.findViewById<TextView>(R.id.tvMotivo)
        val tvEstado          = view.findViewById<TextView>(R.id.tvEstado)
        val btnMarkAttended   = view.findViewById<Button>(R.id.btnMarkAttended)
        val btnCancel         = view.findViewById<Button>(R.id.btnCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cita_doctor, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val c = citas[position]

        /* --- poblar texto --- */
        holder.tvFechaHora.text = "${c.fecha}, ${c.hora}"
        holder.tvPaciente.text  = "Paciente: ${c.pacienteNombre}"
        holder.tvMotivo.text    = "Motivo: ${c.motivo}"
        holder.tvEstado.text    = "Estado: ${c.estado.capitalize()}"

        /* --- botones sólo si la cita está pendiente --- */
        val pendiente = c.estado == "pendiente"
        holder.btnMarkAttended.visibility = if (pendiente) View.VISIBLE else View.GONE
        holder.btnCancel.visibility        = if (pendiente) View.VISIBLE else View.GONE

        holder.btnMarkAttended.setOnClickListener { onMarkAttended(c) }
        holder.btnCancel.setOnClickListener       { onCancel(c) }

        /* --- click sobre la tarjeta --- */
        holder.itemView.setOnClickListener {
            if (pendiente) {
                onClick(c)                 // abre AtenderCita
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "Esta cita ya no se puede modificar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount() = citas.size
}
