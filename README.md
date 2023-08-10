# Connect Android SDK

## Overview

The Connect mobile SDKs allow you to embed the Connect user experience anywhere you want within your mobile application.


## Compatibility

The Connect Android SDK supports the following Android versions.

* Android 5.0 (Lollipop) or later

* minSdkVersion 21 or later

* Android Gradle Plugin v3.4.0 or greater required

* Gradle 5.1.1 or greater required


## Step 1 - Add repository to your project

## Maven-central

Add the following code to the dependency section in the build.gradle file.

```
 implementation 'com.mastercard.openbanking.connect:connect-sdk:<insert latest version>' 
```

## Manual

* Clone the project: connect-sdk

* On your Android project click on File > New > Import Module  > Select the path of connect sdk folder location > Finish

* Modify the build.gradle file in connect-sdk module, remove the below code which is on line no 46 and 123

```
apply from: "$project.rootDir/sonar.gradle"
apply from: "${rootProject.projectDir}/sonatype-publish.gradle"
```
* Clean and build the project

## Step 2 - For projects using AndroidX:

Open the gradle.properties file and set **android.enableJetifier** to **true.**


## Step 3 - Update Android application settings

Add internet permissions to your AndroidManifest.xml file.

```
<uses-permission android:name="android.permission.INTERNET">
```
Add activity in AndroidManifest.xml file.
For the App Link Support:
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

For the DeepLink support
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


## Step 4 - Add code to start the Connect SDK

The Connect class contains a start method that when called, starts an activity with the supplied event handler. The SDK only allows a single instance of the Connect activity to run. If you start Connect while a Connect activity is already running, a RuntimeException is thrown.

For the App Link support
```Connect.start(this, url, "https://yourdomain.com/connect", eventHandler);```

For the Deep Link  support
``Connect.start(this, url, "{deep_link_app_name}://", eventHandler);````

### Connect Class

The Connect Android SDK’s main component is the Connect class that contains a static start method, which runs an activity that connects with the EventHandler.

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
| redirectUrl | redirectUrl to redirect back to app. This parameter is optional and only required to used in app to app flow. |
| eventHandler | A class implementing the EventHandler interface. |

See [Generate 2.0 Connect URL APIs](https://developer.mastercard.com/open-banking-us/documentation/connect/generate-2-connect-url-apis/)



## EventHandler Interface

Throughout Connect’s flow, events about the state of the web application are sent as JSONObjects to the EventHandler methods.

> **_NOTE:_**  The onUserEvent handler will not return anything unless you’re specifically targeting Connect 2.0.

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


## Process Restarts

Android sometimes stops your application’s process and restarts it when your application is re-focused. If this happens, the Connect activity automatically finishes when the application resumes. If you want Connect to run again, call the start method. See [Connect Class.](#connect-class)
