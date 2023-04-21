package kaz.bpmandroid.base

import kaz.bpmandroid.ble.BleService
import kaz.bpmandroid.ble.BleState
import kaz.bpmandroid.ble.MainBluetoothAdapter
import kaz.bpmandroid.ble.BluetoothDevice
import kaz.bpmandroid.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothManager(private val mainBluetoothAdapter: MainBluetoothAdapter) :
    BaseObservable<IBluetoothManager.BleConnectDisconectListener>(), IBluetoothManager {
    var moServiceList: ArrayList<BleService> = ArrayList()

    init {
        mainBluetoothAdapter.listener = this
    }

    fun scanAndConnect() {
        mainBluetoothAdapter.discoverDevices {
            mainBluetoothAdapter.connect(it)
        }
    }

    fun writeDataToDevice(
        foDevice: BluetoothDevice, serviceUUID: String, charUUID: String, payload: ByteArray
    ) {
        println("writeDataToDevice : $charUUID $payload")
        var loService: BleService = BleService(charUUID, foDevice)

        mainBluetoothAdapter.onCharacteristicWrite(
            foDevice, loService, payload, serviceUUID, charUUID
        )
    }

    override fun onStateChange(state: BleState) {
        when (state) {
            is BleState.Connected -> {
                /*view?.showMessage("Connected device ${state.device.name}")*/
                println("Connected device ${state.device.name}")
                mainBluetoothAdapter.discoverServices()
            }
            is BleState.CharacteristicChanged -> {
                println("CharacteristicChanged ${state.characteristic}")
            }
            is BleState.CharacteristicsDiscovered -> {
                for (char in state.chars) {
                    println("Characteristics discovered ${state.chars}")
                    mainBluetoothAdapter.setNotificationEnabled(char)
                }

            }
            is BleState.Disconnected -> {
                println("Disconnect device ${state.device.name}")
                invokeListeners { l ->
                    l.onDisconnect()
                }
            }
            is BleState.ServicesDiscovered -> {
                for (service in state.services) {
                    println("Service discovered ${service.id}")
                    moServiceList.add(service)
                    mainBluetoothAdapter.discoverCharacteristics(service)
                }
                invokeListeners { l ->
                    l.onConnect(state.device)

                }


            }
            is BleState.CharacteristicWrite -> {
                println("CharacteristicWrite ${state.characteristic}")
                if (state.characteristic.id.equals(Utils.BPM_USER_NAME_CHAR, true)) {
                    pairDevice(state.device)
                } else {

                }
            }
            is BleState.CharacteristicRead -> {
                println("CharacteristicRead ${state.characteristic}")
            }
        }.let { /* exhaustive */ }
    }

    fun pairDevice(device: BluetoothDevice) {
        val deviceHash: Long =
            mainBluetoothAdapter.getBPMHash(mainBluetoothAdapter.randomUUID())
        writeDataToDevice(
            device,
            Utils.UUID_KAZ_BPM_SERVICE,
            Utils.BPM_PAIRING_CHAR,
            Utils.getParingHash(0, deviceHash)
        )

    }


}