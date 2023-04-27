//
//  ReadDataTableViewCell.swift
//  bleConnectionDemo2
//
//  Created by Mohini Bhavsar on 18/04/23.
//

import UIKit
import shared
class ReadDataTableViewCell: UITableViewCell {
    
    @IBOutlet weak var vwHeader: UIView!
    @IBOutlet weak var vwDetails: UIView!
    
    @IBOutlet weak var lblHeader: UILabel!{
        didSet{
            lblHeader.font = UIFont.boldSystemFont(ofSize: 20)
        }
    }
    @IBOutlet weak var lblDetails: UILabel!{
        didSet{
            lblDetails.font = UIFont.systemFont(ofSize: 14)
        }
    }
    override func awakeFromNib() {
        super.awakeFromNib()
        self.selectionStyle = .none
    }
    
    func showReadings(dict : BpMeasurement?, index: Int){
        if let dictCurrent = dict{
            vwHeader.isHidden = true
            if index == 0{
                vwHeader.isHidden = false
                lblHeader.text = "Last Reading"
            }else{
                lblHeader.text = ""
            }
            var displayStr = ""
            displayStr = displayStr + " Systolic : " + "\(dictCurrent.systolic)"
            displayStr = displayStr + " Diastolic : " + "\(dictCurrent.diastolic)"
            displayStr = displayStr + " PulseRate : " + "\(dictCurrent.pulse)"
            lblDetails.text = displayStr
        }
    }
    func getUTCtoGMTDateString(date : Date) -> String{
        let dateFormatter = DateFormatter()
        
        // change to a readable time format and change to local time zone
        dateFormatter.dateFormat = "dd-MMM-yyyy h:mm a"
        dateFormatter.timeZone = NSTimeZone.local
        return dateFormatter.string(from: date)
    }
}
