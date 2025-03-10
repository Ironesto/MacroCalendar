package com.gabriel.cal.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.gabriel.cal.SharedViewModel
import com.gabriel.cal.databinding.FragmentCalendarBinding
import java.util.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // ViewModel compartido
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Carga los días seleccionados (si aún no se han cargado)
        if (sharedViewModel.selectedDates.value.isNullOrEmpty()) {
            sharedViewModel.loadSelectedDates()
        }

        // Observa el LiveData para actualizar los decoradores solo si hay cambios reales.
        sharedViewModel.selectedDates.observe(viewLifecycleOwner) { dateSet ->
            // Si el conjunto actual es igual al ya mostrado, no es necesario actualizar
            // (podrías guardar el conjunto anterior en una variable local y compararlo).
            viewLifecycleOwner.lifecycleScope.launch {
                val calendarDays = withContext(Dispatchers.Default) {
                    dateSet.map { CalendarDay.from(Date(it)) }.toSet()
                }
                // Aquí podrías comparar con un conjunto guardado previamente
                // y solo actualizar si son distintos.
                binding.calendarView.removeDecorators()
                binding.calendarView.addDecorator(SelectedDatesDecorator(requireContext(), calendarDays))
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
