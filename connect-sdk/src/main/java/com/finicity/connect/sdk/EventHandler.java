package com.finicity.connect.sdk;

import org.json.JSONObject;

public interface EventHandler extends EventListener {
    void onRouteEvent(JSONObject routeEvent);
    void onUserEvent(JSONObject userEvent);
}
