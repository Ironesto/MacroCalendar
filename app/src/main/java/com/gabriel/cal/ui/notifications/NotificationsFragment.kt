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

    // ViewModel compartido
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

        binding.selectDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Muestra el DatePickerDialog
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Configura la fecha seleccionada (esta fecha puede venir a medianoche)
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val dateMillis = calendar.timeInMillis

                // Agrega la fecha al ViewModel
                sharedViewModel.addSelectedDate(dateMillis)

                // Programa la alarma para esa fecha a las 5:00 PM
                AlarmHelper.scheduleAlarmForDate(requireContext(), dateMillis)
            }, year, month, day).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
