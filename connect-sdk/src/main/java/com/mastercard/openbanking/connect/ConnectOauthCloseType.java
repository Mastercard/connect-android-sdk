package com.mastercard.openbanking.connect;

public enum ConnectOauthCloseType {
    CONNECT_CLIENT_EVENT("connect-client-event"),
    PARTNER_REDIRECTION("partner-redirection"),
    USER_CLOSED("user-closed");

    private final String value;

    ConnectOauthCloseType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ConnectOauthCloseType fromValue(String value) {
        for (ConnectOauthCloseType type : ConnectOauthCloseType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}

