package com.gabriel.cal.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gabriel.cal.databinding.ItemAlarmBinding
import java.text.SimpleDateFormat
import java.util.*

class AlarmsAdapter(private var alarms: List<Long>) : RecyclerView.Adapter<AlarmsAdapter.AlarmViewHolder>() {

    // Formateador para mostrar la fecha y la hora (siempre 5:00 PM)
    private val dateFormat = SimpleDateFormat("EEE, d MMM yyyy 'at' h:mm a", Locale.getDefault())

    inner class AlarmViewHolder(private val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alarmTime: Long) {
            // Dado que programamos la alarma para las 5:00 PM, el formateador mostrará la fecha con esa hora.
            binding.tvAlarmTime.text = dateFormat.format(Date(alarmTime))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(alarms[position])
    }

    override fun getItemCount(): Int = alarms.size

    fun updateData(newAlarms: List<Long>) {
        alarms = newAlarms
        notifyDataSetChanged()
    }
}
