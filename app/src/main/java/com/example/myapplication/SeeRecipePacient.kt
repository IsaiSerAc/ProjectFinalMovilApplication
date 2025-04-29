package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SeeRecipePacient : AppCompatActivity() {

    private lateinit var rvRecetas: RecyclerView
    private val listaRecetas = mutableListOf<Receta>()
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContentView(R.layout.activity_see_recipe_pacient)


        rvRecetas = findViewById(R.id.rvRecetas)
        rvRecetas.layoutManager = LinearLayoutManager(this)
        rvRecetas.adapter = RecetasAdapter(listaRecetas)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firestore.collection("Recetas")
            .whereEqualTo("idPaciente", uid)
            .get()
            .addOnSuccessListener { snap ->
                listaRecetas.clear()
                for (doc in snap.documents) {
                    val receta = doc.toObject(Receta::class.java)?.copy(idReceta = doc.id)
                    receta?.let { listaRecetas.add(it) }
                }
                rvRecetas.adapter?.notifyDataSetChanged()
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
