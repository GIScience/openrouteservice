package org.heigit.ors.snapping;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.PMap;
import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.matrix.MatrixSearchContext;
import org.heigit.ors.matrix.MatrixSearchContextBuilder;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.heigit.ors.util.ProfileTools;
import org.heigit.ors.routing.RoutingProfile;
import org.locationtech.jts.geom.Coordinate;

public class SnappingRequest extends ServiceRequest {
    private final int profileType;
    private final Coordinate[] locations;
    private final double maximumSearchRadius;
    private int maximumLocations;

    public SnappingRequest(int profileType, Coordinate[] locations, double maximumSearchRadius) {
        this.profileType = profileType;
        this.locations = locations;
        this.maximumSearchRadius = maximumSearchRadius;
    }

    public int getProfileType() {
        return profileType;
    }

    public Coordinate[] getLocations() {
        return locations;
    }

    public double getMaximumSearchRadius() {
        return maximumSearchRadius;
    }

    public void setMaximumLocations(int maximumLocations) {
        this.maximumLocations = maximumLocations;
    }

    public int getMaximumLocations() {
        return maximumLocations;
    }

    public SnappingResult computeResult(RoutingProfile rp) throws Exception {
        GraphHopper gh = rp.getGraphhopper();
        String encoderName = RoutingProfileType.getEncoderName(getProfileType());
        FlagEncoder flagEncoder = gh.getEncodingManager().getEncoder(encoderName);
        PMap hintsMap = new PMap();
        int weightingMethod = WeightingMethod.RECOMMENDED; // Only needed to create the profile string
        ProfileTools.setWeightingMethod(hintsMap, weightingMethod, getProfileType(), false);
        ProfileTools.setWeighting(hintsMap, weightingMethod, getProfileType(), false);
        String profileName = ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting", ""), false);
        GraphHopperStorage ghStorage = gh.getGraphHopperStorage();
        String graphDate = ghStorage.getProperties().get("datareader.import.date");

        // TODO: replace usage of matrix search context by snapping-specific class
        MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(ghStorage, gh.getLocationIndex(), AccessFilter.allEdges(flagEncoder.getAccessEnc()), true);
        Weighting weighting = new ORSWeightingFactory(ghStorage, gh.getEncodingManager()).createWeighting(gh.getProfile(profileName), hintsMap, false);
        MatrixSearchContext mtxSearchCntx = builder.create(ghStorage.getBaseGraph(), null, weighting, profileName, getLocations(), getLocations(), getMaximumSearchRadius());
        return new SnappingResult(mtxSearchCntx.getSources().getLocations(), graphDate);
    }
}
