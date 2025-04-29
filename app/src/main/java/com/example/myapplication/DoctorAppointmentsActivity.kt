package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorAppointmentsActivity : AppCompatActivity() {

    private lateinit var rvAppointments: RecyclerView
    private lateinit var appointmentsAdapter: DoctorAppointmentsAdapter
    private val listaCitas = mutableListOf<Cita>()

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContentView(R.layout.activity_doctor_appointments)

        /* ---------- Firebase ---------- */
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        /* ---------- RecyclerView ---------- */
        rvAppointments = findViewById(R.id.rvAppointments)
        rvAppointments.layoutManager = LinearLayoutManager(this)

        appointmentsAdapter = DoctorAppointmentsAdapter(
            listaCitas,

            /* ➊ Tocar la tarjeta: abrir AtenderCita */
            onClick = { cita ->
                startActivity(
                    Intent(this, AtenderCita::class.java).apply {
                        putExtra("CITA_ID", cita.id)        // ← clave que AtenderCita espera
                    }
                )
            },

            /* ➋ Marcar atendida */
            onMarkAttended = { cita ->
                firestore.collection("Citas")
                    .document(cita.id)
                    .update("estado", "atendida")
                    .addOnSuccessListener {
                        cita.estado = "atendida"
                        appointmentsAdapter.notifyItemChanged(listaCitas.indexOf(cita))
                        Toast.makeText(this, "Cita marcada como atendida", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            },

            /* ➌ Cancelar cita */
            onCancel = { cita ->
                firestore.collection("Citas")
                    .document(cita.id)
                    .update("estado", "cancelada")
                    .addOnSuccessListener {
                        cita.estado = "cancelada"
                        appointmentsAdapter.notifyItemChanged(listaCitas.indexOf(cita))
                        Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        )
        rvAppointments.adapter = appointmentsAdapter

        /* ---------- Cargar citas del doctor ---------- */
        val uidDoctor = auth.currentUser?.uid
        if (uidDoctor.isNullOrEmpty()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        firestore.collection("Citas")
            .whereEqualTo("idDoctor", uidDoctor)
            .get()
            .addOnSuccessListener { snap ->
                listaCitas.clear()
                for (doc in snap.documents) {
                    val cita = doc.toObject(Cita::class.java)
                        ?.copy(
                            id = doc.id,
                            nombrePaciente = doc.getString("pacienteNombre") ?: ""
                        )
                    cita?.let { listaCitas.add(it) }
                }
                appointmentsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar citas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /* Flecha de regreso en la ActionBar */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
