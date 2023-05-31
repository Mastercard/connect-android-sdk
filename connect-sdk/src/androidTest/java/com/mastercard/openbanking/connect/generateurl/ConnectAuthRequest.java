package com.mastercard.openbanking.connect.generateurl;

public class ConnectAuthRequest {
    private String partnerId;
    private String partnerSecret;

    ConnectAuthRequest(String partnerId, String partnerSecret) {
        this.partnerId = partnerId;
        this.partnerSecret = partnerSecret;
    }
}
