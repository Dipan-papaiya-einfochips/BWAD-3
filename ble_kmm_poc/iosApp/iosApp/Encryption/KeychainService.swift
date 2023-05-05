//
//  KeychainService.swift
//  iosApp
//
//  Created by Mohini Bhavsar on 05/05/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import KeychainAccess

final class KeychainService {
    // MARK: - public variables
    
    enum Property {
        case databaseKey
        case sessionToken
        case deviceUUID
        case awsConfig
        case mqttCertificateId(deviceId: String)
        case emailAddress
        case emailChecksum
        case selectedRegion
        
        var key: String {
            switch self {
            case .databaseKey: return "databaseKey"
            case .sessionToken: return "sessionToken"
            case .deviceUUID: return "deviceUUID"
            case .awsConfig: return "awsConfig"
            case .mqttCertificateId(let deviceId): return "mqttCertificateId-\(deviceId)"
            case .emailAddress: return "emailAddress"
            case .emailChecksum: return "emailChecksum"
            case .selectedRegion: return "selectedRegion"
            }
        }
    }
    
    // MARK: - private variables
    
    private let keychain: Keychain
    
    // MARK: - life cycle
    
    init(keychain: Keychain) {
        self.keychain = keychain
    }
    
    // MARK: - public methods
    
    func set(_ value: String, for property: Property) {
        remove(property)
        try? keychain
            .accessibility(.whenUnlockedThisDeviceOnly)
            .set(value, key: property.key)
    }
    
    func get(_ property: Property) -> String? {
        try? keychain.get(property.key)
    }
    
    func remove(_ property: Property) {
        try? keychain.remove(property.key)
    }
}
