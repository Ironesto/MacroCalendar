package com.gabriel.cal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _selectedDates = MutableLiveData<Set<Long>>(emptySet())
    val selectedDates: LiveData<Set<Long>> get() = _selectedDates

    fun addSelectedDate(dateInMillis: Long) {
        _selectedDates.value = _selectedDates.value?.plus(dateInMillis) ?: setOf(dateInMillis)
    }
}
