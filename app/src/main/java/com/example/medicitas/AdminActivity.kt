package com.example.medicitas

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private lateinit var userListView: ListView
    private lateinit var dniSearchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var adapter: ArrayAdapter<String>

    private val db = FirebaseFirestore.getInstance()
    private var userList: MutableList<Map<String, Any>> = mutableListOf()
    private var userDisplayList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        userListView = findViewById(R.id.userListView)
        dniSearchInput = findViewById(R.id.dniSearchInput)
        searchButton = findViewById(R.id.searchButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userDisplayList)
        userListView.adapter = adapter

        loadUsers()

        searchButton.setOnClickListener {
            val dni = dniSearchInput.text.toString()
            if (dni.isNotEmpty()) searchUserByDni(dni)
            else loadUsers()
        }

        userListView.setOnItemClickListener { _, _, position, _ ->
            val user = userList[position]
            val email = user["email"].toString()
            val userId = user["id"].toString()

            showRoleDialog(email, userId)
        }
    }

    private fun loadUsers() {
        db.collection("users").get().addOnSuccessListener { documents ->
            userList.clear()
            userDisplayList.clear()
            for (doc in documents) {
                val user = doc.data
                user["id"] = doc.id
                userList.add(user)
                userDisplayList.add("${user["name"]} - ${user["email"]} - ${user["role"]}")
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun searchUserByDni(dni: String) {
        db.collection("users").whereEqualTo("dni", dni).get()
            .addOnSuccessListener { documents ->
                userList.clear()
                userDisplayList.clear()
                for (doc in documents) {
                    val user = doc.data
                    user["id"] = doc.id
                    userList.add(user)
                    userDisplayList.add("${user["name"]} - ${user["email"]} - ${user["role"]}")
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun showRoleDialog(email: String, userId: String) {
        val roles = arrayOf("usuario", "doctor", "admin", "Eliminar usuario")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Acciones para $email")
        builder.setItems(roles) { _, which ->
            when (which) {
                0, 1, 2 -> assignRole(userId, roles[which]) // usuario, doctor, admin
                3 -> deleteUser(userId)
            }
        }
        builder.show()
    }

    private fun assignRole(userId: String, role: String) {
        db.collection("users").document(userId).update("role", role)
            .addOnSuccessListener {
                Toast.makeText(this, "Rol actualizado a $role", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
    }

    private fun deleteUser(userId: String) {
        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
    }
}
