package com.example.tripplanner.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val selectedAttractions = MutableLiveData<List<String>>()
}