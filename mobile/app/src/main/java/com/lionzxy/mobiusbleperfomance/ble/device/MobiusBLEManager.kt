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
import no.nordicsemi.android.ble.PhyRequest
import no.nordicsemi.android.ble.annotation.WriteType
import no.nordicsemi.android.ble.callback.WriteProgressCallback
import no.nordicsemi.android.ble.ktx.getCharacteristic
import no.nordicsemi.android.ble.ktx.splitWithProgressFlow
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import java.util.UUID

private const val TAG = "MobiusBLEManager"

class MobiusBLEManager(
    context: Context,
    scope: CoroutineScope
) : BleManager(context) {
    private var txChar: BluetoothGattCharacteristic? = null
    private var rxChar: BluetoothGattCharacteristic? = null
    private var rxStartChar: BluetoothGattCharacteristic? = null
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
            }
        }

        val service = gatt.getService(Constants.BLESerialService.SERVICE_UUID)
        txChar = service.getCharacteristic(Constants.BLESerialService.TX)
        rxChar = service.getCharacteristic(Constants.BLESerialService.RX)
        rxStartChar = service.getCharacteristic(Constants.BLESerialService.START_BRUTEFORCE)
        return true
    }

    override fun onDeviceReady() {
        // Set up MTU
        /*
        requestMtu(Constants.MTU)
            .enqueue()

        requestConnectionPriority(
            ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH
        ).enqueue()

        setPreferredPhy(
            PhyRequest.PHY_LE_2M_MASK,
            PhyRequest.PHY_LE_2M_MASK,
            PhyRequest.PHY_OPTION_NO_PREFERRED
        ).enqueue()*/

        setNotificationCallback(rxChar).with { _, data ->
            rxSpeedMeter.onReceiveBytes(data.size())
        }
        enableIndications(rxChar).enqueue()
    }


    suspend fun startRxBenchmark(benchScope: CoroutineScope) {
        writeCharacteristic(
            rxStartChar,
            byteArrayOf(1),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).fail { device, status ->
            Log.i(TAG, "Failed write char $status")
        }.done {
            Log.i(TAG, "Sucs write char")
        }.enqueue()

        benchScope.launch {
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable + Dispatchers.Default) {
                    writeCharacteristic(
                        rxStartChar,
                        byteArrayOf(0),
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    ).await()
                }
            }
        }
    }

    fun startTxBenchmark(scope: CoroutineScope) {
        sendBytes(scope)
    }

    private fun sendBytes(
        scope: CoroutineScope
    ) {
        val bytes = ByteArrayGenerator.generate()
        writeCharacteristic(txChar, bytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .split(WriteProgressCallback { device, data, index ->
                if (data != null) {
                    txSpeedMeter.onReceiveBytes(data.size)
                }
            })
            .done {
                Log.i(TAG, "Bytes send!")
                if (scope.isActive) {
                    sendBytes(scope)
                }
            }
            .fail { device, status ->
                Log.i(TAG, "Fail send request with $status")
            }
            .enqueue()
        Log.i(TAG, "Schedule bytes to send...")
    }


    suspend fun connectToDevice(device: BluetoothDevice) = withContext(Dispatchers.Default) {
        connect(device)
            .useAutoConnect(true)
            .enqueue()

        // Wait until device is really connected
        stateAsFlow().filter { it is ConnectionState.Initializing }.first()
    }
}