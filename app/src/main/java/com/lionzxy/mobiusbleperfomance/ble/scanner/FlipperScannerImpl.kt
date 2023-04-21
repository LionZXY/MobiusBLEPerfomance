package com.lionzxy.mobiusbleperfomance.ble.scanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanSettings

private const val TAG = "MobiusBLEScanner"

class MobiusBLEScannerImpl(
    private val context: Context
) {
    private val scanner: BluetoothLeScannerCompat = BluetoothLeScannerCompat.getScanner()
    private val bluetoothManager: BluetoothManager? =
        ContextCompat.getSystemService(context, BluetoothManager::class.java)

    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.getBluetoothAdapter()

    fun findDevices(): Flow<Iterable<DiscoveredBluetoothDevice>> {
        val devices = ArrayList(getAlreadyConnectedDevices())
        val mutex = Mutex()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException(
                "You should request BLUETOOTH_CONNECT before on Android API > 31"
            )
        }

        return merge(
            devices.asFlow(),
            scanner.scanFlow(provideSettings(), provideFilterForDefaultScan()).map {
                DiscoveredBluetoothDevice(it)
            }
        ).filter {
            it.name != null
        }.map { discoveredBluetoothDevice ->
            var mutableDevicesList: List<DiscoveredBluetoothDevice> = emptyList()
            mutex.withLock {
                val alreadyExistDBD = devices.getOrNull(
                    devices.indexOf(discoveredBluetoothDevice)
                )
                if (alreadyExistDBD != null) {
                    val scanResult = discoveredBluetoothDevice.scanResult
                    if (scanResult != null) {
                        alreadyExistDBD.update(scanResult)
                    }
                } else {
                    Log.i(TAG, "Find new device $discoveredBluetoothDevice")
                    devices.add(discoveredBluetoothDevice)
                }
                mutableDevicesList = devices.toList()
            }
            return@map mutableDevicesList
        }
    }

    fun findDeviceById(deviceId: String): Flow<DiscoveredBluetoothDevice> {
        val boundedDevice = getAlreadyBoundedDevice(deviceId)
        if (boundedDevice != null) {
            return flowOf(boundedDevice)
        }
        val connectedDevice = getAlreadyConnectedDevices().filter {
            it.address == deviceId
        }.firstOrNull()
        if (connectedDevice != null) {
            return flowOf(connectedDevice)
        }
        return scanner.scanFlow(provideSettings(), provideFilterForFindById(deviceId))
            .map { DiscoveredBluetoothDevice(it) }
    }

    /**
     * It's a bit of a dirty hack. Flipper does not alert when it is already connected to a device.
     * Android does not allow us to find a device that is already connected.
     * So we take the connected devices and search among them.
     */
    private fun getAlreadyConnectedDevices(): List<DiscoveredBluetoothDevice> {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return emptyList()
        }
        if (bluetoothManager == null) {
            return emptyList()
        }

        return bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).filter {
            it.name != null
        }.map {
            DiscoveredBluetoothDevice(
                device = it,
                nameInternal = it.name,
                rssiInternal = 0,
                previousRssi = 0
            )
        }
    }

    private fun getAlreadyBoundedDevice(deviceId: String): DiscoveredBluetoothDevice? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        return bluetoothAdapter.bondedDevices.filter {
            it.address == deviceId
        }.map {
            DiscoveredBluetoothDevice(
                device = it,
                nameInternal = it.name,
                rssiInternal = 0,
                previousRssi = 0
            )
        }.firstOrNull()
    }

    private fun provideSettings(): ScanSettings {
        return ScanSettings.Builder()
            .setLegacy(false)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setUseHardwareBatchingIfSupported(true)
            .build()
    }

    private fun provideFilterForDefaultScan(): List<ScanFilter> {
        return emptyList()
    }

    private fun provideFilterForFindById(deviceId: String): List<ScanFilter> {
        return listOf(ScanFilter.Builder().setDeviceAddress(deviceId).build())
    }
}


private fun BluetoothManager?.getBluetoothAdapter(): BluetoothAdapter {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            @Suppress("DEPRECATION")
            this?.adapter ?: BluetoothAdapter.getDefaultAdapter()
        }

        else -> {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }
    }
}
