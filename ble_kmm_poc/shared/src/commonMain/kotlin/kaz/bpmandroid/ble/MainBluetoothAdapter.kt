package kaz.bpmandroid.ble

import kaz.bpmandroid.base.IBluetoothManager

expect class MainBluetoothAdapter {

    var listener: IBluetoothManager?

    fun discoverDevices(callback: (BluetoothDevice) -> Unit)

    fun stopScan()

    fun findBondedDevices(callback: (List<BluetoothDevice>) -> Unit)

    fun connect(device: BluetoothDevice)

    fun disconnect()

    fun discoverServices()

    fun discoverCharacteristics(service: BleService)

    fun setNotificationEnabled(char: BleCharacteristic)

    fun setNotificationDisabled(char: BleCharacteristic)

    fun onCharacteristicsRead(
        device: BluetoothDevice,
        service: BleService,
        serviceUUID: String,
        charUUID: String
    )

    fun onCharacteristicWrite(
        device: BluetoothDevice,
        service: BleService,
        payload: ByteArray,
        serviceUUID: String,
        charUUID: String
    )

    fun randomUUID(): String

    fun getBPMHash(uuidString: String?): Long
}


sealed class BleState {
    data class Connected(val device: BluetoothDevice) : BleState()
    data class Disconnected(val device: BluetoothDevice) : BleState()
    data class ServicesDiscovered(val device: BluetoothDevice, val services: List<BleService>) :
        BleState()

    data class CharacteristicsDiscovered(
        val device: BluetoothDevice, val chars: List<BleCharacteristic>
    ) : BleState()

    data class CharacteristicChanged(
        val device: BluetoothDevice, val characteristic: BleCharacteristic
    ) : BleState()

    data class CharacteristicWrite(
        val device: BluetoothDevice, val characteristic: BleCharacteristic
    ) : BleState()

    data class CharacteristicRead(
        val device: BluetoothDevice, val characteristic: BleCharacteristic
    ) : BleState()
}

interface BluetoothAdapterListener {
    fun onStateChange(state: BleState)
}