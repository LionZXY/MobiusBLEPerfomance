package com.lionzxy.mobiusbleperfomance.ui.screens.device

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lionzxy.mobiusbleperfomance.ble.device.MobiusBLEManager
import com.lionzxy.mobiusbleperfomance.ble.scanner.MobiusBLEScannerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DeviceViewModel(
    application: Application,
    private val address: String
) : AndroidViewModel(application) {
    private val scanner = MobiusBLEScannerImpl(application)
    private val bleManager = MobiusBLEManager(application, viewModelScope)

    private val deviceScreenStateFlow =
        MutableStateFlow<DeviceScreenState>(DeviceScreenState.Connecting)

    private var job: Job? = null

    init {
        viewModelScope.launch {
            val device = scanner.findDeviceById(address)
                .first()
            bleManager.connectToDevice(device.device)
            deviceScreenStateFlow.emit(DeviceScreenState.Connected)
        }
    }

    fun getState(): StateFlow<DeviceScreenState> = deviceScreenStateFlow.asStateFlow()
    fun getTxSpeed(): StateFlow<Long> = bleManager.txSpeedMeter.getSpeed()
    fun getRxSpeed(): StateFlow<Long> = bleManager.rxSpeedMeter.getSpeed()

    fun startTxBenchmark() {
        job = viewModelScope.launch(Dispatchers.Default) {
            deviceScreenStateFlow.emit(DeviceScreenState.StartedBenchmark)
            bleManager.startTxBenchmark(this)
        }
    }

    fun startRxBenchmark() {
        job = viewModelScope.launch {
            deviceScreenStateFlow.emit(DeviceScreenState.StartedBenchmark)
            bleManager.startRxBenchmark(this)
        }
    }

    fun stop() {
        viewModelScope.launch {
            job?.cancelAndJoin()
            deviceScreenStateFlow.emit(DeviceScreenState.Connected)
        }
    }
}