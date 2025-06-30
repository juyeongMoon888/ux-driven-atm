package com.mybank.atmweb.domain;

public enum Bank {
    KB("국민은행"),
    NH("농협은행"),
    SH("신한은행"),
    WR("우리은행"),
    IBK("기업은행");

    private final String displayName;


    Bank(String displayName) {
        this.displayName = displayName;
    }

    public String getCode() {
        return name(); //enum name이 code 역할
    }

    public String getDisplayName() {
        return displayName;
    }
}
