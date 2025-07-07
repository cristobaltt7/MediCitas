package com.example.medicitas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class DoctorReservationsAdapter(
    private val onAction: (Reservation, String) -> Unit
) : RecyclerView.Adapter<DoctorReservationsAdapter.ReservationViewHolder>() {

    private var reservations = listOf<Reservation>()

    fun submitList(newList: List<Reservation>) {
        reservations = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_reservation, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        holder.bind(reservations[position])
    }

    override fun getItemCount(): Int = reservations.size

    inner class ReservationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val patientName: TextView = view.findViewById(R.id.nameText)
        private val reservationDate: TextView = view.findViewById(R.id.dateTimeText)
        private val reservationReason: TextView = view.findViewById(R.id.reasonText)
        private val reservationStatus: TextView = view.findViewById(R.id.statusText)
        private val confirmButton: TextView = view.findViewById(R.id.confirmButton)
        private val cancelButton: TextView = view.findViewById(R.id.cancelButton)

        fun bind(reservation: Reservation) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            patientName.text = "Paciente: ${reservation.name}"
            reservationDate.text = "Fecha: ${dateFormat.format(Date(reservation.dateTime))}"
            reservationReason.text = "Motivo: ${reservation.reason}"
            reservationStatus.text = "Estado: ${reservation.status.capitalize()}"

            confirmButton.setOnClickListener {
                onAction(reservation, "confirm")
            }

            cancelButton.setOnClickListener {
                onAction(reservation, "cancel")
            }

            // Mostrar/ocultar botones según estado
            when(reservation.status) {
                "confirmed" -> {
                    confirmButton.visibility = View.GONE
                    cancelButton.visibility = View.VISIBLE
                }
                "cancelled" -> {
                    confirmButton.visibility = View.VISIBLE
                    cancelButton.visibility = View.GONE
                }
                else -> {
                    confirmButton.visibility = View.VISIBLE
                    cancelButton.visibility = View.VISIBLE
                }
            }
        }
    }
}