/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */


#import <Foundation/Foundation.h>

#import "RCTThirdPartyComponentsProvider.h"
#import <React/RCTComponentViewProtocol.h>

@implementation RCTThirdPartyComponentsProvider

+ (NSDictionary<NSString *, Class<RCTComponentViewProtocol>> *)thirdPartyFabricComponents
{
  static NSDictionary<NSString *, Class<RCTComponentViewProtocol>> *thirdPartyComponents = nil;
  static dispatch_once_t nativeComponentsToken;

  dispatch_once(&nativeComponentsToken, ^{
    thirdPartyComponents = @{
		@"RNMapsGoogleMapView": NSClassFromString(@"RNMapsGoogleMapView"), // @jtreact/react-native-maps
		@"RNMapsGooglePolygon": NSClassFromString(@"RNMapsGooglePolygonView"), // @jtreact/react-native-maps
		@"RNMapsMapView": NSClassFromString(@"RNMapsMapView"), // @jtreact/react-native-maps
		@"RNMapsMarker": NSClassFromString(@"RNMapsMarkerView"), // @jtreact/react-native-maps
    };
  });

  return thirdPartyComponents;
}

@end
