package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAddAppointmentBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddAppointment : AppCompatActivity() {

    private lateinit var binding: ActivityAddAppointmentBinding

    // Inicialización correcta de Firestore
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private var selectedDoctorId: String? = null
    private var selectedDoctorName: String? = null
    private var selectedPatientId: String? = null
    private var selectedPatientName: String? = null
    private var selectedCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityAddAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)



        loadDoctors()
        loadPatients()

        binding.tvDate.setOnClickListener { pickDate() }
        binding.tvTime.setOnClickListener { pickTime() }
        binding.btnSave.setOnClickListener { saveAppointment() }
    }

    private fun loadDoctors() {
        db.collection("Doctores").get()
            .addOnSuccessListener { snap ->
                val names = mutableListOf<String>()
                val ids = mutableListOf<String>()
                snap.documents.forEach { doc ->
                    doc.getString("name")?.let {
                        names += it
                        ids += doc.id
                    }
                }
                // Especificamos tipo String para ArrayAdapter
                val adapter = ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    names
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                binding.spinnerDoctor.adapter = adapter
                binding.spinnerDoctor.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedDoctorId = ids[position]
                            selectedDoctorName = names[position]
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
            }
    }

    private fun loadPatients() {
        db.collection("Usuarios")
            .whereEqualTo("rol", "paciente")
            .get()
            .addOnSuccessListener { snap ->
                val names = mutableListOf<String>()
                val ids = mutableListOf<String>()
                snap.documents.forEach { doc ->
                    doc.getString("nombre")?.let {
                        names += it
                        ids += doc.id
                    }
                }
                val adapter = ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    names
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                binding.spinnerPatient.adapter = adapter
                binding.spinnerPatient.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedPatientId = ids[position]
                            selectedPatientName = names[position]
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
            }
    }

    private fun pickDate() {
        val today = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                selectedCalendar.set(y, m, d)
                binding.tvDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(selectedCalendar.time)
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun pickTime() {
        val now = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, h, m ->
                selectedCalendar.set(Calendar.HOUR_OF_DAY, h)
                selectedCalendar.set(Calendar.MINUTE, m)
                binding.tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(selectedCalendar.time)
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveAppointment() {
        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedCalendar.time)
        val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedCalendar.time)

        val cita = hashMapOf(
            "idDoctor"       to selectedDoctorId,
            "nombreDoctor"   to selectedDoctorName,
            "idPaciente"     to selectedPatientId,
            "pacienteNombre" to selectedPatientName, // ← ESTE CAMPO ES EL QUE FALTABA
            "fecha"          to formattedDate,
            "hora"           to formattedTime,
            "motivo"         to "", // motivo vacío por ahora
            "estado"         to "pendiente"
        )


        db.collection("Citas")
            .add(cita)
            .addOnSuccessListener { finish() }
            .addOnFailureListener { /* manejar error */ }
    }



}
