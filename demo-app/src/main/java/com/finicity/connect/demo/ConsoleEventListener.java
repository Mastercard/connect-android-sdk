package com.finicity.connect.demo;

import android.util.Log;

import com.finicity.connect.sdk.EventListener;

import org.json.JSONObject;

public class ConsoleEventListener implements EventListener {
    private static final String TAG = "ConsoleEventListener";

    @Override
    public void onLoaded() {
        Log.i(TAG, ">>> ConsoleEventListener: Received Loaded event");
    }

    @Override
    public void onDone(JSONObject doneEvent) {
        Log.i(TAG, ">>> ConsoleEventListener: Received Done event\n>>>>>> " + doneEvent.toString());
    }

    @Override
    public void onCancel() {
        Log.i(TAG, ">>> ConsoleEventListener: Received Cancel event");
    }

    @Override
    public void onError(JSONObject errorEvent) {
        Log.i(TAG, ">>> ConsoleEventListener: Received Error event\n>>>>>> " + errorEvent.toString());
    }
}
