package com.finicity.connect.sdk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

class ConnectJsInterface {
    private Activity activity;
    private Connect mConnect;
    private EventHandler eventHandler;
    private Boolean mCustomTabStarted = false;

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
        if(eventType.equals("cancel")) {
            eventHandler.onCancel(getEventData(jsonMessage));
            this.finishActivity();
        } else if(eventType.equals("done")) {
            eventHandler.onDone(getEventData(jsonMessage));
            this.finishActivity();
        } else if(eventType.equals("error")) {
            eventHandler.onError(getEventData(jsonMessage));
            this.finishActivity();
        } else if(eventType.equals("route")) {
            eventHandler.onRoute(getEventData(jsonMessage));
        } else if(eventType.equals("user")) {
            eventHandler.onUser(getEventData(jsonMessage));
        } else if(eventType.equals("ack")) {
            mConnect.stopPingTimer();
        } else if(eventType.equals("url")) {
            try {
                String url = jsonMessage.getString("url");
                openLinkInCustomTab(url);
            } catch (JSONException e) {
            }
        } else if(eventType.equals("closePopup")) {
            closeCustomTab();
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
            } catch(Exception e2) { }
        }

        return eventData;
    }

    public void openLinkInCustomTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        Intent intent = customTabsIntent.intent;
        intent.setData(Uri.parse(url));
        mCustomTabStarted = true;
        activity.startActivity(CustomTabsActivityManager.createStartIntent(activity, intent, activity)); // , customTabsIntent.startAnimationBundle);
    }

    public void closeCustomTab() {
        if (!mCustomTabStarted) {
            return;
        }
        mCustomTabStarted = false;
        activity.startActivity(CustomTabsActivityManager.createDismissIntent(activity));
    }

}
