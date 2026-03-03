package com.spribe.enums;

public enum Role {
    SUPERVISOR("supervisor"),
    ADMIN("admin"),
    USER("user");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
