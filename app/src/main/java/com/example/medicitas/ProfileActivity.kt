package com.example.medicitas

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var editProfileButton: CardView
    private lateinit var changePasswordButton: CardView
    private lateinit var logoutButton: CardView
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        userNameTextView = findViewById(R.id.userNameTextView)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        editProfileButton = findViewById(R.id.cardEditProfile)
        changePasswordButton = findViewById(R.id.cardChangePassword)
        logoutButton = findViewById(R.id.cardLogout)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            userEmailTextView.text = currentUser.email

            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: ""
                        val lastname = document.getString("lastname") ?: ""
                        userNameTextView.text = "$name $lastname"
                    } else {
                        userNameTextView.text = "User"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch profile data", Toast.LENGTH_SHORT).show()
                    userNameTextView.text = "User"
                }
        } else {
            userEmailTextView.text = "No session"
            userNameTextView.text = "Guest"
        }

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        changePasswordButton.setOnClickListener {
            startActivity(Intent(this, ChangePasswordProfileActivity::class.java))
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Session closed", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}