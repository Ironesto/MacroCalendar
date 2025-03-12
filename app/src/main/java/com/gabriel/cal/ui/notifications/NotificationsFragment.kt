package com.gabriel.cal.ui.notifications

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.gabriel.cal.AlarmHelper
import com.gabriel.cal.SharedViewModel
import com.gabriel.cal.databinding.FragmentNotificationsBinding
import java.util.*

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    // Obtén el SharedViewModel a nivel de Activity
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Asume que en el layout hay un botón con id "selectDateButton"
        binding.selectDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Abre un DatePickerDialog para que el usuario seleccione una fecha
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Configura el calendario con la fecha seleccionada
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val dateMillis = calendar.timeInMillis

                // Agrega la alarma (con identificador único) al SharedViewModel
                sharedViewModel.addSelectedAlarm(dateMillis)

                // Programa la alarma para esa fecha (por ejemplo, a las 5 PM)
                AlarmHelper.scheduleAlarmForDate(requireContext(), dateMillis)
            }, year, month, day).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
