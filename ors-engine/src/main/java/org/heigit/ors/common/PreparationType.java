package org.heigit.ors.common;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PreparationType {
    FOLDER("FOLDER"),
    ARCHIVE("ARCHIVE");

    private final String type;

    PreparationType(String type) {
        this.type = type;
    }

    @JsonValue
    public String getType() {
        return type;
    }
}
