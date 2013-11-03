# ![Logo](https://raw.github.com/spartako/android-location-utils/master/LocationUtils/src/main/res/drawable-mdpi/ic_launcher.png) LocationUtils
This project aims to provide a simple way to access Location based services.
## What it is
Google Play Services provides Location Services, which is a handy way of accesing location information on Android devices.
However there is a bunch of checks, methods, callbacks and whatnots that are particulaty annoying to implement everytime.
This is just a simple Activity wrapping around all that and exposing the data through a couple abstract methods.

## Usage

#### 1. Android Manifest

Add the following permissions in order to be able to access Location Services
``` xml
<manifest>
	...
	<!-- PERMISSIONS -->
	<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
	<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
	<!-- /PERMISSIONS -->	
	...
	<application android:name="MyApplication">
		...
	</application>
</manifest>
```
#### 2. Gradle Dependencies
Your build.gradle file will need the following dependency
``` java
compile 'com.google.android.gms:play-services:3.2.+'
```
If you don't use yet the new build system based on gradle, notice that you need the Play Services library.

#### 3. LocationActivity
Copy [LocationActivity](https://github.com/spartako/android-location-utils/blob/master/LocationUtils/src/main/java/com/spartako/locationutils/LocationActivity.java) to your project and let your activity extend it. Then implement
``` java
void onNewLocationFix()
LocationRequest getLocationRequest()
```
`onNewLocationFix()` will be called everytime there is a new location fix available.

`getLocationRequest()` provides the criteria which the location fixes will follow, as documented [here](http://developer.android.com/reference/com/google/android/gms/location/LocationRequest.html)**.

#### 4. Public methods.
``` java
void startLocationUpdates()
void stopLocationUpdates()
Location getLastFix()
```
This three are self-explanatory. Start/Stop receiving updates and access the latest fix available.

## Example usage
There is an sample implementation you can check-out and try.

## Limitations
No Geofencing (shouldn't be hard to add though).
No PendingIntents. Just the activity extending LocationActivity will get notified of new fixes.

## Disclaimer
Needless to say LocationActivity has not been tested thoroughly. It's just a mini-help.
If you find any problems don't hesitate opening an issue.
