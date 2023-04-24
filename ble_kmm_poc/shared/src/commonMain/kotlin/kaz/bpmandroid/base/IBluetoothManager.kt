package kaz.bpmandroid.base

import kaz.bpmandroid.ble.BleState
import kaz.bpmandroid.ble.BluetoothDevice

interface IBluetoothManager : IBaseObservable<IBluetoothManager> {
    fun onStateChange(state: BleState)
}