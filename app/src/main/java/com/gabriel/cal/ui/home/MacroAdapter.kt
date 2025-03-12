package com.gabriel.cal.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gabriel.cal.MacroEntry
import com.gabriel.cal.databinding.ItemMacroBinding
import androidx.core.graphics.toColorInt
import com.gabriel.cal.R

class MacroAdapter(
    private var macros: List<MacroEntry>,
    private val onDeleteClicked: (MacroEntry) -> Unit
) : RecyclerView.Adapter<MacroAdapter.MacroViewHolder>() {

    inner class MacroViewHolder(val binding: ItemMacroBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(macro: MacroEntry) {
            val macroInfoText = binding.root.context.getString(
                R.string.macro_info,
                macro.name,
                macro.hour,
                macro.minute
            )
            binding.tvMacroInfo.text = macroInfoText

            // Establece el color de fondo según el color de la macro.
            try {
                binding.root.setBackgroundColor(macro.color.toColorInt())
            } catch (e: IllegalArgumentException) {
                // En caso de que el formato del color no sea válido, se puede asignar un color por defecto
                binding.root.setBackgroundColor(Color.WHITE)
            }
            // Configura el botón de borrar
            binding.btnDeleteMacro.setOnClickListener {
                onDeleteClicked(macro)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MacroViewHolder {
        val binding = ItemMacroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MacroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MacroViewHolder, position: Int) {
        holder.bind(macros[position])
    }

    override fun getItemCount(): Int = macros.size

    fun updateData(newMacros: List<MacroEntry>) {
        macros = newMacros
        notifyDataSetChanged()
    }
}
