package com.shemb.storage.enums;

public enum Action {
    SAVE(1),
    ALLOW(2),
    DELETE(3);

    private final Integer intValue;

    Action(Integer intValue) {
        this.intValue = intValue;
    }

    public Integer getIntValue() {
        return intValue;
    }
}
