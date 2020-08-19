package com.finicity.connect.sdk;

import org.json.JSONObject;

class EventListenerWrapper implements EventHandler {
    private EventListener eventListener;

    public EventListenerWrapper(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void onLoaded() {
        this.eventListener.onLoaded();
    }

    public void onDone(JSONObject doneEvent) {
        this.eventListener.onDone(doneEvent);
    }

    public void onCancel() {
        this.eventListener.onCancel();
    }

    public void onError(JSONObject errorEvent) {
        this.eventListener.onError(errorEvent);
    }

    public void onRouteEvent(JSONObject routeEvent) {}
    public void onUserEvent(JSONObject userEvent) {}
}
