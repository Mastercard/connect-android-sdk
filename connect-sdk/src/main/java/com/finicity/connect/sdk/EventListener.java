package com.finicity.connect.sdk;

import org.json.JSONObject;

public interface EventListener {
    void onDone(JSONObject doneEvent);
    void onCancel(JSONObject cancelEvent);
    void onError(JSONObject errorEvent);
}
