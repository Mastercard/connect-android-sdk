# Connect Android SDK

## Overview

The Connect mobile SDKs allow you to embed the Connect user experience anywhere you want within your mobile application.


## Compatibility

The Connect Android SDK supports the following Android versions.

* Android 5.0 (Lollipop) or later & minSdkVersion 21 or later


## Step 1 - Add repository to your project

## Maven-central

Please modify your root-level Gradle file(build.gradle) as per below code.

```
 allprojects {
   repositories {
       google()
       mavenCentral()
   }
 }
```

Please modify your app-level Gradle file(build.gradle) as per below code.

```
android {
  defaultConfig {
    minSdkVersion 21 // or greater
  }
}
dependencies {
  // ...
  implementation 'com.mastercard.openbanking.connect:connect-sdk:<insert latest version>'
}
```

Note: The latest version of the Connect Android SDK can be found in [Maven Central](https://central.sonatype.com/artifact/com.mastercard.openbanking.connect/connect-sdk/2.3.0/versions).

## Manual

* Clone the Connect Android SDK project from [Github](https://github.com/Mastercard/connect-android-sdk)

* On your Android project click on File > New > Import Module  > Select the path of connect sdk folder location > Finish

* Modify the build.gradle file in connect-sdk module, remove the below code which is on line no 46 and 123

```
apply from: "$project.rootDir/sonar.gradle"
apply from: "${rootProject.projectDir}/sonatype-publish.gradle"
```
* Clean and build the project

## Step 2 - Update Android application settings

The Connect Android SDK requires internet access to connect with our servers. As such, you need to add internet permissions to the AndroidManifest.xml file.

```
<uses-permission android:name="android.permission.INTERNET">
```
## App Link Support

Add activity in AndroidManifest.xml file.

```
 <activity android:name="com.mastercard.openbanking.connect.Connect"   
 android:launchMode="singleTask"    
 android:exported="true">
 <intent-filter>        
    <action android:name="android.intent.action.VIEW" />        
    <category android:name="android.intent.category.DEFAULT" />        
    <category android:name="android.intent.category.BROWSABLE" />        
     <data
        android:scheme="https"
        android:host="{{yourdomain.com}}"/>
 </intent-filter>
 </activity>
 ```

## Deep Link Support(Not recommended)

Add activity in AndroidManifest.xml file.

```
<activity android:name="com.mastercard.openbanking.connect.Connect"   
 android:launchMode="singleTask"    
 android:exported="true">
 <intent-filter>        
    <action android:name="android.intent.action.VIEW" />        
    <category android:name="android.intent.category.DEFAULT" />        
    <category android:name="android.intent.category.BROWSABLE" />        
    <data android:scheme="{deep_link_app_name}"/>    
 </intent-filter>
 </activity>
 ```
{deep_link_app_name} is case sensitive and should only use lower-case character


## Step 3 - Add code to start the Connect SDK

The Connect class contains a start method that when called, starts an activity with the supplied event handler. The SDK only allows a single instance of the Connect activity to run. If you start Connect while a Connect activity is already running, a RuntimeException is thrown.

## Connect Class

The Connect Android SDK’s main component is the Connect class that contains a static start method, which runs an activity that connects with the EventHandler. To access the APIs in the SDK include the following imports:

```  
  import com.mastercard.openbanking.connect.Connect;
  import com.mastercard.openbanking.connect.EventHandler;
```

## App Link Support

```Connect.start(this, url, "https://yourdomain.com/connect", eventHandler);```

## Deep Link Support

```Connect.start(this, url, "{deep_link_app_name}://", eventHandler);```


```
Java
public static void start(Context context, String connectUrl, String redirectUrl, EventHandler eventHandler)
```

```
Kotlin
fun start(context: Context, connectUrl: String?, redirectUrl: String?, eventHandler: EventHandler?)
```

| Argument | Description |
| ------ | ------ |
| context | The Android Context is referenced by Connect when an activity starts. |
| connectUrl | The SDK loads the Connect URL. |
| redirectUrl | App link URL/ Deep link URL to redirect back to your mobile app after completing FI’s OAuth flow. This parameter is only required for App to App. |
| eventHandler | A class implementing the EventHandler interface. |

See [Generate 2.0 Connect URL APIs](https://developer.mastercard.com/open-banking-us/documentation/connect/generate-2-connect-url-apis/)



## EventHandler Interface

Throughout Connect’s flow, events about the state of the web application are sent as JSONObjects to the EventHandler methods.

> **_NOTE:_**  The onUserEvent handler will not return anything unless you’re specifically targeting Connect.

```
Java
public interface EventHandler {
    void onLoad();
    void onCancel(JSONObject cancelEvent);
    void onDone(JSONObject doneEvent);
    void onError(JSONObject errorEvent);
    void onRoute(JSONObject routeEvent);
    void onUser(JSONObject userEvent);
}
```

```
Kotlin
interface EventHandler {
    fun onLoad()
    fun onCancel(cancelEvent: JSONObject?)
    fun onDone(doneEvent: JSONObject?)
    fun onError(errorEvent: JSONObject?)
    fun onRoute(routeEvent: JSONObject?)
    fun onUser(userEvent: JSONObject?)
}
```

Event | Description |
| ------ | ------ |
| onLoad | Sent when the Connect web page is loaded and ready to display |
| onCancel | Sent when the user cancels the Connect application |
| onDone | Sent when the user successfully completes the Connect appliction |
| onError | Sent when there is an error during the Connect application |
| onRoute | Sent when the user navigates to a new route or screen in Connect |
| onUser | Called when a user performs an action. User events provide visibility into what action a user could take within the Connect application |


## Manually stop a connect activity

The Connect activity will automatically finish on done, cancel, and error events.

You can manually finish a Connect activity by invoking:

```
Connect.finishCurrentActivity()
```

If there isn’t a current Connect activity running, then the method will throw a RuntimeException.

