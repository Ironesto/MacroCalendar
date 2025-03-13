package com.gabriel.cal

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class SharedViewModel : ViewModel() {
    // Para alarmas (ya existentes)
    private val _selectedAlarms = MutableLiveData<Set<AlarmEntry>>(emptySet())
    val selectedAlarms: LiveData<Set<AlarmEntry>> get() = _selectedAlarms

    private val db = Firebase.firestore

    // MÉTODOS DE ALARMAS (ya existentes)
    fun addSelectedAlarm(dateInMillis: Long) {
        val alarmId = UUID.randomUUID().toString()
        val newAlarm = AlarmEntry(alarmId, dateInMillis)
        _selectedAlarms.value = _selectedAlarms.value?.plus(newAlarm) ?: setOf(newAlarm)
        saveAlarmToFirestore(newAlarm)
    }

    private fun saveAlarmToFirestore(alarm: AlarmEntry) {
        db.collection("alarms")
            .document(alarm.id)
            .set(mapOf("date" to alarm.date))
            .addOnSuccessListener {
                Log.d("SharedViewModel", "Alarma guardada con ID: ${alarm.id}")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al guardar la alarma", e)
            }
    }

    fun removeSelectedAlarm(alarm: AlarmEntry) {
        _selectedAlarms.value = _selectedAlarms.value?.minus(alarm)
        db.collection("alarms")
            .document(alarm.id)
            .delete()
            .addOnSuccessListener {
                Log.d("SharedViewModel", "Alarma eliminada con ID: ${alarm.id}")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al eliminar la alarma", e)
            }
    }

    fun loadSelectedAlarms() {
        db.collection("alarms")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val alarms = querySnapshot.documents.mapNotNull { doc ->
                    val date = doc.getLong("date")
                    if (date != null) AlarmEntry(doc.id, date) else null
                }.toSet()
                _selectedAlarms.value = alarms
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al cargar las alarmas", e)
            }
    }

    // Para macros
    private val _macros = MutableLiveData<Set<MacroEntry>>(emptySet())
    val macros: LiveData<Set<MacroEntry>> get() = _macros

    // LiveData para asignaciones: Map<dayMillis, MacroEntry>
    private val _assignedMacros = MutableLiveData<MutableMap<Long, MacroEntry>>(mutableMapOf())
    val assignedMacros: LiveData<MutableMap<Long, MacroEntry>> get() = _assignedMacros

    // MÉTODOS PARA MACROS

    fun addMacro(name: String, color: String, hour: Int, minute: Int) {
        val macroId = UUID.randomUUID().toString()
        val newMacro = MacroEntry(macroId, name, color, hour, minute)
        _macros.value = _macros.value?.plus(newMacro) ?: setOf(newMacro)
        saveMacroToFire(newMacro)
    }

    private fun saveMacroToFire(macro: MacroEntry) {
        db.collection("macros")
            .document(macro.id)
            .set(
                mapOf(
                    "name" to macro.name,
                    "color" to macro.color,
                    "hour" to macro.hour,
                    "minute" to macro.minute
                )
            )
            .addOnSuccessListener {
                Log.d("SharedViewModel", "Macro guardada con ID: ${macro.id}")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al guardar la macro", e)
            }
        Log.d("SharedViewModel", "saliendo para guardar macro")

    }

    fun loadMacrosIfNeeded() {
        if (_macros.value.isNullOrEmpty()) {
            loadMacros()
        }
    }

    fun loadMacros() {
        db.collection("macros")
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
        _macros.value = _macros.value?.minus(macro)
        db.collection("macros")
            .document(macro.id)
            .delete()
            .addOnSuccessListener {
                Log.d("SharedViewModel", "Macro eliminada con ID: ${macro.id}")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al eliminar la macro", e)
            }
    }

// MÉTODOS PARA ASIGNACIONES DE MACROS A DÍAS

    fun assignMacroToDay(dayMillis: Long, macro: MacroEntry) {
        val current = _assignedMacros.value ?: mutableMapOf()
        current[dayMillis] = macro
        _assignedMacros.value = current
        db.collection("macroAssignments")
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
        db.collection("macroAssignments")
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
        // Actualiza el mapa local
        val current = _assignedMacros.value ?: mutableMapOf()
        current.remove(dayMillis)
        _assignedMacros.value = current
        // Elimina el documento correspondiente en Firebase
        db.collection("macroAssignments")
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
