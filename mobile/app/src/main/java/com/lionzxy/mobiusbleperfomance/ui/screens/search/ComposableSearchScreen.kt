package com.lionzxy.mobiusbleperfomance.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lionzxy.mobiusbleperfomance.ble.scanner.DiscoveredBluetoothDevice

@Composable
fun ComposableSearchScreen(
    onConnect: (address: String) -> Unit
) {
    val searchViewModel = viewModel<SearchViewModel>()
    val devices by searchViewModel.getDevices().collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(
            items = devices,
            key = { it.address }
        ) { device ->
            ComposableSearchElement(
                device = device,
                onConnect = {
                    onConnect(device.address)
                }
            )
        }
    }
}

@Composable
private fun ComposableSearchElement(
    device: DiscoveredBluetoothDevice,
    onConnect: () -> Unit
) = Card(Modifier.padding(horizontal = 14.dp)) {
    Row(Modifier.padding(4.dp)) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = device.name ?: "Device without name")
            Text(text = device.address)
        }
        Button(onClick = onConnect) {
            Text("Connect")
        }
    }
}