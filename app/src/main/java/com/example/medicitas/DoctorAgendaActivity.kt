package com.example.medicitas

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DoctorAgendaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var specialtySpinner: Spinner
    private lateinit var datePicker: Button
    private lateinit var timePicker: Button
    private lateinit var saveButton: Button
    private lateinit var slotsListView: ListView
    private lateinit var backButton: ImageButton


    private var selectedDate = Calendar.getInstance()
    private var selectedTime = Calendar.getInstance()
    private val availableSlots = mutableListOf<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_agenda)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        backButton = findViewById(R.id.backButton)

        specialtySpinner = findViewById(R.id.specialtySpinner)
        datePicker = findViewById(R.id.datePickerButton)
        timePicker = findViewById(R.id.timePickerButton)
        saveButton = findViewById(R.id.saveAgendaButton)
        slotsListView = findViewById(R.id.slotsListView)

        setupSpecialtiesSpinner()
        setupClickListeners()
        loadExistingAgenda()

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupSpecialtiesSpinner() {
        val specialties = resources.getStringArray(R.array.specialties_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        specialtySpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        datePicker.setOnClickListener {
            showDatePicker()
        }

        timePicker.setOnClickListener {
            showTimePicker()
        }

        saveButton.setOnClickListener {
            saveAgenda()
        }

        slotsListView.setOnItemClickListener { _, _, position, _ ->
            availableSlots.removeAt(position)
            updateSlotsList()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(this, { _, year, month, day ->
            selectedDate.set(year, month, day)
        },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(this, { _, hour, minute ->
            selectedTime.set(Calendar.HOUR_OF_DAY, hour)
            selectedTime.set(Calendar.MINUTE, minute)

            // Combinar fecha y hora seleccionada
            val slot = Calendar.getInstance().apply {
                set(selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH),
                    selectedTime.get(Calendar.HOUR_OF_DAY),
                    selectedTime.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val slotTimestamp = slot.timeInMillis
            if (!availableSlots.contains(slotTimestamp)) {
                availableSlots.add(slotTimestamp)
                updateSlotsList()
            } else {
                Toast.makeText(this, "Este horario ya está agregado", Toast.LENGTH_SHORT).show()
            }
        },
            selectedTime.get(Calendar.HOUR_OF_DAY),
            selectedTime.get(Calendar.MINUTE),
            true).show()
    }

    private fun updateSlotsList() {
        val formattedSlots = availableSlots.map { timestamp ->
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
        slotsListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, formattedSlots)
    }

    private fun saveAgenda() {
        val doctorId = auth.currentUser?.uid ?: return
        val specialty = specialtySpinner.selectedItem.toString()

        if (availableSlots.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un horario disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val agenda = DoctorAgenda(
            doctorId = doctorId,
            specialty = specialty,
            availableSlots = availableSlots
        )

        db.collection("doctor_agendas").document(doctorId)
            .set(agenda)
            .addOnSuccessListener {
                Toast.makeText(this, "Agenda guardada correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar la agenda", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadExistingAgenda() {
        val doctorId = auth.currentUser?.uid ?: return

        db.collection("doctor_agendas").document(doctorId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val agenda = document.toObject(DoctorAgenda::class.java)
                    agenda?.let {
                        availableSlots.clear()
                        availableSlots.addAll(it.availableSlots)
                        updateSlotsList()
                        // Seleccionar la especialidad guardada
                        val specialties = resources.getStringArray(R.array.specialties_array)
                        val position = specialties.indexOf(it.specialty)
                        if (position >= 0) {
                            specialtySpinner.setSelection(position)
                        }
                    }
                }
            }
    }
}