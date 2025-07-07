package com.example.medicitas

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isCurrentPasswordVisible = false
    private var isNewPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password_profile)

        auth = FirebaseAuth.getInstance()

        val currentPasswordInput = findViewById<EditText>(R.id.currentPasswordInput)
        val newPasswordInput = findViewById<EditText>(R.id.newPasswordInput)
        val updatePasswordButton = findViewById<Button>(R.id.updatePasswordButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val currentPasswordToggle = findViewById<ImageView>(R.id.currentPasswordToggle)
        val newPasswordToggle = findViewById<ImageView>(R.id.newPasswordToggle)

        backButton.setOnClickListener {
            finish()
        }

        currentPasswordToggle.setOnClickListener {
            isCurrentPasswordVisible = !isCurrentPasswordVisible
            currentPasswordInput.inputType = if (isCurrentPasswordVisible)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            currentPasswordToggle.setImageResource(
                if (isCurrentPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
            currentPasswordInput.setSelection(currentPasswordInput.text.length)
        }

        newPasswordToggle.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            newPasswordInput.inputType = if (isNewPasswordVisible)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            newPasswordToggle.setImageResource(
                if (isNewPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
            newPasswordInput.setSelection(newPasswordInput.text.length)
        }

        updatePasswordButton.setOnClickListener {
            val currentPassword = currentPasswordInput.text.toString()
            val newPassword = newPasswordInput.text.toString()
            val user = auth.currentUser

            if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                if (user != null && user.email != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                    user.reauthenticate(credential)
                        .addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                user.updatePassword(newPassword)
                                    .addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                            finish()
                                        } else {
                                            Toast.makeText(this, "Error updating the password", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(this, "Authentication error. Check your current password.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
