package kaz.bpmandroid.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import java.lang.reflect.Array.getChar
import java.nio.ByteBuffer
import java.util.*
import java.util.zip.CRC32
import java.util.zip.Checksum
import kaz.bpmandroid.base.IBluetoothManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

@SuppressLint("MissingPermission")
actual class MainBluetoothAdapter(
    private val context: Context
) : BluetoothGattCallback() {

    private val mainThreadHandler = Handler(Looper.getMainLooper())

    private val bluetoothManager: BluetoothManager
        get() = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val bluetoothAdapter: android.bluetooth.BluetoothAdapter
        get() = bluetoothManager.adapter

    private var handler: ((BluetoothDevice) -> Unit)? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val device = result?.device
            /*     result.scanRecord.bytes*/
            if (device != null) {
                handler?.invoke(BluetoothDevice(device.address, device.name ?: "", device))
                stopScan()
            }
        }
    }

    private var connectedDevice: BluetoothDevice? = null
    private var discoveredServices: List<BleService>? = null

    ///////////////////////////////////////////////////////////////////////////
    // Actual declarations
    ///////////////////////////////////////////////////////////////////////////

    actual var listener: IBluetoothManager? = null


    actual fun discoverDevices(callback: (BluetoothDevice) -> Unit) {
        handler = callback
        var mFiltersList = ArrayList<ScanFilter>()
        mFiltersList.add(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString("56484AAE-A8EB-4A97-AC19-A8EA6373E05A"))
                .build()
        )
        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bluetoothAdapter.bluetoothLeScanner?.startScan(mFiltersList, scanSettings, scanCallback)
    }

    actual fun stopScan() {
        bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
        handler = null
    }

    actual fun findBondedDevices(callback: (List<BluetoothDevice>) -> Unit) {
        bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).filterNotNull()
            .map { BluetoothDevice(it.address, it.name, it) }.also(callback)
    }

    actual fun connect(device: BluetoothDevice) {
        val gattCallback = KtGattCallback(this, onConnectionStateChange = { _, _, newState ->
            mainThreadHandler.post {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        connectedDevice = device
                        listener?.onStateChange(BleState.Connected(device))
                    }
                    BluetoothGatt.STATE_DISCONNECTED -> {
                        listener?.onStateChange(BleState.Disconnected(getDeviceOrThrow()))
                        connectedDevice = null
                    }
                }
            }
        })
        device.gatt = device.androidDevice.connectGatt(context, false, gattCallback)
    }

    actual fun disconnect() {
        connectedDevice?.gatt?.disconnect()
        connectedDevice = null
        discoveredServices = null
    }

    actual fun discoverServices() {
        val device = getDeviceOrThrow()
        device.gatt?.discoverServices()
    }

    actual fun discoverCharacteristics(service: BleService) {
        val device = getDeviceOrThrow()
        val bleService = service.device.gatt?.run {
            getService(UUID.fromString(service.id)) ?: error("Service not found ${service.id}")
        } ?: error("Device not connected ${service.device.id}")
        val chars = bleService.characteristics.map { BleCharacteristic(it, service) }
        listener?.onStateChange(BleState.CharacteristicsDiscovered(device, chars))
    }

    actual fun setNotificationEnabled(char: BleCharacteristic) {
        val gatt = getGatt(char.service.device)
        val bleChar = getChar(gatt, char)
        check(gatt.setCharacteristicNotification(bleChar, true)) { "Characteristic not written!" }
        println("setNotificationEnabled " + " ${char.id} " + bleChar.descriptors)
        updateDescriptors(bleChar, gatt)
    }

    actual fun setNotificationDisabled(char: BleCharacteristic) {
        val gatt = getGatt(char.service.device)
        val bleChar = getChar(gatt, char)
        check(gatt.setCharacteristicNotification(bleChar, false)) { "Characteristic not written!" }

    }

    fun updateDescriptors(bleChar: BluetoothGattCharacteristic, gatt: BluetoothGatt) {
        if (bleChar.descriptors.size > 0) {
            val descriptor = bleChar.getDescriptor(bleChar.descriptors[0].uuid)
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

            var liResult = 1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                liResult = gatt.writeDescriptor(
                    descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                )
            } else {
                var lbResult = gatt.writeDescriptor(descriptor)
                if (lbResult) {
                    liResult = 0
                }

            }

            println("Descriptor done $liResult ${bleChar.descriptors[0].uuid}")
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Gatt Callback
    ///////////////////////////////////////////////////////////////////////////

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        mainThreadHandler.post {
            val device = getDeviceOrThrow()
            discoveredServices = gatt.services.map { BleService(it.uuid.toString(), device) }.also {
                listener?.onStateChange(BleState.ServicesDiscovered(device, it))
            }
        }
    }

    /* override fun onCharacteristicChanged(
         gatt: BluetoothGatt,
         characteristic: BluetoothGattCharacteristic
     ) {
         mainThreadHandler.post {
             val device = getDeviceOrThrow()
             val services = checkNotNull(discoveredServices)
             val service = services.first { it.id == characteristic.service.uuid.toString() }
             val char = BleCharacteristic(characteristic, service)
             listener?.onStateChange(BleState.CharacteristicChanged(device, char))
         }

     }*/

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray
    ) {
        println("onCharacteristicChanged ${characteristic.uuid}")
        mainThreadHandler.post {
            val device = getDeviceOrThrow()
            val services = checkNotNull(discoveredServices)
            val service = services.first { it.id == characteristic.service.uuid.toString() }
            val char = BleCharacteristic(characteristic, service)
            listener?.onStateChange(BleState.CharacteristicChanged(device, char))
        }
    }


/*    @Suppress("DEPRECATION")
    @Deprecated("Used natively in Android 12 and lower")
    override fun onCharacteristicRead(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
    ) {
        mainThreadHandler.post {
            val device = getDeviceOrThrow()
            val services = checkNotNull(discoveredServices)
            val service = services.first { it.id == characteristic.service.uuid.toString() }
            val char = BleCharacteristic(characteristic, service)
            listener?.onStateChange(BleState.CharacteristicRead(device, char))
        }
    }*/

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        //super.onCharacteristicRead(gatt, characteristic, value, status)
        println("onCharacteristicRead$value")
        mainThreadHandler.post {
            val device = getDeviceOrThrow()
            val services = checkNotNull(discoveredServices)
            val service = services.first { it.id == characteristic.service.uuid.toString() }
            val char = BleCharacteristic(characteristic, service)
            listener?.onStateChange(BleState.CharacteristicRead(device, char))
        }
    }


    override fun onCharacteristicWrite(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
    ) {
        //super.onCharacteristicWrite(gatt, characteristic, status)
        mainThreadHandler.post {
            val device = getDeviceOrThrow()
            val services = checkNotNull(discoveredServices)
            val service = services.first { it.id == characteristic.service.uuid.toString() }
            val char = BleCharacteristic(characteristic, service)
            listener?.onStateChange(BleState.CharacteristicWrite(device, char))
        }
    }

    private fun getGatt(device: BluetoothDevice): BluetoothGatt {
        return device.gatt ?: error("Device not connected ${device.id}")
    }

    private fun getChar(gatt: BluetoothGatt, char: BleCharacteristic): BluetoothGattCharacteristic {
        val service = gatt.getService(UUID.fromString(char.service.id))
            ?: error("Service not found ${char.service.id}")
        return service.getCharacteristic(UUID.fromString(char.id))
            ?: error("Char not found ${char.id}")
    }

    private fun getCharWithServiceAndCharacteristic(
        gatt: BluetoothGatt, foServiceUUID: String, foCharUUID: String
    ): BluetoothGattCharacteristic {
        val service = gatt.getService(UUID.fromString(foServiceUUID))
            ?: error("Service not found ${foServiceUUID}")
        return service.getCharacteristic(UUID.fromString(foCharUUID))
            ?: error("Char not found ${foCharUUID}")
    }

    private fun getDeviceOrThrow(): BluetoothDevice =
        checkNotNull(connectedDevice) { "Device is not connected!" }

    actual fun randomUUID(): String {
        return UUID.randomUUID().toString()
    }

    actual fun getBPMHash(uuidString: String?): Long {


        //System.out.println("Zero hash issue + uuidString " + uuidString);
        val uuid = UUID.fromString(uuidString)
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        val data = bb.array()
        val checksum: Checksum = CRC32()
        checksum.update(data, 0, data.size)
        return checksum.value and 0x00FFFFFFL
    }

    actual fun characteristicWrite(
        device: BluetoothDevice,
        service: BleService,
        payload: ByteArray,
        serviceUUID: String,
        charUUID: String
    ) {
        runBlocking {
            delay(100)
            val gatt = getGatt(service.device)
            val bleChar = getCharWithServiceAndCharacteristic(gatt, serviceUUID, charUUID)
            var liResult = 1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                liResult = device.gatt!!.writeCharacteristic(
                    bleChar, payload, bleChar.writeType
                )
            } else {
                var lbResult = device.gatt!!.writeCharacteristic(bleChar)
                if (lbResult) {
                    liResult = 0
                }

            }
            println("Write Result: $liResult $charUUID")
        }

    }

    actual fun characteristicsRead(
        device: BluetoothDevice, service: BleService, serviceUUID: String, charUUID: String
    ) {
        runBlocking {
            delay(100)
            println("characteristicsRead: $serviceUUID $charUUID")
            val gatt = getGatt(service.device)
            val bleChar = getCharWithServiceAndCharacteristic(gatt, serviceUUID, charUUID)
            var liResult = device.gatt!!.readCharacteristic(bleChar)
            println("Read Result: $liResult $charUUID")
        }
    }


}

fun log(msg: String) = Log.d("BLE_STATE", msg)