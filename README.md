# Connect Android SDK

## Overview

The Connect Android SDK allows you to embed Finicity Connect anywhere you want within your own mobile applications.


## Compatibility

The Connect Android SDK supports the following Android versions.

* Android 5.0 (Lollipop) or later

* minSdkVersion 21 or later


## Step 1- Add the SDK dependency to your project

### Using JCenter

The Connect Android SDK is now available in JCenter for distribution.  To integrate the SDK into your android project add the following line into the dependency section in build.gradle to get the latest version of the SDK.

```
dependencies {
    implementation 'com.finicity.connect:connect-sdk:+'
}
```

### Download the SDK from Finicity Developer Portal

[connect-sdk-v1.0.3.aar](https://prod-fcconnect-public.s3-us-west-2.amazonaws.com/sdk/android/connect-sdk-v1.0.3.aar) ([sha256](https://prod-fcconnect-public.s3-us-west-2.amazonaws.com/sdk/android/connect-sdk-v1.0.3.aar.sha256))

1. Add the SDK library to your Android project.
    See [Add your library as a dependency](https://developer.android.com/studio/projects/android-library#AddDependency)
   
2. For projects using Android:
    a. Open the gradle.properties file.
    b. Set **android.enableJetifier** to **true.**
    
    See Android help: [Migrating to AndroidX.](https://developer.android.com/jetpack/androidx/migrate)


## Step 2 - Update Android application settings

Add internet permissions to the AndroidManifest.xml file.

```
<uses-permission android:name="android.permission.INTERNET">
```


## Step 3 - Add code to start the Connect SDK

The Connect class contains a start method that when called, starts an activity with the supplied event listener. The SDK only allows a single instance of the Connect activity to run. If you start Connect while a Connect activity is already running, a RuntimeException is thrown.


### Connect Class

The Connect Android SDK’s main component is the Connect class that contains a static start method, which runs an activity that connects with the EventHandler.

```
Java
public static void start(Context context, String connectUrl, EventHandler eventHandler)
```

```
Kotlin
fun start(context: Context, connectUrl: String?, eventHandler: EventHandler?)
```

| Argument | Description |
| ------ | ------ |
| context | The Android Context is referenced by Connect when an activity starts. |
| connectUrl | The SDK loads the Connect URL. |
| eventHandler | A class implementing the EventHandler interface. |

See [Generate 2.0 Connect URL APIs](https://docs.finicity.com/migrate-to-connect-web-sdk-2-0/#migrate-connect-web-sdk-1)


## EventHandler Interface

Throughout Connect’s flow, events about the state of the web application are sent as JSONObjects to the EventHandler methods.

> **_NOTE:_**  The onUserEvent handler will not return anything unless you’re specifically targeting Connect 2.0.

```
Java
public interface EventHandler {
    void onLoaded();
    void onDone(JSONObject doneEvent);
    void onCancel();
    void onError(JSONObject errorEvent);
    void onRouteEvent(JSONObject routeEvent);
    void onUserEvent(JSONObject userEvent);
}
```

```
Kotlin
interface EventHandler {
    fun onLoaded()
    fun onDone(doneEvent: JSONObject?)
    fun onCancel()
    fun onError(errorEvent: JSONObject?)
    fun onRouteEvent(routeEvent: JSONObject?)
    fun onUserEvent(userEvent: JSONObject?)
}
```

Event | Description |
| ------ | ------ |
| loaded | Sent when the Connect web page is loaded and ready to display. |
| done | Sent when the user successfully completes the Connect appliction. |
| cancel | Sent when the user cancels the Connect application.|
| error | Sent when there is an error during the Connect application. |
| route | Sent when the user navigates to a new route or screen in Connect. |
| user | Connect 2.0 (only) Sent when user events occur in Connect. |


### Manually stop a connect activity

The Connect activity will automatically finish on done, cancel, and error events.

You can manually finish a Connect activity by invoking:

```
Connect.finishCurrentActivity()
```


### Process Restarts

Android sometimes stops your application’s process and restarts it when your application is re-focused. If this happens, the Connect activity automatically finishes when the application resumes. If you want Connect to run again, call the start method. See [Connect Class.](#connect-class)
