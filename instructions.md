# Connect Android SDK Instructions

## Official Documentation
Use these links as the source of truth.

- US integration overview: https://developer.mastercard.com/open-banking-us/documentation/connect/integrating/
- US Android SDK guide: https://developer.mastercard.com/open-banking-us/documentation/connect/integrating/android/android-sdk/
- Australia Connect docs: https://developer.mastercard.com/open-banking-au/documentation/connect/
- SDK versions: https://central.sonatype.com/artifact/com.mastercard.openbanking.connect/connect-sdk

## Mobile App Integration Steps
### 1) Prepare backend
- Generate a Data Connect URL from backend APIs.
- Provide that `connectUrl` to your Android app.
- Prefer passing `redirectUri` from backend or `redirectUrl` in SDK start call.

### 2) Install SDK in app
- Ensure `google()` and `mavenCentral()` are configured.
- Ensure `minSdkVersion` is 21+.
- Add dependency:

```gradle
implementation 'com.mastercard.openbanking.connect:connect-sdk:<latest-version>'
```

### 3) Configure AndroidManifest.xml
- Add internet permission:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

- Add `com.mastercard.openbanking.connect.Connect` activity with `singleTask` and a browsable intent filter.
- Prefer App Links (`https://...`) for redirect handling.

### 4) Start SDK flow

```java
import com.mastercard.openbanking.connect.Connect;
import com.mastercard.openbanking.connect.EventHandler;

Connect.start(this, connectUrl, "https://example.com/mastercardConnect", eventHandler);
```

Deep link alternative:

```java
Connect.start(this, connectUrl, "yourapp://", eventHandler);
```

### 5) Handle callbacks
Implement and handle:
- `onLoad`
- `onDone`
- `onCancel`
- `onError`
- `onRoute` / `onRouteEvent`
- `onUser` / `onUserEvent`

### 6) Validate app-to-app behavior
- Use App Links for best reliability.
- Verify redirect back to host app after FI OAuth.
- Verify fallback to browser when FI app is not installed.

### 7) Optional manual close

```java
Connect.finishCurrentActivity();
```

## Project Commands
Run from repository root.

```bash
./gradlew clean
./gradlew build
./gradlew :connect-sdk:assemble
./gradlew :connect-sdk:testDebugUnitTest
./gradlew :connect-sdk:connectedDebugAndroidTest
./gradlew :connect-sdk:jacocoTestReport
```

## Notes
- Supported: Android 5.0+ (`minSdkVersion 21`)
- Not intended for Mastercard Open Finance Europe
- Current module version in this repo: `3.0.7`
