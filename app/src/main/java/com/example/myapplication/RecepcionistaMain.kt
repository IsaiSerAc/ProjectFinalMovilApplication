package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.AdapterView


class RecepcionistaMain : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: AppointmentRecepAdapter
    private lateinit var spinnerDoctors: Spinner

    private val appointments = mutableListOf<Appointment>()
    private val doctorNames = mutableListOf<String>()
    private val doctorIds = mutableListOf<String>()

    private lateinit var calendarView: CalendarView
    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAdd: com.google.android.material.floatingactionbutton.FloatingActionButton


    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var selectedDateMillis: Long = System.currentTimeMillis()
    private var selectedDoctorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContentView(R.layout.activity_recepcionista_main)

        firestore = FirebaseFirestore.getInstance()
        calendarView = findViewById(R.id.calendarView)
        spinnerDoctors = findViewById(R.id.spinnerDoctors)
        rv = findViewById(R.id.rvAppointments)
        tvEmpty = findViewById(R.id.tvEmpty)
        fabAdd = findViewById(R.id.fabAdd)


        adapter = AppointmentRecepAdapter(appointments) { _, _ -> }
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)

        // Configuramos Calendar
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            selectedDateMillis = calendar.timeInMillis
            loadAppointments()
        }

        loadDoctors()

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AgendaCitaRecep::class.java))
        }

    }

    private fun loadDoctors() {
        firestore.collection("Usuarios")
            .whereEqualTo("rol", "doctor")
            .get()
            .addOnSuccessListener { snap ->
                doctorNames.clear()
                doctorIds.clear()

                for (doc in snap.documents) {
                    val name = doc.getString("nombre") ?: continue
                    doctorNames.add(name)
                    doctorIds.add(doc.id)
                }

                val namesWithHint = mutableListOf("Seleccione un doctor")
                namesWithHint.addAll(doctorNames)

                val adapterSpinner = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    namesWithHint
                )
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDoctors.adapter = adapterSpinner

                spinnerDoctors.setOnItemSelectedListener { position ->
                    if (position == 0) {
                        selectedDoctorId = "" // hint seleccionado, no doctor real
                    } else {
                        selectedDoctorId = doctorIds[position - 1] // ajustar índice
                        loadAppointments()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar doctores", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadAppointments() {
        if (selectedDoctorId == null) return

        val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateIso = isoFmt.format(Date(selectedDateMillis))

        firestore.collection("Citas")
            .whereEqualTo("fecha", dateIso)
            .whereEqualTo("idDoctor", selectedDoctorId)
            .get()
            .addOnSuccessListener { qs ->
                appointments.clear()

                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                for (doc in qs.documents) {
                    val data = doc.data ?: continue

                    val fechaStr = data["fecha"] as? String ?: continue
                    val horaStr = data["hora"] as? String ?: continue

                    val parsedDate: Date = try {
                        parser.parse("$fechaStr $horaStr")!!
                    } catch (_: Exception) {
                        Date()
                    }

                    val ts = Timestamp(parsedDate)

                    val doctorName = data["nombreDoctor"] as? String ?: ""
                    val patientName = data["pacienteNombre"] as? String ?: ""
                    val status = data["estado"] as? String ?: ""

                    appointments.add(
                        Appointment(
                            doctorName = doctorName,
                            patientName = patientName,
                            dateTime = ts,
                            status = status
                        )
                    )
                }

                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (appointments.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun Spinner.setOnItemSelectedListener(onSelected: (position: Int) -> Unit) {
        this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                onSelected(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

}
