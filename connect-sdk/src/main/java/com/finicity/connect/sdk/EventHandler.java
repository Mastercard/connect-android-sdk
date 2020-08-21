package com.finicity.connect.sdk;

import org.json.JSONObject;

public interface EventHandler {
    void onLoaded();
    void onDone(JSONObject doneEvent);
    void onCancel();
    void onError(JSONObject errorEvent);
    void onRouteEvent(JSONObject routeEvent);
    void onUserEvent(JSONObject userEvent);
}
