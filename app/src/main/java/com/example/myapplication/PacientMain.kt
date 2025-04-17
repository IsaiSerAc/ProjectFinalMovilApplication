package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PacientMain : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvWelcome: TextView
    private lateinit var btnMisCitas: Button
    private lateinit var btnAgendarCita: Button
    private lateinit var btnMisRecetas: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContentView(R.layout.activity_pacient_main)

        // Instancias de Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Referencias a vistas
        tvWelcome      = findViewById(R.id.tvWelcome)
        btnMisCitas    = findViewById(R.id.btnMisCitas)
        btnAgendarCita = findViewById(R.id.btnAgendarCita)
        btnMisRecetas  = findViewById(R.id.btnMisRecetas)

        // 1) Cargar datos del paciente
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
                val nombre = doc.getString("nombre") ?: "Paciente"
                tvWelcome.text = "Bienvenido, $nombre"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // 2) Listeners de navegación
        btnMisCitas.setOnClickListener {
            startActivity(Intent(this, CheckDate::class.java))
        }

        btnAgendarCita.setOnClickListener {
            startActivity(Intent(this, BookDatePacient::class.java))
        }

        btnMisRecetas.setOnClickListener {
            startActivity(Intent(this, SeeRecipePacient::class.java))
        }
    }
}