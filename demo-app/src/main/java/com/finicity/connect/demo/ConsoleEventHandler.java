package com.finicity.connect.demo;

import android.util.Log;

import com.finicity.connect.sdk.EventHandler;

import org.json.JSONObject;

public class ConsoleEventHandler implements EventHandler {
    private static final String TAG = "ConsoleEventHandler";

    @Override
    public void onLoad() {
        Log.i(TAG, ">>> ConsoleEventHandler: Received Loaded event");
    }

    @Override
    public void onDone(JSONObject doneEvent) {
        Log.i(TAG, ">>> ConsoleEventHandler: Received Done event\n>>>>>> " + doneEvent.toString());
    }

    @Override
    public void onCancel(JSONObject cancelEvent) {
        Log.i(TAG, ">>> ConsoleEventHandler: Received Cancel event\n>>>>>> " + cancelEvent.toString());
    }

    @Override
    public void onError(JSONObject errorEvent) {
        Log.i(TAG, ">>> ConsoleEventHandler: Received Error event\n>>>>>> " + errorEvent.toString());
    }

    @Override
    public void onRoute(JSONObject routeEvent) {
        Log.i(TAG, ">>> ConsoleEventHandler: Received Route event\n>>>>>> " + routeEvent.toString());
    }

    @Override
    public void onUser(JSONObject userEvent) {
        Log.i(TAG, ">>> ConsoleEventHandler: Received User event\n>>>>>> " + userEvent.toString());
    }
}
