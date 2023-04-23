package com.lionzxy.mobiusbleperfomance.ui.screens.device

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun ColumnScope.ComposableSpeed(
    deviceViewModel: DeviceViewModel
) {
    val receiveBytesInSec by deviceViewModel.getRxSpeed().collectAsState()
    val transmitBytesInSec by deviceViewModel.getTxSpeed().collectAsState()


    var maxReceiveSpeed by remember { mutableStateOf(0L) }
    var maxTransmitSpeed by remember { mutableStateOf(0L) }

    if (receiveBytesInSec > maxReceiveSpeed) {
        maxReceiveSpeed = receiveBytesInSec
    }

    if (transmitBytesInSec > maxTransmitSpeed) {
        maxTransmitSpeed = transmitBytesInSec
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val rx = Formatter.formatFileSize(LocalContext.current, receiveBytesInSec)
        val tx = Formatter.formatFileSize(LocalContext.current, transmitBytesInSec)
        Text("Receive speed: $rx/s")
        Text("Send speed: $tx/s")
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val rx = Formatter.formatFileSize(LocalContext.current, maxReceiveSpeed)
        val tx = Formatter.formatFileSize(LocalContext.current, maxTransmitSpeed)
        Text("Max: $rx/s")
        Text("Max: $tx/s")
    }
}