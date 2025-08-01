package com.mybank.atmweb.domain;

public enum BankType {
    A("KB","국민은행", "110"),
    B("SHINHAN","신한은행", "120"),
    C("WOORI","우리은행", "333");

    private final String name;
    private final String displayName;
    private final String prefix;

    BankType(String name, String displayName, String prefix) {
        this.displayName = displayName;
        this.prefix = prefix;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }
}
