package com.example.medicitas

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class SpecialtyReservationsActivity : AppCompatActivity() {

    private lateinit var specialtySpinner: Spinner
    private lateinit var listView: ListView

    private val db = FirebaseFirestore.getInstance()
    private val especialidades = listOf(
        "Pediatría", "Dermatología", "Cardiología", "Ginecología", "Neurología",
        "Oftalmología", "Traumatología", "Psiquiatría", "Endocrinología", "Urología"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialty_reservations)

        specialtySpinner = findViewById(R.id.specialtySpinner)
        listView = findViewById(R.id.reservationsList)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, especialidades)
        specialtySpinner.adapter = adapter

        specialtySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long
            ) {
                val selected = especialidades[position]
                loadReservationsBySpecialty(selected)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadReservationsBySpecialty(specialty: String) {
        db.collection("reservations").whereEqualTo("specialty", specialty).get()
            .addOnSuccessListener { documents ->
                val list = documents.map {
                    "${it.getString("userName")} - ${it.getString("hora")} - ${it.getString("doctorName")}"
                }
                listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
            }
    }
}
