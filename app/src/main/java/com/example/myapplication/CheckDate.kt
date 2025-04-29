package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CheckDate : AppCompatActivity() {

    private lateinit var rvCitas: RecyclerView
    private lateinit var citasAdapter: CitasAdapter  // ← Agregamos el adapter aquí
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val listaCitas = mutableListOf<Cita>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_check_date)

        // Toolbar con botón atrás
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvCitas = findViewById(R.id.rvCitas)
        rvCitas.layoutManager = LinearLayoutManager(this)

        citasAdapter = CitasAdapter(listaCitas) // ← Inicializamos el adapter
        rvCitas.adapter = citasAdapter          // ← Asignamos el adapter al RecyclerView

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Leer todas las citas para este paciente
        firestore.collection("Citas")
            .whereEqualTo("idPaciente", uid)
            .get()
            .addOnSuccessListener { snap ->
                listaCitas.clear()
                for (doc in snap.documents) {
                    val cita = doc.toObject(Cita::class.java)
                    if (cita != null) {
                        firestore.collection("Usuarios")
                            .document(cita.idDoctor)
                            .get()
                            .addOnSuccessListener { docDoctor ->
                                val nombreDoctor = docDoctor.getString("nombre") ?: "Doctor"
                                val citaConNombre = cita.copy(nombreDoctor = nombreDoctor)
                                listaCitas.add(citaConNombre)
                                citasAdapter.notifyDataSetChanged() // ← Ahora sí correcto
                            }
                            .addOnFailureListener {
                                listaCitas.add(cita)
                                citasAdapter.notifyDataSetChanged() // ← Correcto también aquí
                            }
                    }
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
