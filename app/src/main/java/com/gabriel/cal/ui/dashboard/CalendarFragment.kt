package com.gabriel.cal.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import com.gabriel.cal.databinding.FragmentCalendarBinding

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Accedemos al CalendarView
        val calendarView: CalendarView = binding.calendarView
        // TextView para mostrar la fecha seleccionada
        val textSelectedDate = binding.textSelectedDate

        // Configuramos el listener del CalendarView
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Recuerda que 'month' empieza en 0, por lo que puedes sumar 1 para mostrar el mes correctamente
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            textSelectedDate.text = "Fecha seleccionada: $selectedDate"
            // Aquí podrías además iniciar una acción, como programar una alarma o navegar a otra pantalla.
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
