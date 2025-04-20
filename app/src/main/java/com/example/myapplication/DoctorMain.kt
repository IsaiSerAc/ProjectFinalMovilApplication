package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorMain : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvWelcomeDoctor: TextView
    private lateinit var btnVerCitas: Button
    private lateinit var btnMisRecetas: Button
    private lateinit var btnDefinirHorario: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_doctor_main)

        auth      = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        tvWelcomeDoctor   = findViewById(R.id.tvWelcomeDoctor)
        btnVerCitas       = findViewById(R.id.btnVerCitasDoctor)
        btnMisRecetas     = findViewById(R.id.btnMisRecetasDoctor)
        btnDefinirHorario = findViewById(R.id.btnDefinirHorarioDoctor)

        // Mostrar saludo
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        firestore.collection("Usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre") ?: "Doctor"
                tvWelcomeDoctor.text = "Bienvenido, Dr. $nombre"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Navegación
        btnVerCitas.setOnClickListener {
            startActivity(Intent(this, DoctorAppointmentsActivity::class.java))
        }
        btnMisRecetas.setOnClickListener {
            startActivity(Intent(this, DoctorRecipesActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("horarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // ── MODO VER HORARIO ──
                    btnDefinirHorario.isEnabled = true
                    btnDefinirHorario.text = "Ver horario"
                    btnDefinirHorario.setOnClickListener {
                        startActivity(
                            Intent(this, DefinirHorario::class.java)
                                .putExtra("mode", "view")
                        )
                    }
                } else {
                    // ── MODO DEFINIR HORARIO ──
                    btnDefinirHorario.isEnabled = true
                    btnDefinirHorario.text = "Definir horario"
                    btnDefinirHorario.setOnClickListener {
                        startActivity(
                            Intent(this, DefinirHorario::class.java)
                                .putExtra("mode", "edit")
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al verificar horario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    /** Habilita o bloquea el botón según si el doctor ya definió horario */
    private fun inicializarBotonHorario(uid: String) {
        firestore.collection("horarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // Ya hay horario → deshabilita y quita listener
                    btnDefinirHorario.isEnabled = false
                    btnDefinirHorario.text = "Horario definido"
                    btnDefinirHorario.setOnClickListener(null)
                } else {
                    // Sin horario → habilita y vuelve a asignar listener
                    btnDefinirHorario.isEnabled = true
                    btnDefinirHorario.text = "Definir horario"
                    btnDefinirHorario.setOnClickListener {
                        startActivity(Intent(this, DefinirHorario::class.java))
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al verificar horario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

