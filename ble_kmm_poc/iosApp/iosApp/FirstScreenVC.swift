//
//  FirstScreenVC.swift
//  iosApp
//
//  Created by Mohini Bhavsar on 19/04/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import UIKit
import shared

class FirstScreenVC: UIViewController, IBluetoothManager, IBleReadDataListener {
    func onGetReadings(readingData: [BpMeasurement]) {
        debugPrint("onGetReadings ")
    }
    
    func invokeListeners(invoker: @escaping (Any?) -> Void) {
        debugPrint("invokeListeners")
    }
    
    func registerListener(listener: Any?) {
        debugPrint("registerListener")
    }
    
    func unregisterListener(listener: Any?) {
        debugPrint("unregisterListener")
    }
    
    func onStateChange(state: BleState) {
        if let connectedDevice = self.device{
            switch state{
            case BleState.Connected(device: connectedDevice):
                bleState = .connected
                debugPrint("iOS connected")
                //                bleDelegate?.bleDeviceGetConnected()
            case BleState.Disconnected(device: connectedDevice):
                bleState = .disconnected
                //                self.bleDeviceGetDisconnected()
            case BleState.ServicesDiscovered(device: connectedDevice, services: []):
                break
                
            default:
                break
            }
        }
    }
    
    @IBOutlet weak var btnNav: UIButton!
    @IBOutlet weak var lblText: UILabel!{
        didSet{
            lblText.text = ""
            lblText.textAlignment = .center
        }
    }
    let bleAdapter = MainBluetoothAdapter()
    var bleManager : BluetoothManager?
    
    var service: BleService?
    var device : BluetoothDevice?
    var bleState: KAZDeviceStatus?
    override func viewDidLoad() {
        super.viewDidLoad()
        debugPrint("FirstScreenVC didLoad method called")
        
        //        bleAdapter.listener = self
        //        bleManager = BluetoothManager(mainBluetoothAdapter: bleAdapter, foListener: self)
        
        initOld()
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.lblText.text = ""
    }
    @IBAction func btnActionGoNext(_ sender: UIButton) {
    }
    private func initOld() {
        bleAdapter.listener = self
        bleManager = BluetoothManager(mainBluetoothAdapter: bleAdapter)
        bleManager?.doInitConnectDisconnectListener(foListener: self)
        bleManager?.doInitReadingListener(foListener: self)
        bleManager?.scanAndConnect()
        //bleDiscoverDevices(bleAdapter: bleAdapter)
    }
    
    func bleDiscoverDevices(bleAdapter : MainBluetoothAdapter?){
        if let adapter = bleAdapter{
            adapter.discoverDevices { bleDevice in
                debugPrint("discover device", bleDevice)
                //                self.device = bleDevice
                adapter.connect(device: bleDevice)
            }
        }
    }
}
//extension FirstScreenVC: BleManagerDelegate{
//    func bleDeviceGetConnected() {
//        self.lblText.text = "Device Connected on First VC"
//        debugPrint("Device Connected firstScreenVC")
//    }
//}

extension FirstScreenVC : IBleConnectDisconnectListener{
    func onReadyForPair(device: BluetoothDevice) {
        debugPrint("onReadyForPair")
        let bpmHash = BLEUtils.bpmHash(UUID().uuidString)
        self.bleManager?.pairDevice(device: device, devicehash: Int64(bpmHash))
    }
    
    func onConnect(device: BluetoothDevice) {
        debugPrint("onConnect")
        writeProfileNameOnDevice(strName: "Mohini1", device: device)
    }
    
    func onDisconnect() {
        debugPrint("onDisconnect")
    }
    
    func writeProfileNameOnDevice(strName : String, device: BluetoothDevice){
        let  bytesName = Utils.companion.setUserName(fsName:strName)
        DispatchQueue.main.async {
            self.bleManager?.writeDataToDevice(foDevice: device, serviceUUID: Utils.companion.UUID_KAZ_BPM_SERVICE, charUUID: Utils.companion.BPM_USER_NAME_CHAR, payload: bytesName)
        }
    }
    
}
