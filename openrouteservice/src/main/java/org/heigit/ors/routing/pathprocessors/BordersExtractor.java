package org.heigit.ors.routing.pathprocessors;

import org.heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;


public class BordersExtractor {
    public enum Avoid { CONTROLLED, NONE, ALL }
    private BordersGraphStorage storage;
    private int[] avoidCountries;

    public BordersExtractor(BordersGraphStorage storage, int[] avoidCountries) {
        this.storage = storage;
        this.avoidCountries = avoidCountries;
    }

    public int getValue(int edgeId) {
        // Get the type of border
        return storage.getEdgeValue(edgeId, BordersGraphStorage.Property.TYPE);
    }

    public boolean isBorder(int edgeId) {
        int type = storage.getEdgeValue(edgeId, BordersGraphStorage.Property.TYPE);
        return (type == BordersGraphStorage.OPEN_BORDER || type == BordersGraphStorage.CONTROLLED_BORDER);
    }

    public boolean isControlledBorder(int edgeId) {
        return storage.getEdgeValue(edgeId, BordersGraphStorage.Property.TYPE) == BordersGraphStorage.CONTROLLED_BORDER;
    }

    public boolean isOpenBorder(int edgeId) {
        return storage.getEdgeValue(edgeId, BordersGraphStorage.Property.TYPE) == BordersGraphStorage.OPEN_BORDER;
    }

    public boolean restrictedCountry(int edgeId) {
        int startCountry = storage.getEdgeValue(edgeId, BordersGraphStorage.Property.START);
        int endCountry = storage.getEdgeValue(edgeId, BordersGraphStorage.Property.END);

        for(int i = 0; i< avoidCountries.length; i++) {
            if(startCountry == avoidCountries[i] || endCountry == avoidCountries[i] ) {
                return true;
            }
        }
        return false;
    }
}
