package com.gabriel.cal.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gabriel.cal.LoginActivity
import com.gabriel.cal.SharedViewModel
import com.gabriel.cal.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var assignedMacroAdapter: AssignedMacroAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del RecyclerView, etc.
        assignedMacroAdapter = AssignedMacroAdapter(emptyList()) { dayMillis ->
            // Llama a la función del ViewModel que elimina la asignación
            sharedViewModel.removeAssignmentForDay(dayMillis)
        }
        binding.rvAssignedMacros.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssignedMacros.adapter = assignedMacroAdapter

        // Observa la LiveData de asignaciones y actualiza el RecyclerView
        sharedViewModel.assignedMacros.observe(viewLifecycleOwner) { assignmentMap ->
            // Convierte el mapa en una lista de pares ordenada por día (más próximos primero)
            val sortedList = assignmentMap.toList().sortedBy { it.first }
            assignedMacroAdapter.updateData(sortedList)
        }

        // Carga las asignaciones desde Firebase
        sharedViewModel.loadMacroAssignments()

        // Agregar listener para el botón de logout
        binding.btnLogout.setOnClickListener {
            // Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut()
            // Redirigir a la LoginActivity
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            // Finalizar la actividad actual para que no se pueda volver presionando "back"
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
