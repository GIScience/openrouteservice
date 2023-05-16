package org.heigit.ors.routing.pathprocessors;

import org.heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;

import java.util.List;

public class BordersExtractor {
    public enum Avoid { CONTROLLED, NONE, ALL }
    private final BordersGraphStorage storage;
    private final int[] avoidCountries;

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

    /**
     * Check whether the start and end nodes of a list of edges are in the same country.
     * @param edgeIds Edges that the country should be checked for
     * @return true if at least one node is in the same country
     */
    public boolean isSameCountry(List<Integer> edgeIds){
        if(edgeIds.isEmpty())
            return true;

        short country0 = storage.getEdgeValue(edgeIds.get(0), BordersGraphStorage.Property.START);
        short country1 = storage.getEdgeValue(edgeIds.get(0), BordersGraphStorage.Property.END);
        for(int edgeId : edgeIds) {
            short country2 = storage.getEdgeValue(edgeId, BordersGraphStorage.Property.START);
            short country3 = storage.getEdgeValue(edgeId, BordersGraphStorage.Property.END);
            if(country0 != country2
            && country0 != country3
            && country1 != country2
            && country1 != country3)
                return false;
        }
        return true;
    }
}
