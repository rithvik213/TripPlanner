package com.example.tripplanner.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.example.tripplanner.data.Excursion

class ExcursionsViewModel : ViewModel() {
    val excursions = MutableLiveData<MutableList<Excursion>>()

    init {
        excursions.value = mutableListOf()
    }

    fun addExcursions(newExcursions: List<Excursion>) {
        val currentList = excursions.value ?: mutableListOf()
        currentList.addAll(newExcursions)
        excursions.value = currentList
    }

    fun clearExcursions() {
        excursions.value = mutableListOf()
    }
}
