package com.finicity.connect.sdk;

import org.json.JSONObject;

public interface EventHandler {
    void onLoad();
    void onDone(JSONObject doneEvent);
    void onCancel(JSONObject cancelEvent);
    void onError(JSONObject errorEvent);
    void onRoute(JSONObject routeEvent);
    void onUser(JSONObject userEvent);
}
