package com.lionzxy.mobiusbleperfomance.ui.screens.device

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ComposableDeviceScreen(address: String) {
    val deviceViewModel = viewModel<DeviceViewModel>(factory = DeviceViewModelFactory(address))
    val state by deviceViewModel.getState().collectAsState()
    Crossfade(state) {
        Column {
            when (it) {
                DeviceScreenState.Connected -> ComposableConnectedState(deviceViewModel)
                DeviceScreenState.Connecting -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Connecting...")
                }

                DeviceScreenState.StartedBenchmark -> ComposableBenchmarkState(deviceViewModel)
            }
        }
    }
}

@Composable
fun ComposableBenchmarkState(
    deviceViewModel: DeviceViewModel
) = Column(
    modifier = Modifier.fillMaxSize()
) {
    Box(modifier = Modifier.weight(1f)) {
        Button(onClick = deviceViewModel::stop) {
            Text("Stop")
        }
    }
    ComposableSpeed(deviceViewModel)
}

@Composable
fun ComposableConnectedState(
    deviceViewModel: DeviceViewModel
) = Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Button(onClick = deviceViewModel::startTxBenchmark) {
        Text(text = "Start TX benchmark")
    }
    Button(onClick = deviceViewModel::startRxBenchmark) {
        Text(text = "Start RX benchmark")
    }
}