package com.gabriel.cal.ui.macros

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gabriel.cal.SharedViewModel
import com.gabriel.cal.databinding.FragmentMacrosBinding
import com.gabriel.cal.ui.home.MacroAdapter
import java.util.*
import androidx.core.view.isGone

class MacrosFragment : Fragment() {

    private var _binding: FragmentMacrosBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    // Variables para almacenar la selección del usuario
    private var selectedColor: String = "#000000"
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0

    private lateinit var macroAdapter: MacroAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMacrosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón para alternar el formulario
        binding.btnToggleMacroForm.setOnClickListener {
            if (binding.macroFormContainer.isGone) {
                binding.macroFormContainer.visibility = View.VISIBLE
                binding.btnToggleMacroForm.text = "Ocultar formulario"
            } else {
                binding.macroFormContainer.visibility = View.GONE
                binding.btnToggleMacroForm.text = "Crear Macro"
            }
        }

        // Botón para seleccionar color
        binding.btnSelectColor.setOnClickListener {
            val colorNames = arrayOf("Rojo", "Verde", "Azul", "Amarillo")
            val colorValues = arrayOf("#FF0000", "#00FF00", "#0000FF", "#FFFF00")
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Selecciona un color")
                .setItems(colorNames) { _, which ->
                    selectedColor = colorValues[which]
                    binding.tvSelectedColor.text = "Color seleccionado: $selectedColor"
                }
                .show()
        }

        // TimePicker: usando un TimePickerDialog para seleccionar la hora
        binding.timePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(requireContext(), { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                binding.timePicker.hour = hour
                binding.timePicker.minute = minute
            }, currentHour, currentMinute, true).show()
        }

        // Botón para guardar la macro
        binding.btnCreateMacro.setOnClickListener {
            val macroName = binding.etMacroName.text.toString().trim()
            if (macroName.isEmpty()) {
                binding.etMacroName.error = "Ingresa un nombre"
                return@setOnClickListener
            }
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dateMillis = calendar.timeInMillis
            sharedViewModel.addMacro(macroName, selectedColor, selectedHour, selectedMinute)

            // Reinicia el formulario y oculta el contenedor
            binding.etMacroName.text.clear()
            binding.tvSelectedColor.text = "Color seleccionado: #000000"
            binding.timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            binding.timePicker.minute = calendar.get(Calendar.MINUTE)
            binding.macroFormContainer.visibility = View.GONE
            binding.btnToggleMacroForm.text = "Crear Macro"
        }

        // Configura el RecyclerView para listar las macros
        macroAdapter = MacroAdapter(emptyList()) { macroToDelete ->
            sharedViewModel.removeMacro(macroToDelete)
        }
//        val currentHour = binding.timePicker.hour
//        val currentMinute = binding.timePicker.minute
//        selectedHour = currentHour
//        selectedMinute = currentMinute
        binding.rvMacros.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMacros.adapter = macroAdapter

        // Observa la LiveData de macros
        sharedViewModel.macros.observe(viewLifecycleOwner) { macros ->
            val sortedMacros = macros.sortedBy { it.name }
            macroAdapter.updateData(sortedMacros)
        }

        // Carga las macros solo si es necesario
        sharedViewModel.loadMacrosIfNeeded()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
