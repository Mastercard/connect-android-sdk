package com.mastercard.openbanking.connect.generateurl;

import org.json.JSONObject;


public interface ConnectGenerateUrlCallbackHandler {
    void onError(String error);
    void onSuccess(String link);
}
