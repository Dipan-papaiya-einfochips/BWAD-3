//
//  BLEUtils.m
//  iosApp
//
//  Created by Mohini Bhavsar on 26/04/23.
//  Copyright © 2023 orgName. All rights reserved.
//

#import "BLEUtils.h"

@implementation BLEUtils

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

@end
