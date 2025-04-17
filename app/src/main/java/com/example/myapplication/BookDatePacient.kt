package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class BookDatePacient : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var spinnerEsp: Spinner
    private lateinit var spinnerDoc: Spinner
    private lateinit var etFecha: EditText
    private lateinit var etHora: EditText
    private lateinit var etMotivo: EditText
    private lateinit var btnConfirmar: Button

    // Para mapear nombre mostrado a ID interno
    private val doctoresMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_date_pacient)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        spinnerEsp  = findViewById(R.id.spinnerEspecialidad)
        spinnerDoc  = findViewById(R.id.spinnerDoctor)
        etFecha     = findViewById(R.id.etFecha)
        etHora      = findViewById(R.id.etHora)
        etMotivo    = findViewById(R.id.etMotivo)
        btnConfirmar= findViewById(R.id.btnConfirmarCita)

        cargarEspecialidades()

        // DatePicker
        etFecha.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this,
                { _, year, month, day ->
                    etFecha.setText(String.format("%02d/%02d/%04d", day, month+1, year))
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // TimePicker
        etHora.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this,
                { _, hour, minute ->
                    etHora.setText(String.format("%02d:%02d", hour, minute))
                },
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
            ).show()
        }

        btnConfirmar.setOnClickListener {
            val idPaciente = auth.currentUser?.uid ?: run {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val especialidad = spinnerEsp.selectedItem as String
            val nombreDoc   = spinnerDoc.selectedItem as String
            val idDoctor    = doctoresMap[nombreDoc] ?: ""
            val fecha       = etFecha.text.toString()
            val hora        = etHora.text.toString()
            val motivo      = etMotivo.text.toString().trim()

            if (especialidad.isEmpty() || idDoctor.isEmpty() || fecha.isEmpty() || hora.isEmpty() || motivo.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevaCita = hashMapOf(
                "idDoctor" to idDoctor,
                "idPaciente" to idPaciente,
                "fecha" to fecha,
                "hora" to hora,
                "motivo" to motivo,
                "estado" to "pendiente"
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

    private fun cargarEspecialidades() {
        firestore.collection("Doctores")
            .get()
            .addOnSuccessListener { snap ->
                val listaEsp = snap.documents
                    .mapNotNull { it.getString("especialidad") }
                    .distinct()
                spinnerEsp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaEsp)
                    .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                spinnerEsp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long
                    ) {
                        val esp = listaEsp[pos]
                        cargarDoctores(esp)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
    }

    private fun cargarDoctores(especialidad: String) {
        firestore.collection("Doctores")
            .whereEqualTo("especialidad", especialidad)
            .get()
            .addOnSuccessListener { snap ->
                val nombres = mutableListOf<String>()
                doctoresMap.clear()
                for (doc in snap.documents) {
                    val nombre = doc.getString("nombre") ?: continue
                    nombres.add(nombre)
                    doctoresMap[nombre] = doc.id
                }
                spinnerDoc.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
                    .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            }
    }
}
