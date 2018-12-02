package heigit.ors.api.responses.isochrones.GeoJSONIsochronesResponseObjects;

import heigit.ors.api.requests.isochrones.IsochronesRequest;
import heigit.ors.isochrones.Isochrone;
import heigit.ors.isochrones.IsochroneMap;

import java.util.ArrayList;
import java.util.List;


public class GeoJSONIndividualIsochronesMapResponse {
    public IsochroneMap isochroneMap;
    private IsochronesRequest request;
    private List features = new ArrayList<>();


    public GeoJSONIndividualIsochronesMapResponse(IsochroneMap isoMap, IsochronesRequest request) {
        this.isochroneMap = isoMap;
        this.request = request;
    }


    public List<GeoJSONIndividualIsochroneResponse> calculateIsochrones() {
        for (Isochrone isochrone : isochroneMap.getIsochrones()) {
            this.features.add(new GeoJSONIndividualIsochroneResponse(isochrone, request, isochroneMap.getCenter(), isochroneMap.getTravellerId()));
        }
        return this.features;
    }
}
