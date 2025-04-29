package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

class AgendaCitaRecep : AppCompatActivity() {

    private lateinit var spinnerDoctor: Spinner
    private lateinit var spinnerPatient: Spinner
    private lateinit var spinnerHours: Spinner
    private lateinit var tvRecommendation: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var etMotivo: com.google.android.material.textfield.TextInputEditText


    private val doctors = mutableListOf<Pair<String, String>>() // (id, nombre)
    private val patients = mutableListOf<Pair<String, String>>() // (id, nombre)
    private val availableHours = mutableListOf<String>()

    private val firestore = FirebaseFirestore.getInstance()

    private var selectedDate = Calendar.getInstance()
    private var selectedDoctorId: String? = null
    private var selectedPatientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContentView(R.layout.activity_agenda_cita_recep)

        spinnerDoctor = findViewById(R.id.spinnerDoctor)
        spinnerPatient = findViewById(R.id.spinnerPatient)
        spinnerHours = findViewById(R.id.spinnerHours)
        tvRecommendation = findViewById(R.id.tvRecommendation)
        btnSave = findViewById(R.id.btnSaveAppointment)
        etMotivo = findViewById(R.id.etMotivo)


        loadDoctors()
        loadPatients()

        findViewById<android.widget.CalendarView>(R.id.calendarView).setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
            if (selectedDoctorId != null) {
                suggestHour()
            }
        }

        btnSave.setOnClickListener { saveAppointment() }
    }

    private fun loadDoctors() {
        firestore.collection("Usuarios")
            .whereEqualTo("rol", "doctor")
            .get()
            .addOnSuccessListener { result ->
                doctors.clear()
                for (doc in result) {
                    val id = doc.id
                    val name = doc.getString("nombre") ?: continue
                    doctors.add(Pair(id, name))
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, doctors.map { it.second })
                spinnerDoctor.adapter = adapter
                spinnerDoctor.setSelection(0)
                spinnerDoctor.setOnItemSelectedListener { position ->
                    selectedDoctorId = doctors[position].first
                    suggestHour()
                }
            }
    }

    private fun loadPatients() {
        firestore.collection("Usuarios")
            .whereEqualTo("rol", "paciente")
            .get()
            .addOnSuccessListener { result ->
                patients.clear()
                for (doc in result) {
                    val id = doc.id
                    val name = doc.getString("nombre") ?: "Paciente sin nombre"
                    patients.add(Pair(id, name))
                }
                if (patients.isEmpty()) {
                    Toast.makeText(this, "No hay pacientes registrados", Toast.LENGTH_SHORT).show()
                }
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    patients.map { it.second }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPatient.adapter = adapter

                if (patients.isNotEmpty()) {
                    spinnerPatient.setSelection(0)
                    selectedPatientId = patients[0].first
                }

                spinnerPatient.setOnItemSelectedListener { position ->
                    selectedPatientId = patients[position].first
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar pacientes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun suggestHour() {
        if (selectedDoctorId == null) return

        val futureDate = Calendar.getInstance().apply {
            timeInMillis = selectedDate.timeInMillis
            add(Calendar.DAY_OF_YEAR, 7) // ← sumar 7 días
        }

        // Actualizar la fecha seleccionada internamente
        selectedDate.timeInMillis = futureDate.timeInMillis

        // Mover el calendario visualmente
        findViewById<android.widget.CalendarView>(R.id.calendarView).setDate(futureDate.timeInMillis, true, true)

        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dayFormat.format(selectedDate.time)

        val dayOfWeek = when (selectedDate.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY    -> "mon"
            Calendar.TUESDAY   -> "tue"
            Calendar.WEDNESDAY -> "wed"
            Calendar.THURSDAY  -> "thu"
            Calendar.FRIDAY    -> "fri"
            Calendar.SATURDAY  -> "sat"
            Calendar.SUNDAY    -> "sun"
            else -> ""
        }

        // Paso 1: Obtener el horario del doctor
        firestore.collection("horarios")
            .document(selectedDoctorId!!)
            .get()
            .addOnSuccessListener { horarioDoc ->
                val horarioDia = horarioDoc.get(dayOfWeek) as? Map<*, *>

                if (horarioDia == null) {
                    // No trabaja este día
                    availableHours.clear()
                    availableHours.add("No disponible")
                    tvRecommendation.text = "El doctor no trabaja este día"
                    spinnerHours.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availableHours)
                    spinnerHours.isEnabled = false
                    return@addOnSuccessListener
                }

                val start = horarioDia["start"] as? String ?: return@addOnSuccessListener
                val end   = horarioDia["end"] as? String ?: return@addOnSuccessListener

                val availableSlots = generateHourSlots(start, end)

                // Paso 2: Consultar citas ocupadas para ese día
                firestore.collection("Citas")
                    .whereEqualTo("fecha", dateStr)
                    .whereEqualTo("idDoctor", selectedDoctorId)
                    .get()
                    .addOnSuccessListener { citasSnap ->
                        val occupiedHours = citasSnap.documents.mapNotNull { it.getString("hora") }.toSet()

                        // Paso 3: Filtrar horas disponibles
                        availableHours.clear()
                        availableHours.addAll(availableSlots.filter { it !in occupiedHours })

                        if (availableHours.isEmpty()) {
                            tvRecommendation.text = "No hay horarios libres este día"
                            spinnerHours.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("No disponible"))
                            spinnerHours.isEnabled = false
                        } else {
                            tvRecommendation.text = "Sugerencias disponibles"
                            spinnerHours.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availableHours)
                            spinnerHours.isEnabled = true
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar horarios", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateHourSlots(start: String, end: String): List<String> {
        val result = mutableListOf<String>()

        val startParts = start.split(":").map { it.toInt() }
        val endParts = end.split(":").map { it.toInt() }

        val startHour = startParts[0]
        val endHour = endParts[0]

        for (hour in startHour until endHour) {
            result.add(String.format("%02d:00", hour))
        }

        return result
    }



    private fun saveAppointment() {
        val doctorPos = spinnerDoctor.selectedItemPosition
        val patientPos = spinnerPatient.selectedItemPosition
        val hourPos = spinnerHours.selectedItemPosition
        val motivo = etMotivo.text.toString().trim()
        if (motivo.isBlank()) {
            Toast.makeText(this, "Por favor escribe el motivo de la cita", Toast.LENGTH_SHORT).show()
            return
        }


        if (doctorPos == -1 || patientPos == -1 || hourPos == -1 || availableHours.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        val doctor = doctors[doctorPos]
        val patient = patients[patientPos]
        val hourSelected = availableHours[hourPos]
        val dayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

        val cita = mapOf(
            "idDoctor" to doctor.first,
            "nombreDoctor" to doctor.second,
            "idPaciente" to patient.first,
            "pacienteNombre" to patient.second,
            "fecha" to dayStr,
            "hora" to hourSelected,
            "motivo" to motivo,
            "estado" to "pendiente"
        )

        firestore.collection("Citas")
            .add(cita)
            .addOnSuccessListener {
                Toast.makeText(this, "Cita agendada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Extension para usar setOnItemSelectedListener más fácil
    private fun Spinner.setOnItemSelectedListener(onSelected: (position: Int) -> Unit) {
        this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                onSelected(position)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }
}
