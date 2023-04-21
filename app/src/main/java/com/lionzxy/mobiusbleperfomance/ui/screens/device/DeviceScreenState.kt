package com.lionzxy.mobiusbleperfomance.ui.screens.device

sealed class DeviceScreenState {
    object Connecting : DeviceScreenState()

    object Connected : DeviceScreenState()

    object StartedBenchmark : DeviceScreenState()
}