package com.finicity.connect.sdk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

class ConnectJsInterface {
    private Activity activity;
    private EventListener eventListener;

    public ConnectJsInterface(Activity activity, EventListener eventListener) {
        this.activity = activity;
        this.eventListener = eventListener;
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
            eventListener.onCancel();
            this.finishActivity();
        } else if(eventType.equals("done")) {
            eventListener.onDone(jsonMessage);
            this.finishActivity();
        } else if(eventType.equals("error")) {
            eventListener.onError(jsonMessage);
            this.finishActivity();
        }
    }

    private void finishActivity() {
        activity.finish();
    }

    @JavascriptInterface
    public void openLinkInBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(browserIntent);
    }
}
