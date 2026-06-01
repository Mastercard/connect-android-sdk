package com.mastercard.openbanking.connect;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;

import androidx.browser.customtabs.CustomTabsCallback;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;

import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

class ConnectJsInterface {
    private Activity activity;
    private Connect mConnect;
    private EventHandler eventHandler;
    private boolean mCustomTabStarted = false;
    private CustomTabsClient customTabsClient;
    private CustomTabsSession customTabsSession;
    private CustomTabsServiceConnection customTabsServiceConnection;
    CustomTabsCallback callback;
    private boolean mNavigationFailed = false;

    public ConnectJsInterface(Activity activity, EventHandler eventHandler) {
        this.activity = activity;
        this.mConnect = (Connect) activity;
        this.eventHandler = eventHandler;
    }

    @JavascriptInterface
    public void postMessage(String message) {
        JSONObject jsonMessage;
        String eventType;

        try {
            jsonMessage = new JSONObject(message);
            eventType = jsonMessage.getString("type");
        } catch(Exception e) {
            return;
        }

        // Invoke appropriate event listener method
        switch (eventType) {
            case "cancel":
                eventHandler.onCancel(getEventData(jsonMessage));
                this.finishActivity();
                break;
            case "done":
                eventHandler.onDone(getEventData(jsonMessage));
                this.finishActivity();
                break;
            case "error":
                eventHandler.onError(getEventData(jsonMessage));
                this.finishActivity();
                break;
            case "route":
                eventHandler.onRoute(getEventData(jsonMessage));
                break;
            case "user":
                eventHandler.onUser(getEventData(jsonMessage));
                break;
            case "ack":
                mConnect.stopPingTimer();
                break;
            case "url":
                try {
                    String url = jsonMessage.getString("url");
                    openLinkInCustomTab(url);
                } catch (JSONException e) {
                    Log.e("Connect Android SDK","Error parsing the URL");
                }
                break;
            case "closePopup":
                closeCustomTab();
                break;

            case "trackPopupBlockedEvent":
                this.bindCustomServiceAndAddCallback();
                break;

            default:
                break;
        }

    }

    private void finishActivity() {
        activity.finish();
    }

    private JSONObject getEventData(JSONObject rootEvent) {
        // Parse out data field, or query field if data does not exist
        // This is for backwards compatibility with future updates to Connect.
        JSONObject eventData = new JSONObject();
        try{
            if (rootEvent.has("data")) {
                eventData = rootEvent.getJSONObject("data");
            } else if (rootEvent.has("query")) {
                eventData = rootEvent.getJSONObject("query");
            } else {
                Log.e("Connect Android SDK", "Neither 'data' nor 'query' found in the event");
            }
        } catch(JSONException e){
            Log.e("Connect Android SDK", "Error parsing the Event Data", e);
        }
        return eventData;
    }

    private void bindCustomServiceAndAddCallback() {
        bindCustomTabsService();

        callback = new CustomTabsCallback() {
            @Override
            public void onNavigationEvent(int navigationEvent, Bundle extras) {
                switch (navigationEvent) {
                    case NAVIGATION_STARTED:
                        // Reset failure flag on each new navigation
                        mNavigationFailed = false;
                        Log.d("CustomTabs", "Page loading started");
                        break;
                    case NAVIGATION_FAILED:
                        // Mark as failed — Chrome will still fire NAVIGATION_FINISHED after this,
                        // so we use this flag to suppress the false "success" log.
                        mNavigationFailed = true;
                        Log.d("CustomTabs", "Page not loaded");
                        mConnect.postWindowBlockedMessage();
                        break;
                    case NAVIGATION_FINISHED:
                        // Chrome fires NAVIGATION_FINISHED even after NAVIGATION_FAILED (blocked/error URLs).
                        // Only treat it as a real success if no failure was recorded.
                        if (!mNavigationFailed) {
                            Log.d("CustomTabs", "Page loaded successfully");
                        }
                        break;
                }
            }
        };
    }

    private void bindCustomTabsService() {
        String packageName = CustomTabsClient.getPackageName(activity, null);
        if (packageName == null) {
            Log.w("CustomTabs", "No Custom Tabs provider found");
            return;
        }
        customTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                customTabsClient = client;
                customTabsClient.warmup(0);
            }

            @Override public void onServiceDisconnected(ComponentName name) {
                customTabsClient = null;
                customTabsSession = null;
            }
        };
        CustomTabsClient.bindCustomTabsService(activity, packageName, customTabsServiceConnection);
    }

    public void openLinkInCustomTab(String url) {
        Uri uri = Uri.parse(url); // Default to example.com if parsing fails

        // Try to open in an external (non-browser) app first.
        // FLAG_ACTIVITY_REQUIRE_NON_BROWSER (API 30+) is the most reliable way:
        // Android throws ActivityNotFoundException if no non-browser app handles the URI,
        // so we catch it and fall back to Custom Tabs.
        // On API < 30 we fall back to a manual PackageManager check.
        if (tryOpenInExternalApp(uri)) {
            mCustomTabStarted = true;
            return;
        }

        // No external app — open in Custom Tabs with callback
        if (customTabsClient != null) {
            customTabsSession = customTabsClient.newSession(callback);
        } else {
            customTabsSession = null;
        }

        CustomTabsIntent.Builder builder = (customTabsSession != null)
                ? new CustomTabsIntent.Builder(customTabsSession)
                : new CustomTabsIntent.Builder();

        CustomTabsIntent customTabsIntent = builder.build();
        Intent intent = customTabsIntent.intent;
        intent.setData(uri);

        mCustomTabStarted = true;
        activity.startActivity(CustomTabsActivityManager.createStartIntent(activity, intent, activity));
    }

    private boolean tryOpenInExternalApp(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ — FLAG_ACTIVITY_REQUIRE_NON_BROWSER is the reliable path
            try {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, uri);
                appIntent.addFlags(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER);
                activity.startActivity(appIntent);
                Log.d("CustomTabs", "Opened in external app (API 30+): " + uri.getHost());
                return true;
            } catch (ActivityNotFoundException e) {
                // No non-browser app handles this URL — fall through to Custom Tabs
                Log.d("CustomTabs", "No external app found (API 30+), using CCT: " + uri.getHost());
                return false;
            }
        } else {
            // API < 30 — manually check PackageManager for non-browser handlers
            if (hasNonBrowserHandler(uri)) {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(appIntent);
                Log.d("CustomTabs", "Opened in external app (API<30): " + uri.getHost());
                return true;
            }
            return false;
        }
    }

    /**
     * API < 30 fallback: returns true if at least one non-browser app handles this URI.
     */
    private boolean hasNonBrowserHandler(Uri uri) {
        Intent probe = new Intent(Intent.ACTION_VIEW, uri);
        PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> handlers = pm.queryIntentActivities(probe, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : handlers) {
            if (!isBrowserPackage(info.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Identifies well-known browser packages so they are excluded from the universal-link check.
     */
    private boolean isBrowserPackage(String packageName) {
        switch (packageName) {
            case "com.android.chrome":
            case "org.mozilla.firefox":
            case "com.opera.browser":
            case "com.microsoft.edge":
            case "com.brave.browser":
            case "com.samsung.android.app.sbrowser":
            case "com.google.android.apps.chrome":
                return true;
            default:
                return false;
        }
    }

    public void closeCustomTab() {
        if (!mCustomTabStarted) {
            return;
        }
        mCustomTabStarted = false;
        activity.startActivity(CustomTabsActivityManager.createDismissIntent(activity));
    }

    public void unbindCustomTabsService() {
        if (customTabsServiceConnection != null && activity != null) {
            try {
                activity.unbindService(customTabsServiceConnection);
                Log.d("CustomTabs", "Custom Tabs service unbound");
            } catch (IllegalArgumentException e) {
                Log.w("CustomTabs", "Service was not bound, skipping unbind: " + e.getMessage());
            }
        }
        // Always clear references regardless of activity state to prevent leaks
        customTabsServiceConnection = null;
        customTabsClient = null;
        customTabsSession = null;
    }

}
