package com.gabriel.cal.ui.macros

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gabriel.cal.SharedViewModel
import com.gabriel.cal.databinding.FragmentMacrosBinding
import com.gabriel.cal.ui.home.MacroAdapter
import java.util.*

class MacrosFragment : Fragment() {

    private var _binding: FragmentMacrosBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    // Variable para almacenar el color seleccionado; la hora y minuto se obtendrán directamente del TimePicker
    private var selectedColor: String = "#FFBB86FC"

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

        // Configura el TimePicker para abrir un TimePickerDialog al pulsarlo
        binding.timePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(requireContext(), { _, hour, minute ->
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
            // Obtén los valores actuales del TimePicker (si el usuario no los modificó, usarán los valores predeterminados del widget)
            val hour = binding.timePicker.hour
            val minute = binding.timePicker.minute

            // Llama a addMacro usando macroName, selectedColor, y los valores actuales del TimePicker
            sharedViewModel.addMacro(macroName, selectedColor, hour, minute)

            // Reinicia el formulario y oculta el contenedor
            binding.etMacroName.text.clear()
            binding.tvSelectedColor.text = "Color seleccionado: #000000"
            // Opcional: reinicia el TimePicker a la hora actual
            val now = Calendar.getInstance()
            binding.timePicker.hour = now.get(Calendar.HOUR_OF_DAY)
            binding.timePicker.minute = now.get(Calendar.MINUTE)
            binding.macroFormContainer.visibility = View.GONE
            binding.btnToggleMacroForm.text = "Crear Macro"
        }

        // Configura el RecyclerView para listar las macros
        macroAdapter = MacroAdapter(emptyList()) { macroToDelete ->
            sharedViewModel.removeMacro(macroToDelete)
        }
        binding.rvMacros.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMacros.adapter = macroAdapter

        // Observa la LiveData de macros y actualiza la lista ordenada por nombre
        sharedViewModel.macros.observe(viewLifecycleOwner) { macros ->
            val sortedMacros = macros.sortedBy { it.name }
            macroAdapter.updateData(sortedMacros)
        }

        // Precarga las macros desde Firebase solo si es necesario
        sharedViewModel.loadMacrosIfNeeded()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
