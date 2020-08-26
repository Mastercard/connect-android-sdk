package com.finicity.connect.sdk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

class ConnectJsInterface {
    private Activity activity;
    private EventHandler eventHandler;

    public ConnectJsInterface(Activity activity, EventHandler eventHandler) {
        this.activity = activity;
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
            eventHandler.onCancel();
            this.finishActivity();
        } else if(eventType.equals("done")) {
            eventHandler.onDone(getEventData(jsonMessage));
            this.finishActivity();
        } else if(eventType.equals("error")) {
            eventHandler.onError(getEventData(jsonMessage));
            this.finishActivity();
        } else if(eventType.equals("route")) {
            eventHandler.onRouteEvent(getEventData(jsonMessage));
        } else if(eventType.equals("user")) {
            eventHandler.onUserEvent(getEventData(jsonMessage));
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

    @JavascriptInterface
    public void openLinkInBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(browserIntent);
    }
}
