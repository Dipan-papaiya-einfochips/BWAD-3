//
//  FirstScreenVC.swift
//  iosApp
//
//  Created by Mohini Bhavsar on 19/04/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import UIKit
import shared

class FirstScreenVC: UIViewController {
    @IBOutlet weak var btnNav: UIButton!
    @IBOutlet weak var lblText: UILabel!{
        didSet{
            lblText.text = ""
            lblText.textAlignment = .center
        }
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        debugPrint("FirstScreenVC didLoad method called")
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.lblText.text = ""
        KazBleManager.sharedInstance.bleDelegate = self
    }
    @IBAction func btnActionGoNext(_ sender: UIButton) {
        if let svc = self.storyboard?.instantiateViewController(withIdentifier: "SecondScreenVC") as? SecondScreenVC{
            DispatchQueue.main.async {
                self.navigationController?.pushViewController(svc, animated: true)
            }
        }
    }
}
extension FirstScreenVC: BleManagerDelegate{
    func bleDeviceGetConnected() {
        self.lblText.text = "Device Connected on First VC"
        debugPrint("Device Connected firstScreenVC")
    }
}
