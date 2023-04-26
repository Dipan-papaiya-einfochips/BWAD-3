package kaz.bpmandroid.base

import kaz.bpmandroid.ble.BluetoothDevice

interface IBleConnectDisconnectListener {
    fun onConnect(device: BluetoothDevice)
    fun onDisconnect()

    fun onReadyForPair(device: BluetoothDevice)
}