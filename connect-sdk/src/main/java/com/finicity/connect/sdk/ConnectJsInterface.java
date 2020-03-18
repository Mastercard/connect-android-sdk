package com.finicity.connect.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

class ConnectJsInterface {
    private Context context;
    private EventListener eventListener;

    public ConnectJsInterface(Context context, EventListener eventListener) {
        this.context = context;
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
            eventListener.onCancel(jsonMessage);
            this.finishActivity();
        } else if(eventType.equals("done")) {
            eventListener.onDone(jsonMessage);
            this.finishActivity();
        } else if(eventType.equals("error")) {
            eventListener.onError(jsonMessage);
            this.finishActivity();
        } else if(eventType.equals("route")) {
            eventListener.onRoute(jsonMessage);
        }
    }

    private void finishActivity() {
        ((Activity) context).finish();
    }

    @JavascriptInterface
    public void openLinkInBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }
}
