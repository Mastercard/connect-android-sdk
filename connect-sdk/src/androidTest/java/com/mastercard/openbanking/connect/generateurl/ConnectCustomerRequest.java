package com.mastercard.openbanking.connect.generateurl;

public class ConnectCustomerRequest {

    private String username;
    private String firstName;
    private String lastName;

    ConnectCustomerRequest(String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

}
