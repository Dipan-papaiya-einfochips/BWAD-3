//
//  AppDelegate.swift
//  iosApp
//
//  Created by Mohini Bhavsar on 19/04/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import UIKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        debugPrint("App Delegate started")
        self.window = UIWindow(frame: UIScreen.main.bounds)
        if let windowObj = self.window {
            self.setRootViewForApplication(window: windowObj) //navigate as per login
        }
        return true
    }
    
    func setRootViewForApplication(window : UIWindow){
        guard let rootVC = Storyboard_Constant.kMain.instantiateViewController(withIdentifier: "FirstScreenVC") as? FirstScreenVC else {
            return
        }
        DispatchQueue.main.async {
            let navigationController = UINavigationController(rootViewController: rootVC)
            window.rootViewController = navigationController
            window.makeKeyAndVisible()
        }
    }
}

// MARK :- StoryBoard Identifier Constant
struct Storyboard_Constant{
    static let kMain : UIStoryboard = UIStoryboard(name: "Main", bundle:nil)
}
