//
//  FirstScreenVC.swift
//  iosApp
//
//  Created by Mohini Bhavsar on 19/04/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import UIKit
import shared
import MBProgressHUD

class FirstScreenVC: UIViewController, IBluetoothManager, IBleReadDataListener {
    func onMeasurement(){
        debugPrint("get new measurement")
        resetTableViewData()
        isMeasurementData = true
    }
    
    func onGetReadings(readingData: [BpMeasurement]) {
        self.dismissHUD(isAnimated: false)
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
    
    
    var bpmHash : UInt32?
    @IBOutlet weak var tblView: UITableView!
    var readStoredReadingsArr : [BpMeasurement] = []
    
    var userID = 0
    
    var isMeasurementData = false
    var userDBTableRepo: UserDBTableRepo?
    
    var isWriteUserName = false
    override func viewDidLoad() {
        super.viewDidLoad()
        let path = FileManager.documentsDir()
        debugPrint("path: \(path)")
        let deviceID = UIDevice.current.identifierForVendor!.uuidString
        print(deviceID)
        let key = (UIApplication.shared.delegate as! AppDelegate).dbKey ?? ""
        userDBTableRepo = UserDBTableRepo(databaseDriverFactory: DatabaseDriverFactory(key: key))
        self.loadLaunches()
        
        
        debugPrint("FirstScreenVC didLoad method called")
        
        //        bleAdapter.listener = self
        //        bleManager = BluetoothManager(mainBluetoothAdapter: bleAdapter, foListener: self)
        
//        initOld()
    
    }
    func loadLaunches() {
        if let repo = userDBTableRepo{
            repo.getAllUsers { user, error in
                if let launches = user {
                    debugPrint("launches")
                } else {
                    debugPrint("launches error")
                }
            }
        }
        
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    @IBAction func btnActionScan(_ sender: UIButton) {
        startScanAction()
        self.showHUD(progressLabel: "")
        debugPrint("btnActionScan")
    }
    @IBAction func btnActionUserIDChange(_ sender: UIButton) {
        debugPrint("btnActionUserIDChange")
//        if userID == 0 {
//            userID = 1
//        }else {
//            userID = 0
//        }
        startScanAction()
    }
    @IBAction func btnActionWriteUserName(_ sender: UIButton) {
        debugPrint("btnActionWriteUserName")
        let alert = UIAlertController(title: "Change User", message: "", preferredStyle: UIAlertController.Style.alert)
        alert.addAction(UIAlertAction(title: "Click", style: UIAlertAction.Style.default, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }
    private func startScanAction() {
        bpmHash = BLEUtils.bpmHash(UUID().uuidString)
        isWriteUserName = false
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
        
        if let hashDevice = bpmHash{
            self.bleManager?.pairDevice(device: device, devicehash: Int64(hashDevice))
        }
    }
    
    func onConnect(device: BluetoothDevice) {
        debugPrint("onConnect")
        if let connectedPeripheral = bleAdapter?.connectedPeripheral, let advetisementData = bleAdapter?.moAdvertisementData, let hashDevice = bpmHash{
            var dict : [String:Any] = [:]
            if let data = advetisementData[CBAdvertisementDataManufacturerDataKey] as? Data{
                debugPrint("data : ",data)
                
                let dataConver = [UInt8](data)
                
                debugPrint("dataConver : ",dataConver)
                
                let deviceModel = UInt8(dataConver[0])
                dict["StorageDeviceModelKey"] = deviceModel
                
                if deviceModel == 1{
                    isWriteUserName = true
                }else{
                    isWriteUserName = false
                }
                
                let numUsers = UInt8(dataConver[1] & 0x0F)
                dict["StorageDeviceNumUsers"] = numUsers
                
                let pairable = UInt8(dataConver[1] & 0x30)
                dict["StoragePairingStateKey"] = pairable
                
                let led = UInt8((dataConver[1] & 0*40) >> 6)
                dict["StorageLedActivated"] = led
                
                var user1Hash : UInt32 = 0
                var user2Hash : UInt32 = 0
                
                user1Hash |= UInt32(dataConver[2]) << 16//((UInt32)dataConver[2]) << 16;
                user1Hash |= UInt32(dataConver[3]) << 8//((UInt32)dataConver[3]) << 8;
                user1Hash |= UInt32(dataConver[4])//((UInt32)dataConver[4]);
                
                user2Hash |= UInt32(dataConver[5]) << 16//((UInt32)dataConver[5]) << 16;
                user2Hash |= UInt32(dataConver[6]) << 8//((UInt32)dataConver[6]) << 8;
                user2Hash |= UInt32(dataConver[7])//((UInt32)dataConver[7]);
                
                dict["StorageUser1HashKey"] = user1Hash
                dict["StorageUser2HashKey"] = user2Hash
                let bpPAIRING_USER1_PAIRABLE = UInt8(0x10)
                let bpPAIRING_USER2_PAIRABLE = UInt8(0x20)
                debugPrint("dict pairing:",dict)
                let userid = BLEUtils.getUserID(data, hash: hashDevice)
                debugPrint("user id objc: \(userid)")
                if user1Hash == hashDevice || ((pairable != 0) && (Int(pairable) != 0) && (bpPAIRING_USER1_PAIRABLE == pairable)){//user1Hash == hashDevice || ((pairable != 0) && (Int(pairable) != 0) && (PAIRING_USER1_PAIRABLE != 0)){
                    userID = 0
                }else if user2Hash == hashDevice || ((pairable != 0) && (Int(pairable) != 0) && (bpPAIRING_USER2_PAIRABLE == pairable)){//user2Hash == hashDevice || ((pairable != 0) && (Int(pairable) != 0) && (PAIRING_USER2_PAIRABLE != 0)){
                    userID = 1
                }
//                isWriteUserName = false
//                userID = 1
                debugPrint("*********userId : \(userID)*********")
                debugPrint("*********write : \(isWriteUserName)*********")
            }
        }
        self.lblText.text = "Connected Device : \(device.name)"
        writeProfileNameOnDevice(strName: "Mohini", device: device)
    }
    
    func onDisconnect() {
        self.lblText.text = "Device disconnected"
        debugPrint("onDisconnect")
        resetTableViewData()
    }
    
    func writeProfileNameOnDevice(strName : String, device: BluetoothDevice){
        let  bytesName = Utils.companion.setUserName(fsName:strName, loUSerId: Int32(userID),lbWrite: isWriteUserName)
        debugPrint("Name: \(strName) userId : \(userID)")
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

extension FileManager {
    class func documentsDir() -> String {
        var paths = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true) as [String]
        return paths[0]
    }
    
    class func cachesDir() -> String {
        var paths = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true) as [String]
        return paths[0]
    }
}

extension UIViewController {
    
    func showHUD(progressLabel:String){
        DispatchQueue.main.async{
            let progressHUD = MBProgressHUD.showAdded(to: self.view, animated: true)
            progressHUD.label.text = progressLabel
        }
    }
    
    func dismissHUD(isAnimated:Bool) {
        DispatchQueue.main.async{
            MBProgressHUD.hide(for: self.view, animated: isAnimated)
        }
    }
}

