package com.example.medicitas

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ReservationsFormActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var dniInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var specialistSpinner: Spinner
    private lateinit var reasonInput: EditText
    private lateinit var submitButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedDateTimeMillis: Long = 0L



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservations_form)
        // En el onCreate, añade al inicio:
        val selectedSpecialty = intent.getStringExtra("selectedSpecialty")
        val selectedDateTime = intent.getLongExtra("selectedDateTime", 0L)

        initViews()
        setupSpecialtiesSpinner()
        setupDateTimePickers()
        setupSubmitButton()
        if (selectedSpecialty != null && selectedDateTime != 0L) {
            // Seleccionar automáticamente la especialidad y fecha/hora
            val specialtyPosition = (specialistSpinner.adapter as ArrayAdapter<String>).getPosition(selectedSpecialty)
            if (specialtyPosition >= 0) {
                specialistSpinner.setSelection(specialtyPosition)
            }

            selectedDateTimeMillis = selectedDateTime
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateTextView.text = dateFormat.format(Date(selectedDateTime))
            timeTextView.text = dateFormat.format(Date(selectedDateTime))
        }
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        nameInput = findViewById(R.id.nameInput)
        dniInput = findViewById(R.id.dniInput)
        phoneInput = findViewById(R.id.phoneInput)
        specialistSpinner = findViewById(R.id.specialistSpinner)
        reasonInput = findViewById(R.id.reasonInput)
        submitButton = findViewById(R.id.submitButton)
        backButton = findViewById(R.id.backButton)
        dateTextView = findViewById(R.id.dateTextView)
        timeTextView = findViewById(R.id.timeTextView)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setupSpecialtiesSpinner() {
        val specialties = resources.getStringArray(R.array.specialties_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        specialistSpinner.adapter = adapter
    }

    private fun setupDateTimePickers() {
        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDateTimeMillis = calendar.timeInMillis
                    dateTextView.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
                show()
            }
        }

        timeTextView.setOnClickListener {
            if (selectedDateTimeMillis == 0L) {
                Toast.makeText(this, "Selecciona una fecha primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = selectedDateTimeMillis
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    selectedDateTimeMillis = cal.timeInMillis
                    timeTextView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            if (validateForm()) {
                checkDoctorAvailability()
            }
        }
    }

    private fun validateForm(): Boolean {
        return when {
            nameInput.text.isBlank() -> { showError("Ingresa tu nombre"); false }
            dniInput.text.isBlank() -> { showError("Ingresa tu DNI"); false }
            phoneInput.text.isBlank() -> { showError("Ingresa tu teléfono"); false }
            reasonInput.text.isBlank() -> { showError("Ingresa el motivo"); false }
            selectedDateTimeMillis == 0L -> { showError("Selecciona fecha y hora"); false }
            else -> true
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkDoctorAvailability() {
        val specialty = specialistSpinner.selectedItem.toString()

        // Primero buscar doctores con agenda disponible para esta especialidad y horario
        db.collection("doctor_agendas")
            .whereEqualTo("specialty", specialty)
            .whereArrayContains("availableSlots", selectedDateTimeMillis)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(this, "No hay doctores disponibles para este horario", Toast.LENGTH_SHORT).show()
                } else {
                    // Verificar que no exista ya una reserva para este horario
                    val doctorId = querySnapshot.documents.firstOrNull()?.id
                    doctorId?.let {
                        checkExistingReservation(it, specialty)
                    } ?: run {
                        Toast.makeText(this, "Error al encontrar doctor", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar disponibilidad", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkExistingReservation(doctorId: String, specialty: String) {
        db.collection("reservations")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("dateTime", selectedDateTimeMillis)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    createReservation(doctorId, specialty)
                } else {
                    Toast.makeText(this, "Horario no disponible", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createReservation(doctorId: String, specialty: String) {
        val reservation = hashMapOf(
            "userId" to auth.currentUser?.uid,
            "name" to nameInput.text.toString(),
            "dni" to dniInput.text.toString(),
            "phone" to phoneInput.text.toString(),
            "doctorId" to doctorId,
            "specialist" to specialty,
            "reason" to reasonInput.text.toString(),
            "dateTime" to selectedDateTimeMillis,
            "status" to "pending"
        )

        db.collection("reservations")
            .add(reservation)
            .addOnSuccessListener {
                Toast.makeText(this, "Reserva creada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al crear reserva", Toast.LENGTH_SHORT).show()
            }
    }
}