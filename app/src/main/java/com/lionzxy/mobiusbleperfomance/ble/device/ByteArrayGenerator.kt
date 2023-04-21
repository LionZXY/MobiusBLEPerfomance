package com.lionzxy.mobiusbleperfomance.ble.device

import com.lionzxy.mobiusbleperfomance.ble.device.Constants.PACKET_SIZE

object ByteArrayGenerator {
    private var lastByteGenerated: Byte = 0

    @Synchronized
    fun generate(): ByteArray {
        val bytes = ByteArray(PACKET_SIZE) {
            (lastByteGenerated + it).mod(Byte.MAX_VALUE)
        }
        lastByteGenerated = bytes.last()
        return bytes
    }
}