package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorRecipesActivity : AppCompatActivity() {

    private lateinit var rvDoctorRecetas: RecyclerView
    private val listaRecetas = mutableListOf<DoctorReceta>()
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_recipes)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvDoctorRecetas = findViewById(R.id.rvDoctorRecetas)
        rvDoctorRecetas.layoutManager = LinearLayoutManager(this)
        rvDoctorRecetas.adapter = DoctorRecipesAdapter(listaRecetas)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Trae sólo las recetas generadas por este doctor
        firestore.collection("Recetas")
            .whereEqualTo("idDoctor", uid)
            .get()
            .addOnSuccessListener { snap ->
                listaRecetas.clear()
                for (doc in snap.documents) {
                    val r = DoctorReceta(
                        idReceta      = doc.id,
                        idPaciente    = doc.getString("idPaciente") ?: "",
                        fecha         = doc.getString("fecha") ?: "",
                        diagnostico   = doc.getString("diagnostico") ?: "",
                        medicamentos  = doc.get("medicamentos") as? List<String> ?: emptyList(),
                        recomendaciones = doc.getString("recomendaciones") ?: ""
                    )
                    listaRecetas.add(r)
                }
                rvDoctorRecetas.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar recetas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
