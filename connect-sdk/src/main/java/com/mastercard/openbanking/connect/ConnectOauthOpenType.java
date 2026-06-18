package com.mastercard.openbanking.connect;

public enum ConnectOauthOpenType {
    SECURE_CONTAINER("secure-container"),
    FI_APP("xfi-app");

    private final String value;

    ConnectOauthOpenType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ConnectOauthOpenType fromValue(String value) {
        for (ConnectOauthOpenType type : ConnectOauthOpenType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}

