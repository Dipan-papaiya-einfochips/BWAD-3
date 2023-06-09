package kaz.bpmandroid.ble

expect class BluetoothDevice {
    val id: String
    var name: String
}

data class BleService(
    val id: String,
    val device: BluetoothDevice
)

data class BleCharacteristic(
    val id: String,
    val value: ByteArray?,
    val service: BleService
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BleCharacteristic

        if (id != other.id) return false
        if (value != null) {
            if (other.value == null) return false
            if (!value.contentEquals(other.value)) return false
        } else if (other.value != null) return false
        if (service != other.service) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (value?.contentHashCode() ?: 0)
        result = 31 * result + service.hashCode()
        return result
    }
}