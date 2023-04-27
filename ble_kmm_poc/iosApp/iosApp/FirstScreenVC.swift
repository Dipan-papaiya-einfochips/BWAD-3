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
    func onMeasurement(){
        debugPrint("get new measurement")
        readStoredReadingsArr = []
    }
    
    func onGetReadings(readingData: [BpMeasurement]) {
        readStoredReadingsArr.append(readingData[0])
        if readStoredReadingsArr.count == 1{
            DispatchQueue.main.async {
                self.tblView.reloadData()
            }
        }else{
            DispatchQueue.main.async {
                self.tblView.beginUpdates()
                self.tblView.insertRows(at: [IndexPath.init(row: self.readStoredReadingsArr.count-1, section: 0)], with: .automatic)
                self.tblView.endUpdates()
            }
        }
        debugPrint("onGetReadings \(readingData.count)", readingData[0].diastolic)
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
    
    @IBOutlet weak var tblView: UITableView!
    var readStoredReadingsArr : [BpMeasurement] = []
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
        writeProfileNameOnDevice(strName: "Mohini", device: device)
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
extension FirstScreenVC: UITableViewDataSource{
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return readStoredReadingsArr.count
    }
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: "ReadDataTableViewCell") as? ReadDataTableViewCell else {
            return UITableViewCell()
        }
        cell.showReadings(dict: readStoredReadingsArr[indexPath.row], index: indexPath.row)
        return cell
    }
}
