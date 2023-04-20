//
//  KazBleManager.swift
//  iosApp
//
//  Created by Mohini Bhavsar on 20/04/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared
import UIKit

class KazBleManager: BleManagerDelegate{
    var device : BluetoothDevice?
    var bleState: KAZDeviceStatus?
    var bleAdapter : BluetoothAdapter?
    weak var bleDelegate : BleManagerDelegate?
    static var sharedInstance = KazBleManager()
    private init() {
        bleAdapter = BluetoothAdapter()
        bleAdapter?.listener = self
        bleAdapter?.discoverDevices { bleDevice in
            debugPrint("discoved device", bleDevice)
            self.device = bleDevice
            self.bleAdapter?.connect(device: bleDevice)
        }
    }
    func bleDeviceGetConnected() {
        debugPrint("Do task after device get connected")
    }
    func bleDiscoverDevices(bleAdapter : BluetoothAdapter){
        bleAdapter.discoverDevices { bleDevice in
            debugPrint("discover device", bleDevice)
            self.device = bleDevice
            bleAdapter.connect(device: bleDevice)
        }
    }
    func handleReadSensorResponse(byteArray: Any) { //handle read response
        guard let kotlinByteArray = byteArray as? KotlinByteArray else {
            return
        }
    }
    func readHistoryArrayRequest() {
        guard let currentDeviceStatus = bleState, let connectedDevice = self.device else{
            return
        }
        //readHistoryData -> KMM
    }
    func writeUserNameOnDevice(){
        guard let currentDeviceStatus = bleState, let connectedDevice = self.device else{
            return
        }
    }
}

extension KazBleManager : BluetoothAdapterListener{
    func onStateChange(state: BleState) {
        debugPrint("onStateChange called")
        if let connectedDevice = self.device{
            switch state{
            case BleState.Connected(device: connectedDevice):
                debugPrint("Connected device")
                bleState = .connected
                bleDelegate?.bleDeviceGetConnected()
            case BleState.Disconnected(device: connectedDevice):
                debugPrint("Disconnected device")
                bleState = .disconnected
            default:
                break
            }
        }
    }
}

protocol BleManagerDelegate:AnyObject {
    func bleDeviceGetConnected()
}

extension BleManagerDelegate{
    func bleDeviceGetConnected(){
        debugPrint("Device get connected")
    }
}
