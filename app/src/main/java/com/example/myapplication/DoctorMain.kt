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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Oculta la barra de título si aún no lo has hecho globalmente
        supportActionBar?.hide()

        setContentView(R.layout.activity_doctor_main)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        tvWelcomeDoctor    = findViewById(R.id.tvWelcomeDoctor)
        btnVerCitas        = findViewById(R.id.btnVerCitasDoctor)
        btnMisRecetas      = findViewById(R.id.btnMisRecetasDoctor)

        // Obtener nombre del doctor y mostrarlo
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firestore.collection("Usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre") ?: "Doctor"
                tvWelcomeDoctor.text = "Bienvenido, Dr. $nombre"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Navegar a la lista de citas
        btnVerCitas.setOnClickListener {
            startActivity(Intent(this, DoctorAppointmentsActivity::class.java))
        }

        // Navegar al historial de recetas
        btnMisRecetas.setOnClickListener {
            startActivity(Intent(this, DoctorRecipesActivity::class.java))
        }
    }
}
