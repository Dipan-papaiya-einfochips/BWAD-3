//
//  FirstScreenVC.swift
//  iosApp
//
//  Created by Mohini Bhavsar on 19/04/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import UIKit
import shared
//import CoreBluetooth

class FirstScreenVC: UIViewController {
    var device : BluetoothDevice?
    override func viewDidLoad() {
        super.viewDidLoad()
        let bleAdapter = BluetoothAdapter()
        
        bleAdapter.listener = self
        bleAdapter.discoverDevices { bleDevice in
            debugPrint("discoved device", bleDevice)
            self.device = bleDevice
            bleAdapter.connect(device: bleDevice)
        }
        debugPrint("FirstScreenVC didLoad method called")
    }
    
}

extension FirstScreenVC: BluetoothAdapterListener{
    func onStateChange(state: BleState) {
        debugPrint("onStateChange called")
        if let connectedDevice = self.device{
            switch state{
            case BleState.Connected(device: connectedDevice):
                debugPrint("Connected")
            default:
                break
            }
        }
    }
}
