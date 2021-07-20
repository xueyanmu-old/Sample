package com.poatek.sample.ui.scenes.first

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class FirstViewModel : ViewModel() {

    val viewModelInitDate = MutableLiveData<String>()

    var expectedImageOutputPath: String? = null

    init {
        refreshInitializationDate()
    }

    fun refreshInitializationDate() {
        val now = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        viewModelInitDate.value = "ViewModel was initialized at ${formatter.format(now)}"
    }

}