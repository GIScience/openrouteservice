package heigit.ors.api.responses.isochrones.GeoJSONIsochronesResponseObjects;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.isochrones.Isochrone;
import heigit.ors.isochrones.IsochroneMap;

import java.util.ArrayList;
import java.util.List;

public class GeoJSONIsochronesMap {
    private IsochroneMap isochroneMap;
    private Coordinate mapCenter;
    private int travellerID;
    private List<GeoJSONIsochrone> features = new ArrayList<>();

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
