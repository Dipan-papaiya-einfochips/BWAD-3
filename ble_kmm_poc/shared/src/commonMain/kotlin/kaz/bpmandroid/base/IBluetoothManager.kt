package kaz.bpmandroid.base

import kaz.bpmandroid.ble.BleState
import kaz.bpmandroid.ble.BluetoothDevice

interface IBluetoothManager : IBaseObservable<IBluetoothManager.BleConnectDisconectListener> {
    interface BleConnectDisconectListener {
        fun onConnect(device: BluetoothDevice)
        fun onDisconnect()
    }


    fun onStateChange(state: BleState)
}