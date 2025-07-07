package com.example.medicitas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class UsersAdapter(
    private var users: List<User>,
    private val onAction: (User, String) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtEmail: TextView = itemView.findViewById(R.id.txtEmail)
        val txtDni: TextView = itemView.findViewById(R.id.txtDni)
        val spinnerRole: Spinner = itemView.findViewById(R.id.spinnerRole)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.txtName.text = user.name
        holder.txtEmail.text = user.email
        holder.txtDni.text = "DNI: ${user.dni}"

        val roles = listOf("usuario", "doctor")
        val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spinnerRole.adapter = adapter

        val selectedIndex = roles.indexOf(user.role)
        holder.spinnerRole.setSelection(if (selectedIndex >= 0) selectedIndex else 0)

        holder.spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val selectedRole = roles[pos]
                if (selectedRole != user.role) {
                    onAction(user, if (selectedRole == "doctor") "make_doctor" else "make_user")
                }
            }
        }

        holder.btnDelete.setOnClickListener {
            onAction(user, "delete")
        }
    }

    fun updateList(newList: List<User>) {
        users = newList
        notifyDataSetChanged()
    }
}
