//
//  BLEUtils.h
//  iosApp
//
//  Created by Mohini Bhavsar on 26/04/23.
//  Copyright © 2023 orgName. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BLEUtils : NSObject
+(UInt32)bpmHash:(NSString*)uuidString;
@end

NS_ASSUME_NONNULL_END
