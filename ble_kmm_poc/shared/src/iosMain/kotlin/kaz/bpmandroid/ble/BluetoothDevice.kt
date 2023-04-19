package kaz.bpmandroid.ble

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreBluetooth.*
import platform.Foundation.NSData
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