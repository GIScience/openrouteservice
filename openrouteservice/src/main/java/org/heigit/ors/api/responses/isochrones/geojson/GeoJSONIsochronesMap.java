package org.heigit.ors.api.responses.isochrones.geojson;

import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.isochrones.Isochrone;
import org.heigit.ors.isochrones.IsochroneMap;

import java.util.ArrayList;
import java.util.List;

public class GeoJSONIsochronesMap {
    private final IsochroneMap isochroneMap;
    private final Coordinate mapCenter;
    private final int travellerID;
    private final List<GeoJSONIsochrone> features = new ArrayList<>();

    GeoJSONIsochronesMap(IsochroneMap isoMap) {
        this.isochroneMap = isoMap;
        this.mapCenter = isoMap.getCenter();
        this.travellerID = isoMap.getTravellerId();
    }


    List<GeoJSONIsochrone> buildGeoJSONIsochrones() {
        for (Isochrone isochrone : isochroneMap.getIsochrones()) {
            features.add(new GeoJSONIsochrone(isochrone, mapCenter, travellerID));
        }
        return features;
    }
}
