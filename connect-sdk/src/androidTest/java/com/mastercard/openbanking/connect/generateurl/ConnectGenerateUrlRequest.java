package com.mastercard.openbanking.connect.generateurl;

public class ConnectGenerateUrlRequest {
    private String partnerId;
    private String customerId;
    private String consumerId;
    private String redirectUri;

    ConnectGenerateUrlRequest(String partnerId, String customerId, String consumerId) {
        this.partnerId = partnerId;
        this.customerId = customerId;
        this. consumerId = consumerId;
    }
}
