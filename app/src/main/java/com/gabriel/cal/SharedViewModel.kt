package com.gabriel.cal

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class SharedViewModel : ViewModel() {
    private val db = Firebase.firestore

    // Para macros
    private val _macros = MutableLiveData<Set<MacroEntry>>(emptySet())
    val macros: LiveData<Set<MacroEntry>> get() = _macros

    // Para asignaciones: Map<dayMillis, MacroEntry>
    private val _assignedMacros = MutableLiveData<MutableMap<Long, MacroEntry>>(mutableMapOf())
    val assignedMacros: LiveData<MutableMap<Long, MacroEntry>> get() = _assignedMacros

    // Obtiene el UID del usuario autenticado
    private fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun addMacro(name: String, color: String, hour: Int, minute: Int) {
        val uid = getUserId() ?: return
        val macroId = UUID.randomUUID().toString()
        val newMacro = MacroEntry(macroId, name, color, hour, minute)
        _macros.value = _macros.value?.plus(newMacro) ?: setOf(newMacro)
        db.collection("users")
            .document(uid)
            .collection("macros")
            .document(macroId)
            .set(
                mapOf(
                    "name" to name,
                    "color" to color,
                    "hour" to hour,
                    "minute" to minute
                )
            )
            .addOnSuccessListener {
                Log.d("SharedViewModel", "Macro guardada con ID: $macroId")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al guardar la macro", e)
            }
    }

    fun loadMacrosIfNeeded() {
        if (_macros.value.isNullOrEmpty()) {
            loadMacros()
        }
    }

    fun loadMacros() {
        val uid = getUserId() ?: return
        db.collection("users")
            .document(uid)
            .collection("macros")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val macrosList = querySnapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    val color = doc.getString("color")
                    val hour = doc.getLong("hour")?.toInt()
                    val minute = doc.getLong("minute")?.toInt()
                    if (name != null && color != null && hour != null && minute != null) {
                        MacroEntry(doc.id, name, color, hour, minute)
                    } else null
                }.toSet()
                _macros.value = macrosList
                Log.d("SharedViewModel", "Se cargaron ${macrosList.size} macros.")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al cargar macros", e)
            }
    }

    fun removeMacro(macro: MacroEntry) {
        val uid = getUserId() ?: return
        _macros.value = _macros.value?.minus(macro)
        db.collection("users")
            .document(uid)
            .collection("macros")
            .document(macro.id)
            .delete()
            .addOnSuccessListener {
                Log.d("SharedViewModel", "Macro eliminada con ID: ${macro.id}")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al eliminar la macro", e)
            }
    }

    fun updateMacro(context: Context, macro: MacroEntry) {
        val uid = getUserId() ?: return
        // Actualiza la lista local reemplazando la macro modificada
        _macros.value = _macros.value?.map { if (it.id == macro.id) macro else it }?.toSet()

        // Actualiza la macro en Firebase
        db.collection("users")
            .document(uid)
            .collection("macros")
            .document(macro.id)
            .set(macro)
            .addOnSuccessListener {
                Log.d("SharedViewModel", "Macro actualizada con ID: ${macro.id}")
                // Si la macro actualizada está asignada, reprograma la alarma con los nuevos valores
                _assignedMacros.value?.forEach { (dayMillis, assignedMacro) ->
                    if (assignedMacro.id == macro.id) {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = dayMillis
                            set(Calendar.HOUR_OF_DAY, macro.hour)
                            set(Calendar.MINUTE, macro.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        AlarmHelper.scheduleAlarmForDate(context, cal.timeInMillis, macro.name)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al actualizar la macro", e)
            }
    }



    fun assignMacroToDay(dayMillis: Long, macro: MacroEntry) {
        val uid = getUserId() ?: return
        val current = _assignedMacros.value ?: mutableMapOf()
        current[dayMillis] = macro
        _assignedMacros.value = current
        db.collection("users")
            .document(uid)
            .collection("macroAssignments")
            .document(dayMillis.toString())
            .set(
                mapOf(
                    "macroId" to macro.id,
                    "macroName" to macro.name,
                    "day" to Calendar.getInstance().apply { timeInMillis = dayMillis }.get(Calendar.DAY_OF_MONTH),
                    "month" to Calendar.getInstance().apply { timeInMillis = dayMillis }.get(Calendar.MONTH) + 1,
                    "year" to Calendar.getInstance().apply { timeInMillis = dayMillis }.get(Calendar.YEAR)
                )
            )
            .addOnSuccessListener {
                Log.d("SharedViewModel", "Macro asignada para el día $dayMillis")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al asignar macro", e)
            }
    }

    fun loadMacroAssignments() {
        val uid = getUserId() ?: return
        db.collection("users")
            .document(uid)
            .collection("macroAssignments")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val assignmentMap = mutableMapOf<Long, MacroEntry>()
                val macrosList = _macros.value?.toList() ?: emptyList()
                querySnapshot.documents.forEach { doc ->
                    val dayMillis = doc.id.toLongOrNull() ?: return@forEach
                    val macroId = doc.getString("macroId") ?: return@forEach
                    val macro = macrosList.find { it.id == macroId }
                    if (macro != null) {
                        assignmentMap[dayMillis] = macro
                    }
                }
                _assignedMacros.value = assignmentMap
                Log.d("SharedViewModel", "Se cargaron asignaciones: ${assignmentMap.size}")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al cargar asignaciones", e)
            }
    }

    fun removeAssignmentForDay(dayMillis: Long) {
        val uid = getUserId() ?: return
        val current = _assignedMacros.value ?: mutableMapOf()
        current.remove(dayMillis)
        _assignedMacros.value = current
        db.collection("users")
            .document(uid)
            .collection("macroAssignments")
            .document(dayMillis.toString())
            .delete()
            .addOnSuccessListener {
                Log.d("SharedViewModel", "Asignación eliminada para el día $dayMillis")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al eliminar asignación", e)
            }
    }
}
