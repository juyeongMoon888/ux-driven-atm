package com.mybank.atmweb.domain;

public enum Bank {
    KB("국민"), NH("농협"), SH("신한"), WR("우리"), IBK("기업");

    private final String name;


    Bank(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
