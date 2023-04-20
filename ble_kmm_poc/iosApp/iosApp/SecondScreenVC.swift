//
//  SecondScreenVC.swift
//  iosApp
//
//  Created by Mohini Bhavsar on 20/04/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import UIKit

class SecondScreenVC: UIViewController {

    @IBOutlet weak var lblText: UILabel!{
        didSet{
            lblText.text = ""
            lblText.textAlignment = .center
        }
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        KazBleManager.sharedInstance.bleDelegate = self
        // Do any additional setup after loading the view.
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.lblText.text = ""
        KazBleManager.sharedInstance.bleDelegate = self
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
extension SecondScreenVC: BleManagerDelegate{
    func bleDeviceGetConnected() {
        self.lblText.text = "Device Connected on Second VC"
        debugPrint("Device connectd SecondScreenVC")
    }
}
