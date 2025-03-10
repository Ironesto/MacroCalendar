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

        sharedViewModel.selectedDates.observe(viewLifecycleOwner) { dateSet ->
            val calendarDays = dateSet.map { CalendarDay.from(Date(it)) }.toSet()

            // Limpia decoradores previos
            binding.calendarView.removeDecorators()

            // Agrega el decorador con todas las fechas seleccionadas
            binding.calendarView.addDecorator(SelectedDatesDecorator(requireContext(), calendarDays))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
