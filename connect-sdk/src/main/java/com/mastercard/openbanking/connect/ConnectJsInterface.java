package com.mastercard.openbanking.connect;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

class ConnectJsInterface {
    private Activity activity;
    private Connect mConnect;
    private EventHandler eventHandler;
    private boolean mCustomTabStarted = false;

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

        try {
            eventData = rootEvent.getJSONObject("data");
        } catch(Exception e) {
            try {
                eventData = rootEvent.getJSONObject("query");
            } catch(Exception e2) {
                Log.e("Connect Android SDK","Error parsing the Event Data");
            }
        }

        return eventData;
    }

    public void openLinkInCustomTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        Intent intent = customTabsIntent.intent;
        intent.setData(Uri.parse(url));
        mCustomTabStarted = true;
        activity.startActivity(CustomTabsActivityManager.createStartIntent(activity, intent, activity));
    }

    public void closeCustomTab() {
        if (!mCustomTabStarted) {
            return;
        }
        mCustomTabStarted = false;
        activity.startActivity(CustomTabsActivityManager.createDismissIntent(activity));
    }

}
