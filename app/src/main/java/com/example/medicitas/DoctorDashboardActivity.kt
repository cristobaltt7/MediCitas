package com.example.medicitas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class DoctorDashboardActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_dashboard)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.manageAgendaButton).setOnClickListener {
            startActivity(Intent(this, DoctorAgendaActivity::class.java))
        }

        findViewById<Button>(R.id.viewReservationsButton).setOnClickListener {
            startActivity(Intent(this, DoctorReservationsActivity::class.java))
        }

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}