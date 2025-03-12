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
        // Extrae el día, mes y año usando Calendar
        val calendar = Calendar.getInstance().apply { timeInMillis = dayMillis }
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1  // Los meses inician en 0
        val year = calendar.get(Calendar.YEAR)

        // Actualiza la asignación en la LiveData local
        val current = _assignedMacros.value ?: mutableMapOf()
        current[dayMillis] = macro
        _assignedMacros.value = current

        // Guarda la asignación en Firestore, incluyendo macroId, macroName, day, month y year
        db.collection("macroAssignments")
            .document(dayMillis.toString())
            .set(
                mapOf(
                    "macroId" to macro.id,
                    "macroName" to macro.name,
                    "day" to day,
                    "month" to month,
                    "year" to year
                )
            )
            .addOnSuccessListener {
                Log.d("SharedViewModel", "Macro asignada para el día $day/$month/$year con nombre ${macro.name}")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al asignar macro", e)
            }
    }

    fun loadMacroAssignments() {
        db.collection("macroAssignments")
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Necesitamos que las macros ya estén cargadas
                val macrosList = _macros.value?.toList() ?: emptyList()
                val assignmentMap = mutableMapOf<Long, MacroEntry>()
                for (doc in querySnapshot.documents) {
                    val dayMillis = doc.id.toLongOrNull() ?: continue
                    val macroId = doc.getString("macroId") ?: continue
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
        val current = _assignedMacros.value ?: mutableMapOf()
        current.remove(dayMillis)
        _assignedMacros.value = current
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
