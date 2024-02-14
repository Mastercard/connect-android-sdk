# Changelog

### 3.0.2 (February, 15, 2024)

Patch:
- Handled multiple SDK ping events issue.
- Added error message in developer console for invalid redirectUrl passed for App to App Authentication.
- Removed initial loader generated via Connect SDK.


### 3.0.1 (December, 21, 2023)

Patch:
- Added loader before connect session initialization for better user experience.
- Added warning message if invalid Redirect URL is passed for App to App Authentication.


### 3.0.0 (August 10, 2023)

Enhancement:
- Enhanced App To App OAuth Flow with newly added redirectUrl parameter inside Connect Android SDK to support App link and deeplink for navigation between mobile apps. For details on App To App refer [documentation here](https://developer.mastercard.com/open-banking-us/documentation/connect/mobile-sdks/)

Breaking changes:
- Connect Android SDK support for deepLinkUrl is deprecated from this version, Please use the redirectUrl parameter instead, it will support both App link and deeplink. Please follow the readme documentation for [more details](https://github.com/Mastercard/connect-android-sdk#readme)

### 2.3.0 (June 22, 2023)

Enhancement:
- Added optional deeplinkUrl param to support navigation back to app (App to App Authentication).

### 2.1.0 (June 3, 2023)

Patch:
- Fix applied for opening Oauth popup in Chrome custom tabs only.


### 2.0.0 (March 10, 2022)

Patch:
- Updated EventHandler interface to match other SDKs.
- Removed deprecated EventListener interface.  Use EventHandler interface to receive Connect events.  
- Send SDK version and platform type to Connect.
- Added Chrome custom tab support for displaying Oauth popup.


### 1.0.4 (September 22, 2020)

Patch:
- Updated user agent for Oauth popup to prevent webview from being blocked.
- Set versionCode to 104 and versionName to "1.0.4" in Android manifest.

### 1.0.2 (August 19, 2020)

Patch:
- Updated Connect.onCreate() to first call super.onCreate() before doing the EventListener / process restart check (see last change).  Turns out finishing an activity before calling super.onCreate() causes an exception.

### 1.0.1 (Summer 2020)

Patch:
- Added logic to Connect.onCreate() to immediately finish if it detects that Android has restarted the process.  This is because the static EventListener reference in Connect no longer exists and must be supplied by the parent activity.

