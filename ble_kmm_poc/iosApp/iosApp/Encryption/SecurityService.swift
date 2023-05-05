//
//  SecurityService.swift
//  iosApp
//
//  Created by Mohini Bhavsar on 05/05/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import CryptoKit


class SecurityService {
    
    // MARK: - private variables
    
    private let keychainService: KeychainService
    
    // MARK: - life cycle
    
    init(keychainService: KeychainService) {
        self.keychainService = keychainService
    }
    
}

// MARK: - public properties

extension SecurityService {
    
    var databaseSecureKey: String {
        keychainService.get(.databaseKey) ?? generateDatabaseSecureKey()
    }
    
    var emailAddress: String? {
        get { keychainService.get(.emailAddress) }
        set {
            guard let value = newValue else { return keychainService.remove(.emailAddress) }
            keychainService.set(value, for: .emailAddress)
        }
    }
    
    var emailChecksum: String? {
        get { keychainService.get(.emailChecksum) }
        set {
            guard let value = newValue else { return keychainService.remove(.emailChecksum) }
            keychainService.set(value, for: .emailChecksum)
        }
    }
    
}

// MARK: - key generation

extension SecurityService {
    
    private func generateDatabaseSecureKey() -> String {
        let bytes = 32
        var randomBytes = [UInt8](repeating: 0, count: bytes)
        let status = SecRandomCopyBytes(kSecRandomDefault, bytes, &randomBytes)
        let key = randomBytes.map({String(format: "%02hhx", $0)}).joined()
        
        guard status == errSecSuccess else {
            return generateDatabaseSecureKey()
        }
        
        keychainService.set(key, for: .databaseKey)
        print("Database secure key successfully generated!")
        return key
    }
    
}

// MARK: - debug

extension SecurityService {
    
    func resetDatabaseKey() {
        keychainService.set(generateDatabaseSecureKey(), for: .databaseKey)
    }
    
}



