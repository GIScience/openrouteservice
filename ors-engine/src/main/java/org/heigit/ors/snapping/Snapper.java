package org.heigit.ors.snapping;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.util.DefaultSnapFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import org.heigit.ors.matrix.ResolvedLocation;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.Coordinate;

public class Snapper {
    LocationIndex locationIndex;
    EdgeFilter edgeFilter;
    private int profileType;

    public Snapper(RoutingProfile rp, int weightingMethod) {
        profileType = rp.profileType();
        this.locationIndex = rp.getGraphhopper().getLocationIndex();
        this.edgeFilter = snappingEdgeFilter(rp.getGraphhopper(), weightingMethod);
    }

    public Snap snapToGraph(double lon, double lat) {
        return locationIndex.findClosest(lat, lon, edgeFilter);
    }

    public ResolvedLocation resolveLocation(Coordinate coordinate) {
        Snap snap = snapToGraph(coordinate.y, coordinate.x);
        Coordinate snappedCoordinates = new Coordinate(snap.getSnappedPoint().getLon(), snap.getSnappedPoint().getLat());
        return new ResolvedLocation(snappedCoordinates, snap.getClosestEdge().getName(), snap.getQueryDistance());
    }

    private EdgeFilter snappingEdgeFilter(ORSGraphHopper gh, int weightingMethod) {
        String encoderName = RoutingProfileType.getEncoderName(profileType);
        PMap hintsMap = new PMap();
        ProfileTools.setWeightingMethod(hintsMap, weightingMethod, profileType, false);
        ProfileTools.setWeighting(hintsMap, weightingMethod, profileType, false);
// TODO: which effectiveWeighting used at different places is correct?
//        String effectiveWeighting = hintsMap.getString("weightingMethod", "");
        String effectiveWeighting = WeightingMethod.getName(weightingMethod);
        String localProfileName = ProfileTools.makeProfileName(encoderName, effectiveWeighting, false);
        Weighting weighting = new ORSWeightingFactory(gh.getGraphHopperStorage(), gh.getEncodingManager())
                .createWeighting(gh.getProfile(localProfileName), hintsMap, false);
        BooleanEncodedValue profileSubnetwork = gh.getEncodingManager().getBooleanEncodedValue(Subnetwork.key(localProfileName));
        return new DefaultSnapFilter(weighting, profileSubnetwork);
    }
}
