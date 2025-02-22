package org.heigit.ors.benchmark;

public enum RangeType {
    TIME("time"),
    DISTANCE("distance");

    private final String value;

    RangeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

