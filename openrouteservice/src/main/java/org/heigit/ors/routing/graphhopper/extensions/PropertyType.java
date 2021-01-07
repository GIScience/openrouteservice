package org.heigit.ors.routing.graphhopper.extensions;

public interface PropertyType {
    public static PropertyType getFromString(String property) {
        return null;
    }
    public int getOrdinal();
}
