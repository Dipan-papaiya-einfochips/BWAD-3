//
//  BLEUtils.m
//  iosApp
//
//  Created by Mohini Bhavsar on 26/04/23.
//  Copyright © 2023 orgName. All rights reserved.
//

#import "BLEUtils.h"

@implementation BLEUtils

#define kStoragePeripheralKey            @"StoragePeripheralKey"
#define kStorageDateKey                    @"StorageDateKey"
#define kStorageDeviceNumUsersKey        @"StorageDeviceNumUsers"
#define kStoragePairingStateKey            @"StoragePairingStateKey"
#define kStorageUser1HashKey            @"StorageUser1HashKey"
#define kStorageUser2HashKey            @"StorageUser2HashKey"
#define kStorageDeviceModelKey            @"StorageDeviceModelKey"
#define kStorageNumStoredReadings        @"StorageNumStoredReadings"
#define kStorageMaxStoredReadings        @"StorageMaxStoredReadings"
#define kStorageMaxUserStoredReadings    @"StorageMaxUserStoredReadings"
#define kStorageUserNameArrayKey        @"StorageUserNameArrayKey"
#define kStorageLedActivated            @"StorageLedActivated"
#define kStorageDeviceUptime            @"StorageDeviceUptime"

+ (UInt32)bpmHash:(nonnull NSString *)uuidString {
    UInt32 next, crc, mask;
    
    uuid_t uuid;
    [[[NSUUID alloc] initWithUUIDString:uuidString] getUUIDBytes:uuid];
    
    NSMutableData *data = [[NSData dataWithBytes:uuid length:sizeof(uuid)] mutableCopy];
    
    // seed the result
    crc = 0xFFFFFFFF;
    
    // process all data bytes
    while(data.length)
    {
        // grab next byte
        next = (UInt32)((Byte*)[data bytes])[0];
        
        // fold up CRC with new data
        crc = crc ^ next;
        
        // Roll in the mask 8 times
        for (int j=0; j<8; j++)
        {
            // Do eight times.
            mask = -(crc & 1);
            crc = (crc >> 1) ^ (0xEDB88320 & mask);
        }
        
        // keep processing until len is exhausted
        [data replaceBytesInRange:NSMakeRange(0, 1) withBytes:NULL length:0];
    }
    
    // return the result (compliment)
    return (~crc) & 0x00ffffff;    // mask out the top 2 bits
}

+(int)getUserID:(NSData*)manufacturerData hash:(UInt32)bpmHash
{
    NSMutableDictionary *peripheralDic = [[NSMutableDictionary alloc]init];
    UInt8 *data = (UInt8*)manufacturerData.bytes;
    
    UInt8 deviceModel = data[0];
    [peripheralDic setObject:@(deviceModel) forKey:kStorageDeviceModelKey];
    
    UInt8 numUsers = data[1] & 0x0F;
    [peripheralDic setObject:@(numUsers) forKey:kStorageDeviceNumUsersKey];
    
    UInt8 pairable = data[1] & 0x30;
    [peripheralDic setObject:@(pairable) forKey:kStoragePairingStateKey];
    
    UInt8 led = (data[1] & 0x40) >> 6;
    [peripheralDic setObject:@(led) forKey:kStorageLedActivated];
    
    UInt32 user1Hash = 0, user2Hash = 0;
    
    user1Hash |= ((UInt32)data[2]) << 16;
    user1Hash |= ((UInt32)data[3]) << 8;
    user1Hash |= ((UInt32)data[4]);
    
    user2Hash |= ((UInt32)data[5]) << 16;
    user2Hash |= ((UInt32)data[6]) << 8;
    user2Hash |= ((UInt32)data[7]);
    
    [peripheralDic setObject:@(user1Hash) forKey:kStorageUser1HashKey];
    [peripheralDic setObject:@(user2Hash) forKey:kStorageUser2HashKey];
    
    
    NSNumber *pairingState = peripheralDic[kStoragePairingStateKey];
    NSLog(@"Objc Dict: %@", peripheralDic);
    if(([peripheralDic[kStorageUser1HashKey] longLongValue] == bpmHash && bpmHash != 0) || (pairingState && pairingState.intValue & PAIRING_USER1_PAIRABLE))
        return 0;
    else if(([peripheralDic[kStorageUser2HashKey] longLongValue] == bpmHash && bpmHash != 0) || (pairingState && pairingState.intValue & PAIRING_USER2_PAIRABLE))
        return 1;
    else
        return -1;
} 
@end


