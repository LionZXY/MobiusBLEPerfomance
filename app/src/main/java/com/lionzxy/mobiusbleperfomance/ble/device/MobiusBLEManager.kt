package com.lionzxy.mobiusbleperfomance.ble.device

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import no.nordicsemi.android.ble.annotation.WriteType
import no.nordicsemi.android.ble.ktx.splitWithProgressFlow
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import java.util.UUID

private const val TAG = "MobiusBLEManager"

class MobiusBLEManager(
    context: Context,
    scope: CoroutineScope
) : BleManager(context) {
    private val characteristics: MutableMap<UUID, BluetoothGattCharacteristic> = mutableMapOf()
    val txSpeedMeter = SpeedMeter(scope)
    val rxSpeedMeter = SpeedMeter(scope)

    override fun initialize() {
        if (!isBonded) {
            ensureBond().enqueue()
        }
    }

    override fun log(priority: Int, message: String) {
        Log.i(TAG, message)
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        gatt.services.forEach { service ->
            service.characteristics.forEach {
                Log.d(TAG, "Characteristic for service ${service.uuid}: ${it.uuid}")
                characteristics[it.uuid] = it
            }
        }
        return true
    }

    override fun onDeviceReady() {
        // Set up MTU
        requestMtu(Constants.MTU)
            .enqueue()

        requestConnectionPriority(
            ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH
        ).enqueue()

        val rxChar = characteristics[Constants.BLESerialService.RX]

        setNotificationCallback(rxChar).with { _, data ->
            rxSpeedMeter.onReceiveBytes(data.size())
        }
        enableIndications(rxChar).enqueue()
    }


    suspend fun startRxBenchmark(benchScope: CoroutineScope) {
        val rxStartChar = characteristics[Constants.BLESerialService.START_BRUTEFORCE]
        writeCharacteristic(
            rxStartChar,
            byteArrayOf(1),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).enqueue()

        benchScope.launch {
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) {
                    writeCharacteristic(
                        rxStartChar,
                        byteArrayOf(0),
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    ).await()
                }
            }
        }
    }

    suspend fun startTxBenchmark(scope: CoroutineScope) {
        val txChar = characteristics[Constants.BLESerialService.TX]!!
        sendBytes(txChar, scope)
    }

    private suspend fun sendBytes(
        txChar: BluetoothGattCharacteristic,
        scope: CoroutineScope
    ): Unit = withContext(Dispatchers.Main) {
        val bytes = ByteArrayGenerator.generate()
        writeCharacteristic(txChar, bytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .done {
                txSpeedMeter.onReceiveBytes(bytes.size)
                if (scope.isActive) {
                    scope.launch {
                        sendBytes(txChar, scope)
                    }
                }
            }
            .enqueue()
    }


    suspend fun connectToDevice(device: BluetoothDevice) = withContext(Dispatchers.Default) {
        connect(device)
            .useAutoConnect(true)
            .enqueue()

        // Wait until device is really connected
        stateAsFlow().filter { it is ConnectionState.Initializing }.first()
    }
}