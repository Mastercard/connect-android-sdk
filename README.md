# Connect Android SDK

## Compatibility
This SDK is built with minSdkVersion 17, and will work with Android >=4.2 (Jelly Bean) or higher.

## How to use add SDK in your project
### 1. Add the SDK dependency to the project
_Currently, the SDK is built and deployed to Finicity's Artifactory service, but will eventually be deployed to a public Maven repository._

##### Maven Coordinates
| status | groupId | artifactId | version |
| ------ | ------- | ---------- | ------- |
| snapshots | com.finicity.connect | connect-sdk | 0.0.1-SNAPSHOT |
| releases | com.finicity.connect | connect-sdk | _TODO_ |

##### Finicity Artifactory Repositories
_Snapshots_: https://repo.finicity.com/artifactory/libs-snapshot-local  
_Releases_: https://repo.finicity.com/artifactory/libs-release-local

##### Public Maven Repository  
_TODO_

_*If you are using androidx, make sure that `android.enableJetifier` is set to `true` in `gradle.properties`.  It will probably already be set, but is required for this library to work.  This library is designed to support either namespace strategy (i.e. `android.support` or `androidx`).  For more info on `androidx` and Jetifier, see [official documentation](https://developer.android.com/jetpack/androidx/migrate)._

### 2. Update your Android app project settings
- Add ```<uses-permission android:name="android.permission.INTERNET" />``` to AndroidManifest.xml
- Add the Connect Activity ```<activity android:name="com.finicity.connect.sdk.Connect"></activity>``` to AndroidManifest.xml in the `application` section.

### 3. Add code to launch the Connect SDK

#### Connect class
The main component of the Connect SDK is the `Connect` class.  It contains a static `start` method, which launches an Activity and takes care of wiring up the supplied event listener.

_*The SDK only allows a single Connect Activity to run at a time.  If you try to launch Connect while a Connect Activity is currently running, a RuntimeException will be thrown._

```public static void start(Context context, String connectUrl, EventListener eventListener)```

| Argument | Description | Note |
| -------- | ----------- | ---- |
| context | The Android Context to launch the SDK from |
| connectUrl | The Connect URL to launch in the SDK.  This URL is generated through a call to the Finicity API.  See https://community.finicity.com/s/article/Generate-Finicity-Connect-URL for more details on how to do this. | The `redirectUrl` referenced in the Connect documentation is not used by the Android SDK and will be ignored. |
| eventListener | A class implementing the `EventListener` interface (_see below_) |

### EventListener interface
The Connect SDK embeds the Connect Web Application in a WebView.  During the Connect flow, the web application sends events about its state to the SDK.  These events are received as JSONObjects by the EventListener methods.

_EventListener code_
```
public interface EventListener {
    void onDone(JSONObject doneEvent);
    void onCancel(JSONObject cancelEvent);
    void onError(JSONObject errorEvent);
    void onRoute(JSONObject routeEvent);
}
```
| Event | Description |
| ----- | ----------- |
| done | Sent when the user successfully completes the Connect flow |
| cancel | Sent when the user cancels the Connect flow |
| error | Sent when something went wrong |
| route | The current route/screen in Connect has changed |

For more information on each of these events, when they are sent, and what they represent, see https://docs.finicity.com/embedded-finicity-connect-web-based/ and https://docs.finicity.com/connect-web-sdk-route-events/.

### 4. Stopping the Connect Activity
The activity launched by the SDK will automatically finish on the `done`, `cancel`, and `error` events described above (after sending the events to the `EventListener` instance supplied when starting Connect).

Alternatively, the static `Connect.finishCurrentActivity()` method can be invoked to manually finish the Connect Activity.  If there is no Connect Activity currently running, calling this method will throw a RuntimeException.

## How to build and deploy the SDK to a Finicity Artifactory repository

1. Clone this repo.
2. Install Android Studio (available at https://developer.android.com/studio).
3. Update the `JAVA_HOME` environment variable to point to the JDK embedded in Android Studio.
    - Open the project, click `File -> Project Structure` and the look at `JDK Location` to find this value. _*This works on Android Studio 3.5.3 but could change._
4. Set `MAVEN_REPO_USER` and `MAVEN_REPO_PASS` environment variables. _These are not listed here for security purposes but can be provided on request._
5. (optional) Update the sdk version if needed.  This is specified on line 6 of the SDK's `build.gradle` file ([link](https://gitlab.fini.city/connect/android-sdk/blob/master/connect-sdk/build.gradle#L6)).
6. In a terminal, from the root of the project, run either of the following commands
    - To push to the libs-snapshot-local repository, run `./gradlew publishLibraryPublicationToLibs-snapshot-localRepository`
    - To push to the libs-release-local repository, run `./gradlew publishLibraryPublicationToLibs-release-localRepository`


