package com.finicity.connect.sdk;

import org.json.JSONObject;

@Deprecated
public interface EventListener {
    void onLoaded();
    void onDone(JSONObject doneEvent);
    void onCancel();
    void onError(JSONObject errorEvent);
}
