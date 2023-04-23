package com.lionzxy.mobiusbleperfomance.ble.device

import java.util.UUID

object Constants {
    const val PACKET_SIZE = 30000
    const val MTU = 414

    // BLE serial service uuids: service uuid and characteristics uuids
    object BLESerialService {
        val SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")

        val TX: UUID = UUID.fromString("19ed82ae-ed21-4c9d-4145-228e62fe0000")
        val RX: UUID = UUID.fromString("19ed82ae-ed21-4c9d-4145-228e61fe0000")
        val START_BRUTEFORCE: UUID = UUID.fromString("19ed82ae-ed21-4c9d-4145-228e63fe0000")
    }
}