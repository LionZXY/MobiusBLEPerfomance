package com.lionzxy.mobiusbleperfomance.ui.screens.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras

@Suppress("UNCHECKED_CAST")
class DeviceViewModelFactory(
    private val address: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = checkNotNull(extras[APPLICATION_KEY])

        return DeviceViewModel(
            application = application,
            address = address
        ) as T
    }
}