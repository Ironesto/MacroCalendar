package com.gabriel.cal.ui.calendar

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.gabriel.cal.AlarmHelper
import com.gabriel.cal.SharedViewModel
import com.gabriel.cal.databinding.FragmentCalendarBinding
import kotlinx.coroutines.launch
import java.util.*
import androidx.core.graphics.toColorInt

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // ViewModel compartido (que contiene macros y asignaciones)
    private val sharedViewModel: SharedViewModel by activityViewModels()

    // Variable para almacenar el día seleccionado (en milisegundos)
    private var currentlySelectedDate: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Asegúrate de cargar las asignaciones de macros desde Firebase.
        // Esto puede hacerse desde el SharedViewModel, por ejemplo:
        sharedViewModel.loadMacroAssignments()

        // Observa la LiveData de asignaciones para pintar los días según el color de la macro asignada.
        sharedViewModel.assignedMacros.observe(viewLifecycleOwner) { assignmentMap ->
            lifecycleScope.launch {
                // Agrupa los días por el color de la macro asignada.
                val groupedByColor = mutableMapOf<Int, MutableSet<CalendarDay>>()
                assignmentMap.forEach { (dayMillis, macro) ->
                    val calDay = CalendarDay.from(Date(dayMillis))
                    val colorInt = try {
                        macro.color.toColorInt()
                    } catch (e: Exception) {
                        Color.WHITE
                    }
                    groupedByColor.getOrPut(colorInt) { mutableSetOf() }.add(calDay)
                }
                binding.calendarView.removeDecorators()
                groupedByColor.forEach { (color, days) ->
                    binding.calendarView.addDecorator(MacroColorDecorator(days, color))
                }
            }
        }

        // Listener para seleccionar un día en el calendario.
        binding.calendarView.setOnDateChangedListener(OnDateSelectedListener { _, date, selected ->
            if (selected) {
                currentlySelectedDate = date.date.time
                binding.textSelectedDate.text = "Fecha seleccionada: ${date.day}/${date.month + 1}/${date.year}"
                // Muestra un botón "Añadir Macro" para asignar una macro a ese día.
                binding.btnAddMacro.visibility = View.VISIBLE
            }
        })

        // Botón "Añadir Macro": abre un diálogo con la lista de macros para asignar al día seleccionado.
        binding.btnAddMacro.setOnClickListener {
            val macros = sharedViewModel.macros.value?.toList() ?: emptyList()
            if (macros.isEmpty()) return@setOnClickListener

            val macroNames = macros.map { it.name }.toTypedArray()
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Elige una macro")
                .setItems(macroNames) { _, which ->
                    val selectedMacro = macros[which]
                    currentlySelectedDate?.let { dayMillis ->
                        // Combinar el día seleccionado con la hora y minuto de la macro para obtener el momento de la alarma.
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = dayMillis
                            set(Calendar.HOUR_OF_DAY, selectedMacro.hour)
                            set(Calendar.MINUTE, selectedMacro.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val alarmTime = cal.timeInMillis
                        // Programar la alarma con la hora y minuto de la macro.
                        AlarmHelper.scheduleAlarmForDate(requireContext(), alarmTime, selectedMacro.name)
                        // Asignar la macro al día en el ViewModel y en Firebase.
                        sharedViewModel.assignMacroToDay(dayMillis, selectedMacro)
                        // Ocultar el botón "Añadir Macro" (opcional)
                        binding.btnAddMacro.visibility = View.GONE
                    }
                }
                .show()
        }

        sharedViewModel.assignedMacros.observe(viewLifecycleOwner) { assignmentMap ->
            lifecycleScope.launch {
                // Agrupa los días según el color de la macro asignada.
                val groupedByColor = mutableMapOf<Int, MutableSet<CalendarDay>>()
                assignmentMap.forEach { (dayMillis, macro) ->
                    val calDay = CalendarDay.from(Date(dayMillis))
                    val colorInt = try {
                        macro.color.toColorInt()
                    } catch (e: Exception) {
                        Color.WHITE
                    }
                    groupedByColor.getOrPut(colorInt) { mutableSetOf() }.add(calDay)
                }
                binding.calendarView.removeDecorators()
                groupedByColor.forEach { (color, days) ->
                    binding.calendarView.addDecorator(MacroDayDecorator(requireContext(), days, color))
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
