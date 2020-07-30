package org.heigit.ors.routing.pathprocessors;

import org.heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;

import java.util.ArrayList;
import java.util.List;

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

    public boolean isSameCountry(List<Integer> edgeIds){
        List<Short> countryIds = new ArrayList<>();
        for(int edgeId : edgeIds) {
            countryIds.add(storage.getEdgeValue(edgeId, BordersGraphStorage.Property.START));
            countryIds.add(storage.getEdgeValue(edgeId, BordersGraphStorage.Property.END));
        }

        return countryIds.isEmpty() || countryIds.stream().allMatch(countryIds.get(0)::equals);

    }
}
