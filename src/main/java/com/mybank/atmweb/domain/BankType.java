package com.mybank.atmweb.domain;

public enum BankType {
    KB("국민은행", "110"),
    SHINHAN("신한은행", "120"),
    WOORI("우리은행", "333");

    private final String displayName;
    private final String prefix;

    BankType(String displayName, String prefix) {
        this.displayName = displayName;
        this.prefix = prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }
}
