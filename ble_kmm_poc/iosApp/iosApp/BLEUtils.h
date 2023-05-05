//
//  BLEUtils.h
//  iosApp
//
//  Created by Mohini Bhavsar on 26/04/23.
//  Copyright © 2023 orgName. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>
NS_ASSUME_NONNULL_BEGIN

@interface BLEUtils : NSObject
+(UInt32)bpmHash:(NSString*)uuidString;
+(int)getUserID:(NSData*)manufacturerData hash:(UInt32)bpmHash; 
@end

NS_ASSUME_NONNULL_END

#define PAIRING_USER1_PAIRABLE      ((UInt8)0x10)
#define PAIRING_USER2_PAIRABLE      ((UInt8)0x20)

