package com.example.medicitas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ReservationsAdapter(
    private var reservations: MutableList<Reservation>,
    private val isDoctorView: Boolean
) : RecyclerView.Adapter<ReservationsAdapter.ReservationViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    inner class ReservationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.nameText)
        val specialistText: TextView = view.findViewById(R.id.specialistText)
        val reasonText: TextView = view.findViewById(R.id.reasonText)
        val dateTimeText: TextView = view.findViewById(R.id.dateTimeText)
        val statusText: TextView = view.findViewById(R.id.statusText)
        val confirmButton: Button? = view.findViewById(R.id.confirmButton)
        val cancelButton: Button? = view.findViewById(R.id.cancelButton)
    }

    fun updateReservations(newReservations: List<Reservation>) {
        reservations.clear()
        reservations.addAll(newReservations)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val layout = if (isDoctorView) R.layout.item_doctor_reservation else R.layout.item_user_reservation
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = reservations[position]
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        if (isDoctorView) {
            // Vista doctor: muestra info del paciente
            holder.nameText.text = "Paciente: ${reservation.name}"
            holder.specialistText.text = "DNI: ${reservation.dni}"
            holder.reasonText.text = "Teléfono: ${reservation.phone}"
        } else {
            // Vista usuario: muestra info del doctor/especialidad
            holder.nameText.text = "Doctor: ${reservation.specialist}"
            holder.specialistText.text = "Especialidad: ${reservation.specialist}"
            holder.reasonText.text = "Motivo: ${reservation.reason}"
        }

        holder.dateTimeText.text = "Fecha: ${sdf.format(Date(reservation.dateTime))}"
        holder.statusText.text = "Estado: ${reservation.status.capitalize()}"

        // Configurar botones según la vista
        if (isDoctorView) {
            holder.confirmButton?.setOnClickListener {
                updateReservationStatus(reservation.id, "confirmed", position)
            }
            holder.cancelButton?.setOnClickListener {
                updateReservationStatus(reservation.id, "cancelled", position)
            }

            // Mostrar/ocultar botones según estado
            when (reservation.status) {
                "confirmed" -> {
                    holder.confirmButton?.visibility = View.GONE
                    holder.cancelButton?.visibility = View.VISIBLE
                }
                "cancelled" -> {
                    holder.confirmButton?.visibility = View.VISIBLE
                    holder.cancelButton?.visibility = View.GONE
                }
                else -> {
                    holder.confirmButton?.visibility = View.VISIBLE
                    holder.cancelButton?.visibility = View.VISIBLE
                }
            }
        } else {
            // Vista usuario: ocultar botones de confirmación/cancelación
            holder.confirmButton?.visibility = View.GONE
            holder.cancelButton?.visibility = View.GONE
        }
    }

    private fun updateReservationStatus(reservationId: String, status: String, position: Int) {
        db.collection("reservations").document(reservationId)
            .update("status", status)
            .addOnSuccessListener {
                reservations[position] = reservations[position].copy(status = status)
                notifyItemChanged(position)
            }
    }

    override fun getItemCount(): Int = reservations.size
}