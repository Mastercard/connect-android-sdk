# Changelog

### 3.0.0 (October 10, 2023)
Enhancement:
- Newly added redirectUrl parameter inside SDK to support universal link and deeplink, It will enhance app to app seamless communication.
### Breaking changes
- SDK support for deepLinkUrl is deprecated, Please use redirectUrl parameter instead, it will support both universal link and deeplink.
  Please follow readme documentation. https://github.com/Mastercard/connect-android-sdk#readme


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

