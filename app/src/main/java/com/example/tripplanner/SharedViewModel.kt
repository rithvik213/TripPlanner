package com.example.tripplanner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val selectedAttractions = MutableLiveData<List<String>>()
}