package com.example.myapplication


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.widget.TextView
import com.google.android.material.button.MaterialButton

class AtenderCita : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvFechaHora: TextView
    private lateinit var tvPaciente: TextView
    private lateinit var tvMotivo: TextView
    private lateinit var etDiagnostico: TextInputEditText
    private lateinit var etMedicamentos: TextInputEditText
    private lateinit var etRecomendaciones: TextInputEditText
    private lateinit var btnGenerarReceta: MaterialButton

    private var citaId: String? = null
    private var idPaciente: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atender_cita)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        citaId = intent.getStringExtra("CITA_ID")
        if (citaId.isNullOrEmpty()) {
            Toast.makeText(this, "Cita inválida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Referencias a vistas
        tvFechaHora       = findViewById(R.id.tvFechaHoraDetalle)
        tvPaciente        = findViewById(R.id.tvPacienteDetalle)
        tvMotivo          = findViewById(R.id.tvMotivoDetalle)
        etDiagnostico     = findViewById(R.id.etDiagnostico)
        etMedicamentos    = findViewById(R.id.etMedicamentos)
        etRecomendaciones = findViewById(R.id.etRecomendaciones)
        btnGenerarReceta  = findViewById(R.id.btnGenerarReceta)

        cargarDatosCita()

        btnGenerarReceta.setOnClickListener { generarReceta() }
    }

    private fun cargarDatosCita() {
        firestore.collection("Citas")
            .document(citaId!!)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Cita no encontrada", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }
                val fecha = doc.getString("fecha") ?: ""
                val hora  = doc.getString("hora") ?: ""
                tvFechaHora.text = "Fecha: $fecha, Hora: $hora"

                idPaciente = doc.getString("idPaciente")
                val nombrePac = doc.getString("pacienteNombre") ?: "Paciente"
                tvPaciente.text = "Paciente: $nombrePac"

                val motivo = doc.getString("motivo") ?: ""
                tvMotivo.text = "Motivo: $motivo"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun generarReceta() {
        val diag  = etDiagnostico.text.toString().trim()
        val meds  = etMedicamentos.text.toString().trim()
        val recs  = etRecomendaciones.text.toString().trim()

        if (diag.isEmpty() || meds.isEmpty() || recs.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Preparar lista de medicamentos
        val medsList = meds.split(",").map { it.trim() }

        // Datos de la receta
        val receta = hashMapOf(
            "idCita"          to citaId,
            "idDoctor"        to auth.currentUser!!.uid,
            "idPaciente"      to idPaciente,
            "fecha"           to SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            "diagnostico"     to diag,
            "medicamentos"    to medsList,
            "recomendaciones" to recs
        )

        // Guardar receta y actualizar estado de la cita
        firestore.collection("Recetas")
            .add(receta)
            .addOnSuccessListener {
                firestore.collection("Citas")
                    .document(citaId!!)
                    .update("estado", "atendida")

                Toast.makeText(this, "Receta generada correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al generar receta: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
