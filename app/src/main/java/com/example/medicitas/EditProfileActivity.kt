package com.example.medicitas

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.button.MaterialButton

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val dniInput = findViewById<EditText>(R.id.dniInput)
        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)

        val user = auth.currentUser
        if (user != null) {
            emailInput.setText(user.email)
            emailInput.isEnabled = false

            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        nameInput.setText(document.getString("name") ?: "")
                        phoneInput.setText(document.getString("phone") ?: "")
                        dniInput.setText(document.getString("dni") ?: "")
                    }
                }
        }

        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val dni = dniInput.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty() && dni.isNotEmpty()) {
                val updates = mapOf(
                    "name" to name,
                    "phone" to phone,
                    "dni" to dni
                )

                db.collection("users").document(user!!.uid)
                    .update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error updating the profile", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please, complete all the fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}