package com.mybank.atmweb.domain;

public enum BankType {
    KB(false, "국민은행", "110"),
    SHINHAN(false, "신한은행", "120"),
    WOORI(false, "우리은행", "333"),
    MYBANK(true, "마이은행", "235");

    private final boolean internal;
    private final String displayName;
    private final String prefix;


    BankType(boolean internal, String displayName, String prefix) {
        this.internal = internal;
        this.displayName = displayName;
        this.prefix = prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isInternal() {
        return internal;
    }
}
