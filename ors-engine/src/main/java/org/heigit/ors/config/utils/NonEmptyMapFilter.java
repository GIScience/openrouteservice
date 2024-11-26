package org.heigit.ors.config.utils;


import java.util.Map;
import java.util.Objects;

public class NonEmptyMapFilter {

    @Override
    public boolean equals(Object other) {
        if (other == null) return true;
        if (other instanceof Map) {
            return ((Map<?, ?>) other).isEmpty();
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.getClass());
    }
}
