package com.mastercard.openbanking.connect.generateurl;

class ConnectBirthday {
    private int year;
    private int month;
    private int dayOfMonth;

    ConnectBirthday(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }
}

public class ConnectConsumerRequest {
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String phone;
    private String ssn;
    private ConnectBirthday birthday;

    ConnectConsumerRequest(
            String firstName,
            String lastName,
            String address,
            String city,
            String state,
            String zip,
            String phone,
            String ssn,
            int year,
            int month,
            int dayOfMonth
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.phone = phone;
        this.ssn = ssn;
        this.birthday = new ConnectBirthday(year, month, dayOfMonth);
    }
}
