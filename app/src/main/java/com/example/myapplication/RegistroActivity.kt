package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var spinnerRol: Spinner
    private lateinit var btnRegistrar: Button
    private lateinit var tvVolverLogin: TextView

    // Roles disponibles para el usuario
    private val roles = arrayOf("Paciente", "Doctor", "Recepcionista")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        etNombre = findViewById(R.id.etNombre)
        etEmail = findViewById(R.id.etEmailRegistro)
        etPassword = findViewById(R.id.etPasswordRegistro)
        spinnerRol = findViewById(R.id.spinnerRolRegistro)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        tvVolverLogin = findViewById(R.id.tvVolverLogin)

        // Configuración del spinner de roles
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRol.adapter = adapter

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val rol = spinnerRol.selectedItem.toString()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Registrar el usuario en Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser!!.uid
                    // Datos del usuario para guardar en Firestore
                    val datosUsuario = hashMapOf(
                        "nombre" to nombre,
                        "email" to email,
                        "rol" to rol
                    )

                    // Guardar en la colección "Usuarios"
                    firestore.collection("Usuarios").document(uid)
                        .set(datosUsuario)
                        .addOnSuccessListener {
                            // Mostrar Toast de éxito y redireccionar a MainActivity
                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error al guardar en Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al registrar: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

        }
    }
}
