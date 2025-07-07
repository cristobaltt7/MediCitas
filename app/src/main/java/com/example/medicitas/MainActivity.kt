package com.example.medicitas

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.medicitas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private val healthTips = listOf(
        "Drink at least 8 glasses of water a day to stay healthy and hydrated.",
        "Get at least 7-8 hours of sleep every night for better health.",
        "Exercise regularly to improve your overall well-being.",
        "Eat plenty of fruits and vegetables for a balanced diet.",
        "Take breaks and manage stress to keep your mind fresh."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val healthTipTextView = findViewById<TextView>(R.id.healthTipTextView)
        val randomTip = healthTips.random()
        healthTipTextView.text = randomTip
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        binding.welcomeTextView.text = currentUser?.let {
            "Hello, ${it.email}!"
        } ?: "Welcome to MediCitas"

        binding.viewProfileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.makeReservationButton.setOnClickListener {
            startActivity(Intent(this, ReservationsFormActivity::class.java))
        }

        binding.viewReservationsButton.setOnClickListener {
            startActivity(Intent(this, ReservationsActivity::class.java))
        }

        // Nuevo botón para ver citas disponibles
        binding.viewAvailableAppointmentsButton.setOnClickListener {
            startActivity(Intent(this, AvailableAppointmentsActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}