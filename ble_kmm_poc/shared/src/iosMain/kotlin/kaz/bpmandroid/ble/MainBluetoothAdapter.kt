package kaz.bpmandroid.ble

import kaz.bpmandroid.base.IBluetoothManager
import kaz.bpmandroid.util.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import platform.CoreBluetooth.*
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.NSUUID
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue


actual class MainBluetoothAdapter {

    actual var listener: IBluetoothManager? = null

    private var isReady = false
    private var whenReady: ((MainBluetoothAdapter) -> Unit)? = null
    private var connectedDevice: BluetoothDevice? = null
    private var discoveredServices: ArrayList<BleService> = ArrayList()
    var connectedPeripheral: CBPeripheral? = null
    var moAdvertisementData: Map<Any?, *>? = null


    // list of CBCharacteristics to discover
    private var discoverCharacteristics: MutableList<CBCharacteristic> = mutableListOf()
    private val delegateImpl =
        object : NSObject(), CBCentralManagerDelegateProtocol, CBPeripheralDelegateProtocol {
            override fun centralManagerDidUpdateState(central: CBCentralManager) {
                println("\n centralManagerDidUpdateState" + central.state + CBCentralManagerStatePoweredOn)
                when (central.state) {
                    CBCentralManagerStatePoweredOn -> whenReady?.invoke(this@MainBluetoothAdapter)
                }
            }

            override fun centralManager(
                central: CBCentralManager,
                didDiscoverPeripheral: CBPeripheral,
                advertisementData: Map<Any?, *>,
                RSSI: NSNumber
            ) {
                println("\n peripheral didDiscoverPeripheral")
                moAdvertisementData = advertisementData
                val device = BluetoothDevice(didDiscoverPeripheral)
                onDeviceReceived?.invoke(device)
            }

            override fun centralManager(
                central: CBCentralManager,
                didConnectPeripheral: CBPeripheral
            ) {
                stopScan()
                println("\n peripheral didConnectPeripheral")
                println("\n connect device call after$didConnectPeripheral")
                connectedDevice = BluetoothDevice(didConnectPeripheral)

                connectedDevice.also {
                    println("\n peripheral pass connect device")
                    connectedPeripheral = connectedDevice!!.peripheral
                    listener?.onStateChange(BleState.Connected(connectedDevice!!))
                }
            }

            override fun peripheral(peripheral: CBPeripheral, didDiscoverServices: NSError?) {
                val device = getDeviceOrThrow()
                for (service in peripheral.cbServices) {
                    println("didDiscoverServices" + service.UUID.UUIDString)
                    var loBleService = BleService(service.UUID.UUIDString, device)
                    discoveredServices.add(loBleService)
                }
                runBlocking {
                    delay(200)
                    listener?.onStateChange(BleState.ServicesDiscovered(device, discoveredServices))
                }


                /* discoveredServices = peripheral.cbServices.map { BleService(it.UUID.UUIDString, device) }.also {

                 }*/
            }

            override fun peripheral(
                peripheral: CBPeripheral,
                didDiscoverCharacteristicsForService: CBService,
                error: NSError?
            ) {
                println("\n didDiscoverCharacteristicsForService")
                val device = getDeviceOrThrow()
                val serviceId = didDiscoverCharacteristicsForService.UUID.UUIDString
                val service = checkNotNull(discoveredServices).first { it.id == serviceId }
                val chars = didDiscoverCharacteristicsForService.cbChars.map { char ->
                    BleCharacteristic(
                        char,
                        service
                    )
                }
                val characteristics = didDiscoverCharacteristicsForService.characteristics()
                if (characteristics != null && characteristics.count() > 0) {
                    for (characteristic in characteristics!!) {
                        peripheral.setNotifyValue(
                            true, characteristic as CBCharacteristic
                        )
                        if (characteristic.UUID.UUIDString.contains(
                                Utils.BPM_NUM_READINGS_CHAR,
                                true
                            )
                        ) {
                            peripheral.readValueForCharacteristic(characteristic)
                        } else if (characteristic.UUID.UUIDString.contains(
                                Utils.BPM_PAIRING_CHAR,
                                true
                            )
                        ) {
                            peripheral.readValueForCharacteristic(characteristic)
                        }
                        discoverCharacteristics.add(characteristic as CBCharacteristic)
                    }
                }
                listener?.onStateChange(BleState.CharacteristicsDiscovered(device, chars))
            }

            override fun peripheral(
                peripheral: CBPeripheral,
                didUpdateValueForCharacteristic: CBCharacteristic,
                error: NSError?
            ) {
                println("\n didUpdateValueForCharacteristi6c")
                println("didUpdateValueForCharacteristic size count ${discoveredServices!!.size}")
                var service = didUpdateValueForCharacteristic.service
                var chUUID = didUpdateValueForCharacteristic.UUID.UUIDString
                println("didUpdateValueForCharacteristic chUUID : " + chUUID)

                var loBleService = BleService(service!!.UUID.UUIDString(), getDeviceOrThrow())
                println("didUpdateValueForCharacteristic service UUID : " + service.UUID.UUIDString)
                if ((service.UUID.UUIDString == Utils.UUID_KAZ_BPM_SERVICE) || (service.UUID.UUIDString == Utils.UUID_BLOOD_PRESSURE_SERVICE)) {
                    println("didUpdateValueForCharacteristic onStateChange : " + chUUID)
                    if (chUUID.equals(Utils.BPM_USER_NAME_CHAR, true)) {
                        listener?.onStateChange(
                            BleState.CharacteristicWrite(
                                getDeviceOrThrow(),
                                BleCharacteristic(
                                    didUpdateValueForCharacteristic,
                                    loBleService
                                )
                            )
                        )
                    } else if (chUUID.equals(Utils.BPM_NUM_READINGS_CHAR, true)) {
                        listener?.onStateChange(
                            BleState.CharacteristicRead(
                                getDeviceOrThrow(),
                                BleCharacteristic(
                                    didUpdateValueForCharacteristic,
                                    loBleService
                                )
                            )
                        )
                    } else {
                        listener?.onStateChange(
                            BleState.CharacteristicChanged(
                                getDeviceOrThrow(),
                                BleCharacteristic(
                                    didUpdateValueForCharacteristic,
                                    loBleService
                                )
                            )
                        )
                    }

                }
            }

            // This method is called when a device gets disconnected
            @Suppress("CONFLICTING_OVERLOADS")
            override fun centralManager(
                central: CBCentralManager,
                didDisconnectPeripheral: CBPeripheral,
                error: NSError?
            ) {
                connectedDevice = BluetoothDevice(didDisconnectPeripheral)
                connectedDevice.also {
                    connectedPeripheral = null
                    println("\n peripheral pass connect device")
                    listener?.onStateChange(BleState.Disconnected(connectedDevice!!))
                }
            }
        }

    private var manager: CBCentralManager? = null
    private var onDeviceReceived: ((BluetoothDevice) -> Unit)? = null

    init {
        manager = CBCentralManager(delegateImpl, dispatch_get_main_queue())
        manager?.delegate = delegateImpl
    }

    private fun getDeviceOrThrow(): BluetoothDevice {
        // println("\n getDeviceOrThrow$connectedDevice")
        val device = connectedDevice
        //  println("\n getDeviceOrThrow1$device")
        check(device != null) { "Device is not connected!" }
        return device
    }

    ///////////////////////////////////////////////////////////////////////////
    // Actual declarations
    ///////////////////////////////////////////////////////////////////////////

    actual fun discoverDevices(callback: (BluetoothDevice) -> Unit) {
        val bpmServiceUUID = CBUUID.UUIDWithString(Utils.UUID_BLOOD_PRESSURE_SERVICE)
        println("\n discoverDevices")
        if (isReady) {
            println("\n discoverDevices isReady")
            manager?.scanForPeripheralsWithServices(listOf(bpmServiceUUID), null)
            onDeviceReceived = callback
        } else {
            whenReady = {
                println("\n discoverDevices whenReady")
                whenReady = null
                manager?.scanForPeripheralsWithServices(listOf(bpmServiceUUID), null)
                onDeviceReceived = callback
            }
        }
    }

    actual fun stopScan() {
        println("\n stopScan")
        manager?.stopScan()
        onDeviceReceived = null
    }

    actual fun findBondedDevices(callback: (List<BluetoothDevice>) -> Unit) {
        println("\n findBondedDevices")
        manager?.retrieveConnectedPeripheralsWithServices(listOf("180A"))
            ?.mapNotNull { it as? CBPeripheral }
            ?.map { BluetoothDevice(it) }
            ?.also(callback)
    }

    actual fun connect(device: BluetoothDevice) {
        println("\n connect device call before" + device.peripheral)

        manager?.connectPeripheral(device.peripheral, null)
    }

    actual fun disconnect() {
        println("\n disconnect")
        manager?.cancelPeripheralConnection(getDeviceOrThrow().peripheral)
    }

    actual fun discoverServices() {
        println("\n discoverServices")
        getDeviceOrThrow().peripheral.delegate = delegateImpl
        getDeviceOrThrow().peripheral.discoverServices(null)
    }

    actual fun discoverCharacteristics(service: BleService) {
        println("\n discoverCharacteristics")
        val UUID_CH_BP_MEASUREMENT = CBUUID.UUIDWithString("2DB34480-BCE5-4BB7-9F56-55BD202317C5")
        val UUID_CH_BP_INTERMEDIATE_CUFF_PRESSURE =
            CBUUID.UUIDWithString("77AB1C51-2F6B-4A7B-81AF-5E301984BF13")
        val UUID_CH_BP_FEATURE = CBUUID.UUIDWithString("C3A2ED78-B3F1-4086-AB3B-BF3C5685A745")
        val UUID_CH_BPM_PAIRING = CBUUID.UUIDWithString("DEFFE5DE-90B2-4D5C-9888-76BDAA950C78")
        val UUID_CH_BPM_USER_NAME = CBUUID.UUIDWithString("FCE1BE76-A487-4331-96B0-53C8097E306C")
        val s = service.device.peripheral.cbServices.first { it.UUID.UUIDString == service.id }
        service.device.peripheral.discoverCharacteristics(null, s)
//        service.device.peripheral.discoverCharacteristics(listOf(UUID_CH_BPM_PAIRING, UUID_CH_BPM_USER_NAME, UUID_CH_BP_MEASUREMENT,UUID_CH_BP_INTERMEDIATE_CUFF_PRESSURE,UUID_CH_BP_FEATURE ), s)
    }

    actual fun setNotificationEnabled(char: BleCharacteristic) {
        println("\n setNotificationEnabled")
        val cbService =
            char.service.device.peripheral.cbServices.first { it.UUID.UUIDString == char.service.id }
        val c = cbService.cbChars.first { it.UUID.UUIDString == char.id }
        char.service.device.peripheral.setNotifyValue(true, c)
    }

    actual fun setNotificationDisabled(char: BleCharacteristic) {
        println("\n setNotificationDisabled")
        val cbService =
            char.service.device.peripheral.cbServices.first { it.UUID.UUIDString == char.service.id }
        val c = cbService.cbChars.first { it.UUID.UUIDString == char.id }
        char.service.device.peripheral.setNotifyValue(true, c)
    }

    actual fun randomUUID(): String {
        return NSUUID().UUIDString()
    }

    actual fun getBPMHash(uuidString: String?): Long {
        return 0L
    }

    actual fun characteristicsRead(
        device: BluetoothDevice,
        service: BleService,
        serviceUUID: String,
        charUUID: String
    ) {
        println("characteristicsRead called")
        val characteristics = discoverCharacteristics
        if (characteristics != null && characteristics.count() > 0) {
            for (characteristic in characteristics!!) {
                val cbCharacteristic = characteristic as CBCharacteristic
                if (characteristic.UUID.UUIDString.contains(charUUID, true)) {
                    device.peripheral.readValueForCharacteristic(characteristic)
                }
            }
        }

    }

    actual fun characteristicWrite(
        device: BluetoothDevice,
        service: BleService,
        payload: ByteArray,
        serviceUUID: String,
        charUUID: String
    ) {
        println("\n onCharacteristicWrite")
//        val hexStringFromByteArray = payload.toHexString()
        val characteristics = discoverCharacteristics
        if (characteristics != null && characteristics.count() > 0) {
            for (characteristic in characteristics!!) {
                val cbCharacteristic = characteristic as CBCharacteristic
                if (characteristic.UUID.UUIDString.contains(charUUID, true)) {
                    println("writeCharacteristic init")
//                    writeCharacteristic = characteristic
                    val data = payload.toNSData()
                    device.peripheral.writeValue(
                        data,
                        characteristic,
                        CBCharacteristicWriteWithResponse
                    )
                }
            }
        }
    }
}
