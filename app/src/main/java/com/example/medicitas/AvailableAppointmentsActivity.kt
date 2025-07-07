package com.example.medicitas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AvailableAppointmentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AvailableAppointmentsAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var noAppointmentsText: TextView
    private lateinit var backButton: ImageButton
    private lateinit var specialtySpinner: Spinner
    private lateinit var filterButton: Button

    private var allAppointments = listOf<AvailableAppointment>()
    private var filteredAppointments = listOf<AvailableAppointment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_available_appointments)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.availableAppointmentsRecyclerView)
        noAppointmentsText = findViewById(R.id.noAppointmentsText)
        backButton = findViewById(R.id.backButton)
        specialtySpinner = findViewById(R.id.specialtySpinner)
        filterButton = findViewById(R.id.filterButton)

        setupSpecialtySpinner()
        setupAdapter()
        loadAvailableAppointments()

        backButton.setOnClickListener { finish() }

        filterButton.setOnClickListener {
            applyFilter()
        }
    }

    private fun setupSpecialtySpinner() {
        val specialties = resources.getStringArray(R.array.specialties_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        specialtySpinner.adapter = adapter
    }

    private fun setupAdapter() {
        adapter = AvailableAppointmentsAdapter { appointment ->
            val intent = Intent(this, ReservationsFormActivity::class.java).apply {
                putExtra("selectedSpecialty", appointment.specialty)
                putExtra("selectedDateTime", appointment.dateTime)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadAvailableAppointments() {
        db.collection("doctor_agendas")
            .get()
            .addOnSuccessListener { result ->
                val availableAppointments = mutableListOf<AvailableAppointment>()

                for (document in result) {
                    val specialty = document.getString("specialty") ?: ""
                    val doctorId = document.getString("doctorId") ?: ""
                    val slots = document.get("availableSlots") as? List<Long> ?: emptyList()

                    slots.forEach { slot ->
                        availableAppointments.add(
                            AvailableAppointment(
                                doctorId = doctorId,
                                specialty = specialty,
                                dateTime = slot
                            )
                        )
                    }
                }

                allAppointments = availableAppointments
                checkReservedAppointments(allAppointments)
            }
    }

    private fun checkReservedAppointments(availableAppointments: List<AvailableAppointment>) {
        db.collection("reservations")
            .get()
            .addOnSuccessListener { result ->
                val reservedSlots = result.documents.mapNotNull { doc ->
                    doc.getLong("dateTime")
                }

                val trulyAvailable = availableAppointments.filter { appointment ->
                    !reservedSlots.contains(appointment.dateTime)
                }

                allAppointments = trulyAvailable.sortedBy { it.dateTime }
                filteredAppointments = allAppointments
                updateAppointmentsList()
            }
    }

    private fun applyFilter() {
        val selectedSpecialty = specialtySpinner.selectedItem.toString()

        filteredAppointments = if (selectedSpecialty == "Todas las especialidades") {
            allAppointments
        } else {
            allAppointments.filter { it.specialty == selectedSpecialty }
        }

        updateAppointmentsList()
    }

    private fun updateAppointmentsList() {
        if (filteredAppointments.isEmpty()) {
            noAppointmentsText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noAppointmentsText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.submitList(filteredAppointments)
        }
    }
}

data class AvailableAppointment(
    val doctorId: String,
    val specialty: String,
    val dateTime: Long
)