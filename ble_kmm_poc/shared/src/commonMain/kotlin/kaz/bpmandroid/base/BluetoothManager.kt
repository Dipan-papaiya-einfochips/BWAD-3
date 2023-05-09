package kaz.bpmandroid.base

import kaz.bpmandroid.ble.BleCharacteristic
import kaz.bpmandroid.ble.BleService
import kaz.bpmandroid.ble.BleState
import kaz.bpmandroid.ble.BluetoothDevice
import kaz.bpmandroid.ble.MainBluetoothAdapter
import kaz.bpmandroid.model.BpMeasurement
import kaz.bpmandroid.model.DeviceInfo
import kaz.bpmandroid.util.Utils
import kaz.bpmandroid.util.Utils.Companion.peripheralsDiscovered
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class BluetoothManager(
    private val mainBluetoothAdapter: MainBluetoothAdapter
) : BaseObservable<IBluetoothManager>(), IBluetoothManager {
    var moServiceList: ArrayList<BleService> = ArrayList()

    var miCurrentReadingIndex = 0
    var miTotalReadingIndex = 0
    lateinit var moConnectDisconnectListener: IBleConnectDisconnectListener
    lateinit var moReadingDataListener: IBleReadDataListener
    var moUserNumber: Int = 0
    var moReadings: ArrayList<BpMeasurement>? = null

    init {
        mainBluetoothAdapter.listener = this
    }

    fun initConnectDisconnectListener(foListener: IBleConnectDisconnectListener) {
        moConnectDisconnectListener = foListener
    }

    fun initReadingListener(foListener: IBleReadDataListener) {
        moReadingDataListener = foListener
    }

    fun scanAndConnect() {
//        mainBluetoothAdapter.listener = this
        mainBluetoothAdapter.discoverDevices {
            mainBluetoothAdapter.connect(it)
        }
    }

    override fun onStateChange(state: BleState) {
        when (state) {
            is BleState.Connected -> {
                println("Connected device ${state.device.name}")
                mainBluetoothAdapter.discoverServices()
            }
            is BleState.CharacteristicChanged -> {
                println("CharacteristicChanged ${state.characteristic}")
                doNextCharacteristicOperations(state.characteristic, state.device, "notified")
            }
            is BleState.CharacteristicsDiscovered -> {
                println("Characteristics discovered ${state.chars}")
                for (char in state.chars) {
                    runBlocking {
                        delay(100)
                        mainBluetoothAdapter.setNotificationEnabled(char)
                    }
                }
            }
            is BleState.Disconnected -> {
                println("Disconnect device ${state.device.name}")
                moConnectDisconnectListener.onDisconnect()
            }
            is BleState.ServicesDiscovered -> {
                val macAddr: String = state.device.id

                var info: DeviceInfo? = peripheralsDiscovered!![macAddr]

                if (info == null) {
                    info = DeviceInfo()
                    peripheralsDiscovered!!.put(macAddr, info)
                }
                for (service in state.services) {
                    runBlocking {
                        delay(50)
                        println("Service discovered ${service.id}")
                        moServiceList.add(service)
                        mainBluetoothAdapter.discoverCharacteristics(service)
                    }
                }
                println("ServicesDiscovered onConnect")
                moConnectDisconnectListener.onConnect(state.device)
            }
            is BleState.CharacteristicWrite -> {
                println("CharacteristicWrite ${state.characteristic}")
                doNextCharacteristicOperations(state.characteristic, state.device, "write")

            }
            is BleState.CharacteristicRead -> {
                println("CharacteristicRead ${state.characteristic}")
                doNextCharacteristicOperations(state.characteristic, state.device, "read")
            }

            else -> {}
        }.let { /* exhaustive */ }
    }

    private fun doNextCharacteristicOperations(
        characteristic: BleCharacteristic, device: BluetoothDevice, characteristicState: String
    ) {
        if (characteristic.id.equals(Utils.BPM_USER_NAME_CHAR, true)) {
            println("doNextCharacteristicOperations Utils.BPM_USER_NAME_CHAR")
            var data = characteristic.value
            moUserNumber = data!![0].toInt()

            println("UserNumber$moUserNumber")
            /*         moUserNumber = data!![0].toInt()
                     println("Before UserNumber$moUserNumber")
                     moUserNumber = 1
                     println("NameData$data")
                     println("AfterUserNumber$moUserNumber")*/
            //pairDevice(device)
            if (characteristicState.equals("write")) {
                moConnectDisconnectListener.onReadyForPair(device)
            }

        } else if (characteristic.id.equals(
                Utils.BPM_PAIRING_CHAR, true
            )
        ) {
            if (characteristicState.equals("write")) {
                println("doNextCharacteristicOperations " + characteristic.id)
                readDataFromDevice(device, Utils.UUID_KAZ_BPM_SERVICE, Utils.BPM_NUM_READINGS_CHAR)
            }
        } else if (characteristic.id.equals(Utils.PRESSURE_MEASUREMENT_CHAR, true)) {
            if (characteristicState.equals("notified")) {
                println("doNextCharacteristicOperations " + characteristic.id)
                moReadingDataListener.onMeasurement()
                readDataFromDevice(device, Utils.UUID_KAZ_BPM_SERVICE, Utils.BPM_NUM_READINGS_CHAR)
            }
        } else if (characteristic.id.equals(Utils.BPM_NUM_READINGS_CHAR, true)) {
            if (characteristicState.equals("read")) {
                println("doNextCharacteristicOperations Utils.BPM_NUM_READINGS_CHAR")
                readAndGetTheDataFromDevice(characteristic, device)
            }
        } else if (characteristic.id.equals(Utils.BPM_REQUESTED_READING_CHAR, true)) {
            if (characteristicState.equals("notified")) {
                println("doNextCharacteristicOperations Utils.BPM_REQUESTED_READING_CHAR")
                moReadings = ArrayList()
                val deviceInfo = peripheralsDiscovered!![device.id]
                val meas = getBpMeasurement(
                    characteristic.value!!, deviceInfo!!
                )
                println(
                    "Reading downloaded ++ systolic " + meas!!.systolic.toString() + ", Diastolic " + meas.diastolic.toString() + ", pulse " + meas.pulse.toString() + ", Date " + meas.day.toString() + ", " + meas.month.toString()
                )
                moReadings!!.add(meas)

                moReadingDataListener.onGetReadings(moReadings!!)
                println("Index before $miCurrentReadingIndex$miTotalReadingIndex")
                if (miCurrentReadingIndex < miTotalReadingIndex) {
                    miCurrentReadingIndex++
                    println("Index after add $miCurrentReadingIndex$miTotalReadingIndex")
                    prepareToGetTheHistoryData(moUserNumber, miCurrentReadingIndex, device)
                }
            }


        }
    }

    fun writeDataToDevice(
        foDevice: BluetoothDevice, serviceUUID: String, charUUID: String, payload: ByteArray
    ) {
        println("writeDataToDevice : $charUUID $payload")
        var loService: BleService = BleService(charUUID, foDevice)

        mainBluetoothAdapter.characteristicWrite(
            foDevice, loService, payload, serviceUUID, charUUID
        )
    }

    fun readDataFromDevice(
        device: BluetoothDevice, serviceUUID: String, charUUID: String
    ) {
        println("readDataFromDevice : $charUUID")
        var loService: BleService = BleService(charUUID, device)
        mainBluetoothAdapter.characteristicsRead(device, loService, serviceUUID, charUUID)
    }

    private fun readAndGetTheDataFromDevice(
        characteristic: BleCharacteristic, device: BluetoothDevice
    ) {
        val deviceInfo = peripheralsDiscovered!![device.id]
        val data = characteristic.value
        if (deviceInfo != null) {
            deviceInfo.numUsers = data!![0].toInt()
            deviceInfo.maxUserStoredReadings = data[1].toInt() and 0xFF
            deviceInfo.user0NumReadings = data[2].toInt() and 0xFF
            deviceInfo.user1NumReadings = data[3].toInt() and 0xFF
            downloadAllStoredReadingsForUser(deviceInfo, moUserNumber, device)
        }
    }

    private fun downloadAllStoredReadingsForUser(
        deviceInfo: DeviceInfo, userNumber: Int, device: BluetoothDevice
    ) {
        println("Download stored readings called for userNumber = $userNumber, Device info = $deviceInfo")
        if (deviceInfo != null) {
            var numReadings = 0
            if (userNumber == 0) {
                numReadings = deviceInfo.user0NumReadings
            } else if (userNumber == 1) {
                numReadings = deviceInfo.user1NumReadings
            }
            println("Readings added Number of stored readings for user == ${numReadings}")
            miTotalReadingIndex = numReadings
            if (numReadings != 0) {
                miCurrentReadingIndex = 0
                prepareToGetTheHistoryData(userNumber, miCurrentReadingIndex, device)
            }

        }
    }

    private fun prepareToGetTheHistoryData(userNumber: Int, index: Int, device: BluetoothDevice) {
        println("prepareToGetTheHistoryData $userNumber$index")
        val data = ByteArray(2)
        data[0] = (userNumber and 0xFF).toByte()
        data[1] = (index and 0xFF).toByte()

        writeDataToDevice(device, Utils.UUID_KAZ_BPM_SERVICE, Utils.BPM_READING_REQUEST_CHAR, data)
    }

    fun pairDevice(device: BluetoothDevice, devicehash: Long) {
        //val deviceHash: Long = mainBluetoothAdapter.getBPMHash(mainBluetoothAdapter.randomUUID())
        writeDataToDevice(
            device,
            Utils.UUID_KAZ_BPM_SERVICE,
            Utils.BPM_PAIRING_CHAR,
            Utils.getParingHash(moUserNumber, devicehash)
        )

    }

    fun getBpMeasurement(data: ByteArray, info: DeviceInfo): BpMeasurement? {
        val bpMeas: BpMeasurement = BpMeasurement()
        var index = 1
        val unitsKPa = data[0].toInt() and 0x01 != 0
        val timeStampPresent = data[0].toInt() and 0x02 != 0
        val pulsePresent = data[0].toInt() and 0x04 != 0
        val userIdPresent = data[0].toInt() and 0x08 != 0
        val measurementStatusPresent = data[0].toInt() and 0x10 != 0
        if (unitsKPa) {
            bpMeas.units = "kPa"
        } else {
            bpMeas.units = "mmHg"
        }

        bpMeas.systolic = data[index++].toInt() and 0xFF
        bpMeas.systolic = bpMeas.systolic or (data[index++].toInt() and 0xFF shl 8)
        bpMeas.diastolic = data[index++].toInt() and 0xFF
        bpMeas.diastolic = bpMeas.diastolic or (data[index++].toInt() and 0xFF shl 8)
        bpMeas.map = data[index++].toInt() and 0xFF
        bpMeas.map = bpMeas.map or (data[index++].toInt() and 0xFF shl 8)
        if (timeStampPresent) {
            var year = data[index++].toInt() and 0xFF
            year = year or (data[index++].toInt() and 0xFF shl 8)
            val month = data[index].toInt() and 0x7f

            // does the measruement need adjusting because it was taken when the clock was not set?
            bpMeas.readingHasDefaultClock = data[index++].toInt() and 0x80 != 0
            val day = (data[index++].toInt() and 0xFF).toByte()
            val hours = (data[index++].toInt() and 0xFF).toByte()
            val minutes = (data[index++].toInt() and 0xFF).toByte()
            val seconds = (data[index++].toInt() and 0xFF).toByte()

            bpMeas.year = year
            bpMeas.month = month
            bpMeas.day = day.toInt()
            bpMeas.hours = hours.toInt()
            bpMeas.minutes = minutes.toInt()
            bpMeas.seconds = seconds.toInt()

        }
        if (pulsePresent) {
            bpMeas.pulse = data[index++].toInt() and 0xFF
            bpMeas.pulse = bpMeas.pulse or (data[index++].toInt() and 0xFF shl 8)
        }
        if (userIdPresent) {
            bpMeas.userId = data[index++].toInt() and 0xFF
        }
        if (measurementStatusPresent) {
            var status = data[index++].toInt() and 0xFF
            status = status or (data[index++].toInt() and 0xFF shl 8)
            bpMeas.irregularPulse = status and 0x01 != 0
        }
        return bpMeas
    }
}