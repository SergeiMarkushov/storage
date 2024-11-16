package com.shemb.storage.dtos.enums;

import java.util.Arrays;
import java.util.Objects;

public enum FileCategory {
    UNDEFINED(0),
    DOCUMENT(1),
    MEDIA(2);

    private final Integer intValue;

    FileCategory(Integer intValue) {
        this.intValue = intValue;
    }

    public static FileCategory byIntValue(Integer value) {
        return Arrays.stream(values()).filter(v -> Objects.equals(v.getIntValue(), value)).findFirst().orElse(UNDEFINED);
    }

    public Integer getIntValue() {
        return intValue;
    }
}
