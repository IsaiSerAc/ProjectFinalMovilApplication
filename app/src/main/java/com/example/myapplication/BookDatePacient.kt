package com.example.myapplication

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@SuppressLint("NewApi") // Permite usar java.time con desugaring
class BookDatePacient : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var spinnerDoc: Spinner
    private lateinit var spinnerHora: Spinner
    private lateinit var etFecha: EditText
    private lateinit var etMotivo: EditText
    private lateinit var btnConfirmar: Button

    private val doctoresMap = mutableMapOf<String, String>()
    private var selectedFechaIso = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_book_date_pacient)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        spinnerDoc   = findViewById(R.id.spinnerDoctor)
        spinnerHora  = findViewById(R.id.spinnerHoraP)
        etFecha      = findViewById(R.id.etFecha)
        etMotivo     = findViewById(R.id.etMotivo)
        btnConfirmar = findViewById(R.id.btnConfirmarCita)

        // Carga lista de doctores
        cargarDoctores()

        // Cuando cambie doctor, recarga slots
        spinnerDoc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                recargarSlots()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // DatePicker
        etFecha.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this,
                { _, y, m, d ->
                    selectedFechaIso = "%04d-%02d-%02d".format(y, m+1, d)
                    etFecha.setText("%02d/%02d/%04d".format(d, m+1, y))
                    recargarSlots()
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Confirmar cita
        btnConfirmar.setOnClickListener {
            val nombreDoc = spinnerDoc.selectedItem as? String ?: ""
            val idDoctor  = doctoresMap[nombreDoc] ?: ""
            val fecha     = selectedFechaIso
            val horaSel   = spinnerHora.selectedItem as? String ?: ""
            val motivo    = etMotivo.text.toString().trim()

            if (idDoctor.isBlank() || fecha.isBlank() || horaSel.isBlank() || motivo.isBlank()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevaCita = mapOf(
                "idDoctor"   to idDoctor,
                "idPaciente" to auth.currentUser!!.uid,
                "fecha"      to fecha,
                "hora"       to horaSel,
                "motivo"     to motivo,
                "estado"     to "pendiente"
            )

            firestore.collection("Citas")
                .add(nuevaCita)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cita agendada correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al agendar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cargarDoctores() {
        firestore.collection("Usuarios")
            .whereEqualTo("rol", "doctor")
            .get()
            .addOnSuccessListener { snap ->
                val nombres = snap.documents.mapNotNull { it.getString("nombre") }
                doctoresMap.clear()
                nombres.forEach { name ->
                    doctoresMap[name] = snap.documents.first { it.getString("nombre")==name }.id
                }
                spinnerDoc.adapter = ArrayAdapter(this,
                    android.R.layout.simple_spinner_item,
                    if (nombres.isEmpty()) listOf("No hay doctores disponibles") else nombres
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error cargando doctores: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    @SuppressLint("NewApi")
    private fun recargarSlots() {
        val nombreDoc = spinnerDoc.selectedItem as? String ?: return
        val doctorId  = doctoresMap[nombreDoc] ?: return
        val fechaIso  = selectedFechaIso.ifBlank { return }

        firestore.collection("horarios").document(doctorId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Doctor sin disponibilidad definida", Toast.LENGTH_SHORT).show()
                    spinnerHora.adapter = null
                    return@addOnSuccessListener
                }
                val dayKey = dayKeyFromFecha(fechaIso)
                @Suppress("UNCHECKED_CAST")
                val rango = doc.get(dayKey) as? Map<String, String>
                if (rango == null) {
                    Toast.makeText(this, "El doctor no trabaja ese día", Toast.LENGTH_SHORT).show()
                    spinnerHora.adapter = null
                    return@addOnSuccessListener
                }
                val allSlots = generateTimeSlots(rango["start"]!!, rango["end"]!!)
                firestore.collection("Citas")
                    .whereEqualTo("idDoctor", doctorId)
                    .whereEqualTo("fecha", fechaIso)
                    .get()
                    .addOnSuccessListener { snap ->
                        val ocup = snap.documents.mapNotNull { it.getString("hora") }
                        val libres = allSlots.filterNot { it in ocup }
                        spinnerHora.adapter = ArrayAdapter(this,
                            android.R.layout.simple_spinner_item,
                            libres
                        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar disponibilidad: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("NewApi")
    private fun dayKeyFromFecha(fecha: String): String {
        return when (LocalDate.parse(fecha).dayOfWeek) {
            DayOfWeek.MONDAY    -> "mon"
            DayOfWeek.TUESDAY   -> "tue"
            DayOfWeek.WEDNESDAY -> "wed"
            DayOfWeek.THURSDAY  -> "thu"
            DayOfWeek.FRIDAY    -> "fri"
            DayOfWeek.SATURDAY  -> "sat"
            DayOfWeek.SUNDAY    -> "sun"
        }
    }

    @SuppressLint("NewApi")
    private fun generateTimeSlots(start: String, end: String): List<String> {
        val fmt = DateTimeFormatter.ofPattern("HH:mm")
        var cur = LocalTime.parse(start, fmt)
        val stop = LocalTime.parse(end, fmt)
        return buildList {
            while (cur < stop) {
                add(cur.format(fmt))
                cur = cur.plusMinutes(30)
            }
        }
    }
}

