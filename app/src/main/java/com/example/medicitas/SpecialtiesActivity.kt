package com.example.medicitas

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class SpecialtiesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialties)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val specialties = listOf("Cardiology", "Dermatology", "Neurology", "Pediatrics")
        val listView = findViewById<ListView>(R.id.specialtiesListView)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, specialties)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedSpecialty = specialties[position]
            val intent = Intent(this, ReservationsFormActivity::class.java)
            intent.putExtra("selectedSpecialty", selectedSpecialty)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}