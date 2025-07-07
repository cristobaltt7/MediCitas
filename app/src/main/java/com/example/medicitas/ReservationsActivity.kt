package com.example.medicitas

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReservationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationsAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var noReservationsText: TextView
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservations_list)

        // Inicializar vistas
        recyclerView = findViewById(R.id.reservationsRecyclerView)
        noReservationsText = findViewById(R.id.noReservationsText)
        backButton = findViewById(R.id.backButton)

        // Configurar RecyclerView
        adapter = ReservationsAdapter(mutableListOf(), false) // false indica vista de usuario
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Cargar reservas del usuario
        loadUserReservations()

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadUserReservations() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("reservations")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar reservas", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val reservations = mutableListOf<Reservation>()
                snapshot?.documents?.forEach { doc ->
                    val reservation = doc.toObject(Reservation::class.java)?.copy(id = doc.id)
                    reservation?.let { reservations.add(it) }
                }

                if (reservations.isEmpty()) {
                    noReservationsText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    noReservationsText.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.updateReservations(reservations)
                }
            }
    }
}