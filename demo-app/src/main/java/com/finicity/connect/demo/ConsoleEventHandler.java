package com.finicity.connect.demo;

import com.finicity.connect.sdk.EventHandler;

import org.json.JSONObject;

public class ConsoleEventHandler implements EventHandler {
    @Override
    public void onLoaded() {
        System.out.println(">>> ConsoleEventHandler: Received loaded event");
    }

    @Override
    public void onDone(JSONObject doneEvent) {
        System.out.println(">>> ConsoleEventHandler: Received done event\n>>>>>> " + doneEvent.toString());
    }

    @Override
    public void onCancel() {
        System.out.println(">>> ConsoleEventHandler: Received Cancel event");
    }

    @Override
    public void onError(JSONObject errorEvent) {
        System.out.println(">>> ConsoleEventHandler: Received Error event\n>>>>>> " + errorEvent.toString());
    }

    @Override
    public void onRouteEvent(JSONObject routeEvent) {
        System.out.println(">>> ConsoleEventHandler: Received Route event\n>>>>>> " + routeEvent.toString());
    }

    @Override
    public void onUserEvent(JSONObject userEvent) {
        System.out.println(">>> ConsoleEventHandler: Received User event\n>>>>>> " + userEvent.toString());

    }
}
