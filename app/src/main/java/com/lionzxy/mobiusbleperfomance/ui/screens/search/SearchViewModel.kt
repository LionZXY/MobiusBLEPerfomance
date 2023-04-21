package com.lionzxy.mobiusbleperfomance.ui.screens.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lionzxy.mobiusbleperfomance.ble.scanner.DiscoveredBluetoothDevice
import com.lionzxy.mobiusbleperfomance.ble.scanner.MobiusBLEScannerImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val scanner = MobiusBLEScannerImpl(application)

    private val discoveredDevices = MutableStateFlow<List<DiscoveredBluetoothDevice>>(emptyList())

    init {
        scanner.findDevices().onEach {
            discoveredDevices.emit(it.toList())
        }.launchIn(viewModelScope)
    }

    fun getDevices(): StateFlow<List<DiscoveredBluetoothDevice>> = discoveredDevices.asStateFlow()
}