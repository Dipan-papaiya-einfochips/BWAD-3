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
        resetTableViewData()
        isMeasurementData = true
    }
    
    func onGetReadings(readingData: [BpMeasurement]) {
        readStoredReadingsArr.append(readingData[0])
      if readStoredReadingsArr.count > 1{
            readStoredReadingsArr = readStoredReadingsArr.reversed()
        }
        DispatchQueue.main.async {
            self.tblView.reloadData()
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
    
    func resetTableViewData(){
        readStoredReadingsArr = []
        DispatchQueue.main.async {
            self.tblView.reloadData()
        }
    }
    @IBOutlet weak var vwScan: UIView!
    @IBOutlet weak var btnScan: UIButton!{
        didSet{
            btnScan.titleLabel?.textColor = UIColor.white
            btnScan.backgroundColor = UIColor.systemBrown
            btnScan.layer.cornerRadius = btnScan.frame.height / 2
            btnScan.titleLabel?.font = UIFont.boldSystemFont(ofSize: 20)
        }
    }
    @IBOutlet weak var vwUserId: UIView!{
        didSet{
            vwUserId.isHidden = true
        }
    }
    @IBOutlet weak var btnUserIDChange: UIButton!{
        didSet{
            btnUserIDChange.titleLabel?.textColor = UIColor.white
            btnUserIDChange.backgroundColor = UIColor.systemBrown
            btnUserIDChange.layer.cornerRadius = btnScan.frame.height / 2
            btnUserIDChange.titleLabel?.font = UIFont.boldSystemFont(ofSize: 20)
        }
    }
    @IBOutlet weak var vwWriteName: UIView!{
        didSet{
            vwWriteName.isHidden = true
        }
    }
    @IBOutlet weak var btnWriteUserName: UIButton!{
        didSet{
            btnWriteUserName.titleLabel?.textColor = UIColor.white
            btnWriteUserName.backgroundColor = UIColor.systemBrown
            btnWriteUserName.layer.cornerRadius = btnScan.frame.height / 2
            btnWriteUserName.titleLabel?.font = UIFont.boldSystemFont(ofSize: 20)
        }
    }
    @IBOutlet weak var lblText: UILabel!{
        didSet{
            lblText.text = "Please press scan to connect device"
            lblText.font = UIFont.boldSystemFont(ofSize: 20)
            lblText.textColor = .white
        }
    }
    var bleAdapter : MainBluetoothAdapter?
    var bleManager : BluetoothManager?
    
    var service: BleService?
    var device : BluetoothDevice?
    var bleState: KAZDeviceStatus?
    
    @IBOutlet weak var tblView: UITableView!
    var readStoredReadingsArr : [BpMeasurement] = []
    
    var userID = 0
    
    var isMeasurementData = false
    override func viewDidLoad() {
        super.viewDidLoad()
        debugPrint("FirstScreenVC didLoad method called")
        
        //        bleAdapter.listener = self
        //        bleManager = BluetoothManager(mainBluetoothAdapter: bleAdapter, foListener: self)
        
//        initOld()
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    @IBAction func btnActionScan(_ sender: UIButton) {
        startScanAction()
        debugPrint("btnActionScan")
    }
    @IBAction func btnActionUserIDChange(_ sender: UIButton) {
        debugPrint("btnActionUserIDChange")
        if userID == 0 {
            userID = 1
        }else {
            userID = 0
        }
        startScanAction()
    }
    @IBAction func btnActionWriteUserName(_ sender: UIButton) {
        debugPrint("btnActionWriteUserName")
        let alert = UIAlertController(title: "Change User", message: "", preferredStyle: UIAlertController.Style.alert)
        alert.addAction(UIAlertAction(title: "Click", style: UIAlertAction.Style.default, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }
    private func startScanAction() {
        isMeasurementData = false
        resetTableViewData()
        bleAdapter = MainBluetoothAdapter()
        if let adapter = bleAdapter{
            adapter.listener = self
            bleManager = BluetoothManager(mainBluetoothAdapter: adapter)
            bleManager?.doInitConnectDisconnectListener(foListener: self)
            bleManager?.doInitReadingListener(foListener: self)
            bleManager?.scanAndConnect()
        }
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
extension FirstScreenVC : IBleConnectDisconnectListener{
    func onReadyForPair(device: BluetoothDevice) {
        debugPrint("onReadyForPair")
        let bpmHash = BLEUtils.bpmHash(UUID().uuidString)
        self.bleManager?.pairDevice(device: device, devicehash: Int64(bpmHash))
    }
    
    func onConnect(device: BluetoothDevice) {
        debugPrint("onConnect")
        self.lblText.text = "Connected Device : \(device.name)"
        writeProfileNameOnDevice(strName: "Mohini", device: device)
    }
    
    func onDisconnect() {
        self.lblText.text = "Device get disconnected"
        debugPrint("onDisconnect")
    }
    
    func writeProfileNameOnDevice(strName : String, device: BluetoothDevice){
        let  bytesName = Utils.companion.setUserName(fsName:strName, loUSerId: Int32(userID),lbWrite: true)
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
        cell.showReadings(dict: readStoredReadingsArr[indexPath.row], index: indexPath.row, isNewMeasurement: isMeasurementData)
        return cell
    }
}
