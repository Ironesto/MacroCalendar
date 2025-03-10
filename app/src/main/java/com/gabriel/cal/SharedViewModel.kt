package com.gabriel.cal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // Guardamos la fecha seleccionada en milisegundos (Long)
    private val _selectedDate = MutableLiveData<Long>()
    val selectedDate: LiveData<Long> = _selectedDate

    fun setSelectedDate(dateInMillis: Long) {
        _selectedDate.value = dateInMillis
    }
}
