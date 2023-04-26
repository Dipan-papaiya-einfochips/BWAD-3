package kaz.bpmandroid.ble

import kotlinx.cinterop.*
import platform.CoreBluetooth.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

actual data class BluetoothDevice(
    actual val id: String,
    actual val name: String,
    internal val peripheral: CBPeripheral
)

internal fun BluetoothDevice(peripheral: CBPeripheral) = BluetoothDevice(
    id = peripheral.identifier.UUIDString,
    name = peripheral.name.orEmpty(),
    peripheral = peripheral
)

internal val CBPeripheral.cbServices get() = services().orEmpty().map { it as CBService }
internal val CBService.cbChars get() = characteristics().orEmpty().map { it as CBCharacteristic }

internal fun BleCharacteristic(char: CBCharacteristic, service: BleService): BleCharacteristic {
    val id = char.UUID.UUIDString
    val value = char.value?.toByteArray()
    return BleCharacteristic(id, value, service)
}

internal fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    usePinned {
        memcpy(it.addressOf(0), bytes, length)
    }
}

internal fun ByteArray.toNSData(): NSData = memScoped {
    println("toNSData cALLED")
    NSData.create(
        bytes = allocArrayOf(this@toNSData),
        length = size.convert()
    )
}

internal fun ByteArray.toHexString(
    separator: String? = null,
    prefix: String? = null,
    lowerCase: Boolean = false
): String {
    if (size == 0) return ""
    val hexCode = if (lowerCase) "0123456789abcdef" else "0123456789ABCDEF"
    val capacity = size * (2 + (prefix?.length ?: 0)) + (size - 1) * (separator?.length ?: 0)
    val r = StringBuilder(capacity)
    for (b in this) {
        if (separator != null && r.isNotEmpty()) r.append(separator)
        if (prefix != null) r.append(prefix)
        r.append(hexCode[b.toInt() shr 4 and 0xF])
        r.append(hexCode[b.toInt() and 0xF])
    }
    return r.toString()
}
