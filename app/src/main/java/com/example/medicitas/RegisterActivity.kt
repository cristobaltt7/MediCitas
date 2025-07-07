package com.example.medicitas

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isPasswordVisible = false

    // Patrones de validación
    private val dniPattern = Pattern.compile("^[0-9]{8}[A-Za-z]\$")
    private val phonePattern = Pattern.compile("^[0-9]{9}\$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val dniInput = findViewById<EditText>(R.id.dniInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val passwordToggle = findViewById<ImageView>(R.id.passwordToggle)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Mostrar/ocultar contraseña
        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            passwordInput.inputType = if (isPasswordVisible)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordToggle.setImageResource(
                if (isPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
            passwordInput.setSelection(passwordInput.text.length)
        }

        // Volver al login
        backButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Registrar usuario
        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val dni = dniInput.text.toString().trim().uppercase() // Aseguramos mayúscula en DNI
            val password = passwordInput.text.toString().trim()

            if (validateInputs(name, email, phone, dni, password)) {
                checkUniquenessAndRegister(name, email, phone, dni, password)
            }
        }
    }

    private fun validateInputs(
        name: String,
        email: String,
        phone: String,
        dni: String,
        password: String
    ): Boolean {
        return when {
            name.isEmpty() -> {
                showError("Ingrese su nombre completo")
                false
            }
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Ingrese un email válido")
                false
            }
            !dniPattern.matcher(dni).matches() -> {
                showError("DNI inválido. Formato: 12345678A")
                false
            }
            !phonePattern.matcher(phone).matches() -> {
                showError("Teléfono inválido. Debe tener 9 dígitos")
                false
            }
            password.length < 6 -> {
                showError("La contraseña debe tener al menos 6 caracteres")
                false
            }
            else -> true
        }
    }

    private fun checkUniquenessAndRegister(
        name: String,
        email: String,
        phone: String,
        dni: String,
        password: String
    ) {
        // Verificar si el DNI ya existe
        db.collection("users")
            .whereEqualTo("dni", dni)
            .get()
            .addOnSuccessListener { dniDocs ->
                if (dniDocs.isEmpty) {
                    // Verificar si el teléfono ya existe
                    db.collection("users")
                        .whereEqualTo("phone", phone)
                        .get()
                        .addOnSuccessListener { phoneDocs ->
                            if (phoneDocs.isEmpty) {
                                registerNewUser(name, email, phone, dni, password)
                            } else {
                                showError("Este teléfono ya está registrado")
                            }
                        }
                } else {
                    showError("Este DNI ya está registrado")
                }
            }
            .addOnFailureListener {
                showError("Error al verificar datos. Intente nuevamente")
            }
    }

    private fun registerNewUser(
        name: String,
        email: String,
        phone: String,
        dni: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val user = User(
                        id = userId,
                        name = name,
                        email = email,
                        phone = phone,
                        dni = dni,
                        role = "user"
                    )

                    db.collection("users").document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Registro exitoso. Bienvenido/a $name",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToLogin()
                        }
                        .addOnFailureListener {
                            showError("Error al guardar datos del usuario")
                        }
                } else {
                    showError("Error al registrar: ${task.exception?.message}")
                }
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}