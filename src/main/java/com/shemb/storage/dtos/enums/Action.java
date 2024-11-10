package com.shemb.storage.dtos.enums;

import java.util.Arrays;
import java.util.Objects;

public enum Action {
    UNDEFINED(0),
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

    public static Action byIntValue(Integer value) {
        return Arrays.stream(values()).filter(v -> Objects.equals(v.getIntValue(), value)).findFirst().orElse(UNDEFINED);
    }
}
