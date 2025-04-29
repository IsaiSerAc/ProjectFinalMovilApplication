package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemAppointmentBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AppointmentAdapter(
    private val items: List<Appointment>,
    private val listener: (Appointment, AppointmentAction) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val b: ItemAppointmentBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(app: Appointment) {
            b.tvDoctor.text = app.doctorName
            b.tvPatient.text = app.patientName
            b.tvDateTime.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(app.dateTime.toDate())

            b.btnConfirm.visibility = if (app.status == "pendiente") View.VISIBLE else View.GONE
            b.btnCancel.visibility = if (app.status == "pendiente") View.VISIBLE else View.GONE

            b.btnConfirm.setOnClickListener { listener(app, AppointmentAction.CONFIRM) }
            b.btnCancel.setOnClickListener { listener(app, AppointmentAction.CANCEL) }
        }
    }
}
