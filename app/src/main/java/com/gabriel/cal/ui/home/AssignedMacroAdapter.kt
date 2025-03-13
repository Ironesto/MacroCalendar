package com.gabriel.cal.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gabriel.cal.MacroEntry
import com.gabriel.cal.databinding.ItemAssignedMacroBinding
import java.text.SimpleDateFormat
import java.util.*

class AssignedMacroAdapter(
    private var items: List<Pair<Long, MacroEntry>>, // Pair(dayMillis, MacroEntry)
    private val onDeleteClicked: (dayMillis: Long) -> Unit
) : RecyclerView.Adapter<AssignedMacroAdapter.AssignedMacroViewHolder>() {

    inner class AssignedMacroViewHolder(val binding: ItemAssignedMacroBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<Long, MacroEntry>) {
            val (dayMillis, macro) = item
            // Formatea la fecha en formato "dd/MM/yyyy"
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(Date(dayMillis))
            // Combina la fecha, el nombre y la hora de la macro
            binding.tvAssignedMacroInfo.text =
                "$dateStr: ${macro.name} (${macro.hour}:${if(macro.minute < 10) "0${macro.minute}" else macro.minute})"
            // Establece el fondo del ítem con el color de la macro
            try {
                binding.root.setBackgroundColor(Color.parseColor(macro.color))
            } catch (e: Exception) {
                binding.root.setBackgroundColor(Color.LTGRAY)
            }
            // Configura el botón "Borrar" para eliminar la asignación
            binding.btnDeleteAssignment.setOnClickListener {
                onDeleteClicked(dayMillis)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignedMacroViewHolder {
        val binding = ItemAssignedMacroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssignedMacroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssignedMacroViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Pair<Long, MacroEntry>>) {
        items = newItems
        notifyDataSetChanged()
    }
}
