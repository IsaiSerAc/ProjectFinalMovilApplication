package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DefinirHorario : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db   by lazy { FirebaseFirestore.getInstance() }

    // día → (Switch, Inicio, Fin)
    private lateinit var ui: Map<String, Triple<SwitchCompat, TextInputEditText, TextInputEditText>>
    private lateinit var btnGuardar: MaterialButton
    private var mode: String = "edit"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_definir_horario)

        // Determina si estamos en modo 'view' o 'edit'
        mode = intent.getStringExtra("mode") ?: "edit"

        // Mapea cada día a sus componentes UI
        ui = mapOf(
            "mon" to Triple(findViewById(R.id.swLunes),    findViewById(R.id.etLunesInicio),    findViewById(R.id.etLunesFin)),
            "tue" to Triple(findViewById(R.id.swMartes),   findViewById(R.id.etMartesInicio),   findViewById(R.id.etMartesFin)),
            "wed" to Triple(findViewById(R.id.swMiercoles),findViewById(R.id.etMiercolesInicio),findViewById(R.id.etMiercolesFin)),
            "thu" to Triple(findViewById(R.id.swJueves),   findViewById(R.id.etJuevesInicio),   findViewById(R.id.etJuevesFin)),
            "fri" to Triple(findViewById(R.id.swViernes),  findViewById(R.id.etViernesInicio),  findViewById(R.id.etViernesFin)),
            "sat" to Triple(findViewById(R.id.swSabado),   findViewById(R.id.etSabadoInicio),   findViewById(R.id.etSabadoFin)),
            "sun" to Triple(findViewById(R.id.swDomingo),  findViewById(R.id.etDomingoInicio),  findViewById(R.id.etDomingoFin))
        )

        // Asigna MaterialTimePicker a cada campo de hora
        ui.values.forEach { (_, ini, fin) ->
            ini.setOnClickListener { pickTime(ini) }
            fin.setOnClickListener { pickTime(fin) }
        }

        btnGuardar = findViewById(R.id.btnGuardarHorario)

        if (mode == "view") {
            // Modo solo lectura: oculta guardar y bloquea UI
            btnGuardar.visibility = View.GONE
            loadAndLockHorario()
        } else {
            // Modo edición: habilita guardar
            btnGuardar.setOnClickListener {
                val horario = buildHorario() ?: return@setOnClickListener
                guardarHorario(horario)
            }
        }
    }

    /** Carga el horario desde Firestore y desactiva toda interacción */
    private fun loadAndLockHorario() {
        val uid = auth.uid ?: return
        db.collection("horarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                ui.forEach { (dayKey, triple) ->
                    val sw   = triple.first
                    val etI  = triple.second
                    val etF  = triple.third
                    val data = doc.get(dayKey) as? Map<*, *>

                    sw.isChecked = (data != null)
                    if (data != null) {
                        etI.setText(data["start"] as? String)
                        etF.setText(data["end"] as? String)
                    }

                    // Desactiva edición
                    sw.isEnabled  = false
                    etI.isEnabled = false
                    etF.isEnabled = false
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error cargando horario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun pickTime(target: TextInputEditText) {
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .build().apply {
                addOnPositiveButtonClickListener {
                    target.setText(String.format("%02d:%02d", hour, minute))
                }
                show(supportFragmentManager, target.id.toString())
            }
    }

    private fun buildHorario(): Map<String, Map<String, String>>? {
        val out = mutableMapOf<String, Map<String, String>>()
        ui.forEach { (key, t) ->
            if (t.first.isChecked) {
                val ini = t.second.text.toString()
                val fin = t.third.text.toString()
                if (ini.isBlank() || fin.isBlank()) { toast("Completa horas"); return null }
                if (ini >= fin) { toast("Inicio debe ser menor que fin"); return null }
                out[key] = mapOf("start" to ini, "end" to fin)
            }
        }
        if (out.isEmpty()) { toast("Selecciona al menos un día"); return null }
        return out
    }

    private fun guardarHorario(horario: Map<String, Map<String, String>>) {
        val uid = auth.uid ?: return
        db.collection("horarios").document(uid).set(horario)
            .addOnSuccessListener {
                Toast.makeText(this, "Horario guardado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
