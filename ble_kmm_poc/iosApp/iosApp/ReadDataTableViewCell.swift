//
//  ReadDataTableViewCell.swift
//  bleConnectionDemo2
//
//  Created by Mohini Bhavsar on 18/04/23.
//

import UIKit
import shared
class ReadDataTableViewCell: UITableViewCell {
    @IBOutlet weak var vwMain: UIView!{
        didSet{
            vwMain.dropShadow()
        }
    }
    @IBOutlet weak var vwSystolic: UIView!
    
    @IBOutlet weak var lblSystolic: UILabel!{
        didSet{
            lblSystolic.font = UIFont.systemFont(ofSize: 14)
        }
    }
    
    @IBOutlet weak var vwDiastolic: UIView!
    
    @IBOutlet weak var lblDiastolic: UILabel!{
        didSet{
            lblDiastolic.font = UIFont.systemFont(ofSize: 14)
        }
    }
    
    @IBOutlet weak var vwPulseRate: UIView!
    
    @IBOutlet weak var lblPulseRate: UILabel!{
        didSet{
            lblPulseRate.font = UIFont.systemFont(ofSize: 14)
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        self.selectionStyle = .none
    }
    
    func showReadings(dict : BpMeasurement?, index: Int, isNewMeasurement : Bool){
        if let dictCurrent = dict{
            let font = UIFont.systemFont(ofSize: 14)
            let boldFont = UIFont.boldSystemFont(ofSize: 14)
            self.lblSystolic.attributedText = "Systolic : \(dictCurrent.systolic)".withBoldText(
                boldPartsOfString: ["Systolic : "], font: font, boldFont: boldFont)
            self.lblDiastolic.attributedText = "Diastolic : \(dictCurrent.diastolic)" .withBoldText(
                boldPartsOfString: ["Diastolic : "], font: font, boldFont: boldFont)
            self.lblPulseRate.attributedText = "Pulse : \(dictCurrent.pulse)".withBoldText(
                boldPartsOfString: ["Pulse : "], font: font, boldFont: boldFont)
        }
        vwMain.backgroundColor = UIColor.white
        self.lblSystolic.textColor = .black
        self.lblDiastolic.textColor = self.lblSystolic.textColor
        self.lblPulseRate.textColor = self.lblSystolic.textColor
        if isNewMeasurement , index == 0{
            self.lblSystolic.textColor = .white
            self.lblDiastolic.textColor = self.lblSystolic.textColor
            self.lblPulseRate.textColor = self.lblSystolic.textColor
            if #available(iOS 15.0, *) {
                vwMain.backgroundColor = UIColor.systemMint
            } else {
                // Fallback on earlier versions
                vwMain.backgroundColor = UIColor.purple
            }
        }
    }
    func getUTCtoGMTDateString(date : Date) -> String{
        let dateFormatter = DateFormatter()
        
        // change to a readable time format and change to local time zone
        dateFormatter.dateFormat = "dd-MMM-yyyy h:mm a"
        dateFormatter.timeZone = NSTimeZone.local
        return dateFormatter.string(from: date)
    }
    
    func addBoldText(fullString: NSString, boldPartsOfString: Array<NSString>, font: UIFont!, boldFont: UIFont!) -> NSAttributedString {
        let nonBoldFontAttribute = [NSAttributedString.Key.font:font!]
        let boldFontAttribute = [NSAttributedString.Key.font:boldFont!]
        let boldString = NSMutableAttributedString(string: fullString as String, attributes:nonBoldFontAttribute)
        for i in 0 ..< boldPartsOfString.count {
            boldString.addAttributes(boldFontAttribute, range: fullString.range(of: boldPartsOfString[i] as String))
        }
        return boldString
    }
}
extension String {
    func withBoldText(boldPartsOfString: Array<NSString>, font: UIFont!, boldFont: UIFont!) -> NSAttributedString {
        let nonBoldFontAttribute = [NSAttributedString.Key.font:font!]
        let boldFontAttribute = [NSAttributedString.Key.font:boldFont!]
        let boldString = NSMutableAttributedString(string: self as String, attributes:nonBoldFontAttribute)
        for i in 0 ..< boldPartsOfString.count {
            boldString.addAttributes(boldFontAttribute, range: (self as NSString).range(of: boldPartsOfString[i] as String))
        }
        return boldString
    }
}


extension UIView {
    func dropShadow() {
        
        layer.shadowColor = UIColor.gray.cgColor
        layer.shadowOffset = CGSize(width: 1, height: 1.5)
        layer.masksToBounds = false
        
        layer.shadowOpacity = 0.3
        layer.shadowRadius = 3
        //layer.shadowPath = UIBezierPath(rect: bounds).cgPath
        layer.rasterizationScale = UIScreen.main.scale
        layer.shouldRasterize = true
    }
}
