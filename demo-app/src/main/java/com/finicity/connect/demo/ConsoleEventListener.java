package com.finicity.connect.demo;

import com.finicity.connect.sdk.EventListener;

import org.json.JSONObject;

public class ConsoleEventListener implements EventListener {
    @Override
    public void onCancel(JSONObject cancelEvent) {
        System.out.println(">>> Received Cancel event\n>>> " + cancelEvent.toString());
    }

    @Override
    public void onDone(JSONObject doneEvent) {
        System.out.println(">>> Received done event\n>>> " + doneEvent.toString());
    }

    @Override
    public void onError(JSONObject errorEvent) {
        System.out.println(">>> Received Error event\n>>> " + errorEvent.toString());
    }

    @Override
    public void onRoute(JSONObject routeEvent) {
        System.out.println(">>> Received Route event\n>>> " + routeEvent.toString());
    }
}
