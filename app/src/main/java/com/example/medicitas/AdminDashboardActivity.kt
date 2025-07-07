package com.example.medicitas

import android.os.Bundle
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth


class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private val db = FirebaseFirestore.getInstance()
    private val usersList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        searchInput = findViewById(R.id.searchInput)
        usersRecyclerView = findViewById(R.id.recyclerUsers)
        usersRecyclerView.layoutManager = LinearLayoutManager(this)

        usersAdapter = UsersAdapter(usersList) { user, action ->
            when (action) {
                "delete" -> deleteUser(user)
                "make_doctor" -> updateRole(user.id, "doctor")
                "make_user" -> updateRole(user.id, "usuario")
                "make_admin" -> updateRole(user.id, "admin")
            }
        }

        usersRecyclerView.adapter = usersAdapter

        fetchUsers()

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                filterUsers(query)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchUsers() {
        db.collection("users").get().addOnSuccessListener { result ->
            usersList.clear()
            for (doc in result) {
                val user = User(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    dni = doc.getString("dni") ?: "",
                    role = doc.getString("role") ?: "usuario"
                )
                usersList.add(user)
            }
            usersAdapter.notifyDataSetChanged()
        }
    }

    private fun filterUsers(query: String) {
        val filtered = usersList.filter { it.dni.contains(query, ignoreCase = true) }
        usersAdapter.updateList(filtered)
    }

    private fun updateRole(userId: String, newRole: String) {
        db.collection("users").document(userId)
            .update("role", newRole)
            .addOnSuccessListener {
                Toast.makeText(this, "Rol actualizado", Toast.LENGTH_SHORT).show()
                fetchUsers()
            }
    }

    private fun deleteUser(user: User) {
        db.collection("users").document(user.id).delete().addOnSuccessListener {
            Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
            fetchUsers()
        }
    }
}
