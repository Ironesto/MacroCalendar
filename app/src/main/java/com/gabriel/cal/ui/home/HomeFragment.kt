package com.gabriel.cal.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gabriel.cal.SharedViewModel
import com.gabriel.cal.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var alarmsAdapter: AlarmsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el RecyclerView
        alarmsAdapter = AlarmsAdapter(emptyList())
        binding.rvAlarms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlarms.adapter = alarmsAdapter

        // Observa el LiveData para actualizar la lista de alarmas
        sharedViewModel.selectedAlarms.observe(viewLifecycleOwner) { alarms ->
            val currentTime = System.currentTimeMillis()
            // Filtra las alarmas futuras y ordénalas por fecha
            val futureAlarms = alarms.filter { it.date >= currentTime }
                .sortedBy { it.date }
                .map { it.date } // Extrae solo la fecha (Long)
            alarmsAdapter.updateData(futureAlarms)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
