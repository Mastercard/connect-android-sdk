package com.finicity.connect.demo;

import com.finicity.connect.sdk.EventListener;

import org.json.JSONObject;

public class ConsoleEventListener implements EventListener {

    @Override
    public void onLoaded() {
        System.out.println(">>> Received loaded event");
    }

    @Override
    public void onDone(JSONObject doneEvent) {
        System.out.println(">>> Received done event\n>>> " + doneEvent.toString());
    }

    @Override
    public void onCancel() {
        System.out.println(">>> Received Cancel event");
    }

    @Override
    public void onError(JSONObject errorEvent) {
        System.out.println(">>> Received Error event\n>>> " + errorEvent.toString());
    }
}
