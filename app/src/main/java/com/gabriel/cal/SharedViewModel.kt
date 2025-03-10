package com.gabriel.cal

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SharedViewModel : ViewModel() {
    // Guardamos las fechas seleccionadas (en milisegundos) como un Set inmutable
    private val _selectedDates = MutableLiveData<Set<Long>>(emptySet())
    val selectedDates: LiveData<Set<Long>> get() = _selectedDates

    // Instancia de Firestore para guardar y cargar los días seleccionados
    private val db = Firebase.firestore

    // Agrega una fecha seleccionada al LiveData y la guarda en Firestore
    fun addSelectedDate(dateInMillis: Long) {
        _selectedDates.value = _selectedDates.value?.plus(dateInMillis) ?: setOf(dateInMillis)
        saveDateToFirestore(dateInMillis)
    }

    // Guarda la fecha en Firestore en una colección "selected_dates"
    private fun saveDateToFirestore(dateInMillis: Long) {
        val data = hashMapOf("date" to dateInMillis)
        db.collection("selected_dates")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d("SharedViewModel", "Fecha guardada con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al guardar la fecha", e)
            }
    }

    // Carga las fechas guardadas desde Firestore y actualiza el LiveData
    fun loadSelectedDates() {
        db.collection("selected_dates")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dates = querySnapshot.documents.mapNotNull { doc ->
                    doc.getLong("date")
                }.toSet()
                _selectedDates.value = dates
            }
            .addOnFailureListener { e ->
                Log.w("SharedViewModel", "Error al cargar fechas", e)
            }
    }
}
