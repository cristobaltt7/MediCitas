package com.example.medicitas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AvailableAppointmentsAdapter(
    private val onItemClick: (AvailableAppointment) -> Unit
) : RecyclerView.Adapter<AvailableAppointmentsAdapter.AppointmentViewHolder>() {

    private var appointments = listOf<AvailableAppointment>()

    fun submitList(newList: List<AvailableAppointment>) {
        appointments = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_available_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    inner class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val specialtyText: TextView = view.findViewById(R.id.specialtyText)
        private val dateText: TextView = view.findViewById(R.id.dateText)
        private val timeText: TextView = view.findViewById(R.id.timeText)
        private val bookButton: TextView = view.findViewById(R.id.bookButton)

        fun bind(appointment: AvailableAppointment) {
            val date = Date(appointment.dateTime)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            specialtyText.text = "Especialidad: ${appointment.specialty}"
            dateText.text = "Fecha: ${dateFormat.format(date)}"
            timeText.text = "Hora: ${timeFormat.format(date)}"

            bookButton.setOnClickListener {
                onItemClick(appointment)
            }
        }
    }
}