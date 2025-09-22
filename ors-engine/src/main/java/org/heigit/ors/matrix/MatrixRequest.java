/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.matrix;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.util.PMap;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.exceptions.MaxVisitedNodesExceededException;
import org.heigit.ors.exceptions.PointNotFoundException;
import org.heigit.ors.matrix.algorithms.core.CoreMatrixAlgorithm;
import org.heigit.ors.matrix.algorithms.dijkstra.DijkstraMatrixAlgorithm;
import org.heigit.ors.matrix.algorithms.rphast.RPHASTMatrixAlgorithm;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.routing.graphhopper.extensions.ORSEdgeFilterFactory;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperStorage;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.heigit.ors.util.ProfileTools;
import org.heigit.ors.util.TemporaryUtilShelter;
import org.locationtech.jts.geom.Coordinate;

public class MatrixRequest extends ServiceRequest {
    private String profileName;
    private int profileType = -1;
    private Coordinate[] sources;
    private Coordinate[] destinations;
    private int metrics = MatrixMetricsType.DURATION;
    private int weightingMethod;
    private DistanceUnit units = DistanceUnit.METERS;
    private boolean resolveLocations = false;
    private boolean flexibleMode = false;
    private String algorithm;
    private MatrixSearchParameters searchParameters;
    private double maximumSearchRadius;
    private int maximumVisitedNodes;
    private boolean hasInfiniteUTurnCosts;

    public MatrixRequest(double maximumSearchRadius, int maximumVisitedNodes, double uTurnCosts) {
        this.maximumSearchRadius = maximumSearchRadius;
        this.maximumVisitedNodes = maximumVisitedNodes;
        this.hasInfiniteUTurnCosts = uTurnCosts == Weighting.INFINITE_U_TURN_COSTS;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Coordinate[] getSources() {
        return sources;
    }

    public void setSources(Coordinate[] sources) {
        this.sources = sources;
    }

    public Coordinate[] getDestinations() {
        return destinations;
    }

    public void setDestinations(Coordinate[] destinations) {
        this.destinations = destinations;
    }

    public int getMetrics() {
        return metrics;
    }

    public void setMetrics(int metrics) {
        this.metrics = metrics;
    }

    public boolean getResolveLocations() {
        return resolveLocations;
    }

    public void setResolveLocations(boolean resolveLocations) {
        this.resolveLocations = resolveLocations;
    }

    public int getProfileType() {
        return profileType;
    }

    public void setProfileType(int profile) {
        profileType = profile;
    }

    public DistanceUnit getUnits() {
        return units;
    }

    public void setUnits(DistanceUnit units) {
        this.units = units;
    }

    public int getTotalNumberOfLocations() {
        return destinations.length * sources.length;
    }

    public int getWeightingMethod() {
        return weightingMethod;
    }

    /**
     * Return weightingMethod or sane default if method is UNKNOWN.
     */
    public int getWeightingMethodOrDefault() {
        return weightingMethod == WeightingMethod.UNKNOWN ? WeightingMethod.RECOMMENDED : weightingMethod;
    }

    public void setWeightingMethod(int weightingMethod) {
        this.weightingMethod = weightingMethod;
    }

    public boolean getFlexibleMode() {
        return flexibleMode;
    }

    public void setFlexibleMode(boolean flexibleMode) {
        this.flexibleMode = flexibleMode;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public double getMaximumSearchRadius() {
        return maximumSearchRadius;
    }

    public void setMaximumSearchRadius(double radius) {
        this.maximumSearchRadius = radius;
    }

    public int getMaximumVisitedNodes() {
        return maximumVisitedNodes;
    }

    public void setMaximumVisitedNodes(int maximumVisitedNodes) {
        this.maximumVisitedNodes = maximumVisitedNodes;
    }

    public void setSearchParameters(MatrixSearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    public MatrixSearchParameters getSearchParameters() {
        return searchParameters;
    }

    public boolean isValid() {
        return !(sources == null && destinations == null);
    }

    public boolean hasInfiniteUTurnCosts() {
        return hasInfiniteUTurnCosts;
    }

    public void setInfiniteUTurnCosts(boolean hasInfiniteUTurnCosts) {
        this.hasInfiniteUTurnCosts = hasInfiniteUTurnCosts;
    }

    /**
     * Compute a NxM matrix from a request using any of the three available approaches.
     * For performance reasons, RPHAST is preferred over CoreMatrix, which is preferred over DijkstraMatrix, depending on request conditions.
     *
     * @param routingProfile@return A MatrixResult object, possibly with both time and distance values for all combinations of N and M input locations
     * @throws Exception
     */
    public MatrixResult computeMatrix(RoutingProfile routingProfile) throws Exception {
        GraphHopper gh = routingProfile.getGraphhopper();
        String encoderName = RoutingProfileType.getEncoderName(getProfileType());
        FlagEncoder flagEncoder = gh.getEncodingManager().getEncoder(encoderName);
        PMap hintsMap = new PMap();
        boolean hasTurnCosts = Boolean.TRUE.equals(routingProfile.getProfileProperties().getBuild().getEncoderOptions().getTurnCosts());
        ProfileTools.setWeightingMethod(hintsMap, getWeightingMethodOrDefault(), getProfileType(), false);
        ProfileTools.setWeighting(hintsMap, getWeightingMethodOrDefault(), getProfileType(), false);
        String profileName = ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting", ""), hasTurnCosts);

        //TODO Refactoring : probably remove MatrixAlgorithmFactory alltogether as the checks for algorithm choice have to be performed here again. Or combine in a single check nicely
        try {
            // RPHAST
            if (!getFlexibleMode() && gh.getCHPreparationHandler().isEnabled() && routingProfile.hasCHProfile(profileName)) {
                return computeRPHASTMatrix(gh, flagEncoder, profileName);
            }
            // Core
            else if (getSearchParameters().getDynamicSpeeds() && routingProfile.getGraphhopper().isCoreAvailable(profileName)) {
                return computeCoreMatrix(gh, flagEncoder, hintsMap, profileName, routingProfile);
            }
            // Dijkstra
            else {
                // use CHProfileName (w/o turn costs) since Dijkstra is node-based so turn restrictions are not used.
                return computeDijkstraMatrix(gh, flagEncoder, hintsMap, profileName.replace("_with_turn_costs", ""));
            }
        } catch (PointNotFoundException e) {
            throw e;
        } catch (MaxVisitedNodesExceededException e) {
            throw new InternalServerException(MatrixErrorCodes.MAX_VISITED_NODES_EXCEEDED, "Unable to compute a distance/duration matrix: " + e.getMessage());
        } catch (Exception ex) {
            throw new InternalServerException(MatrixErrorCodes.UNKNOWN, "Unable to compute a distance/duration matrix: " + ex.getMessage());
        }
    }

    /**
     * Compute a matrix based on a contraction hierarchies graph using the RPHAST algorithm. This is fast, but inflexible.
     *
     * @param gh
     * @param flagEncoder
     * @param profileName
     * @return
     * @throws Exception
     */
    private MatrixResult computeRPHASTMatrix(GraphHopper gh, FlagEncoder flagEncoder, String profileName) throws Exception {
        RoutingCHGraph routingCHGraph = gh.getGraphHopperStorage().getRoutingCHGraph(profileName);
        MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getGraphHopperStorage(), gh.getLocationIndex(), AccessFilter.allEdges(flagEncoder.getAccessEnc()), getResolveLocations());
        MatrixSearchContext mtxSearchCntx = builder.create(routingCHGraph.getBaseGraph(), routingCHGraph, routingCHGraph.getWeighting(), profileName, getSources(), getDestinations(), getMaximumSearchRadius());

        RPHASTMatrixAlgorithm algo = new RPHASTMatrixAlgorithm();
        algo.init(this, gh, mtxSearchCntx.getRoutingCHGraph(), flagEncoder, routingCHGraph.getWeighting());
        return algo.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), getMetrics());
    }

    /**
     * Compute a matrix based on the normal graph. Slow, but highly flexible in terms of request parameters.
     *
     * @param gh
     * @param flagEncoder
     * @param hintsMap
     * @param profileName
     * @return
     * @throws Exception
     */
    private MatrixResult computeDijkstraMatrix(GraphHopper gh, FlagEncoder flagEncoder, PMap hintsMap, String profileName) throws Exception {
        Graph graph = gh.getGraphHopperStorage().getBaseGraph();
        Weighting weighting = new ORSWeightingFactory(gh.getGraphHopperStorage(), gh.getEncodingManager()).createWeighting(gh.getProfile(profileName), hintsMap, false);
        MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getGraphHopperStorage(), gh.getLocationIndex(), AccessFilter.allEdges(flagEncoder.getAccessEnc()), getResolveLocations());
        MatrixSearchContext mtxSearchCntx = builder.create(graph, null, weighting, profileName, getSources(), getDestinations(), getMaximumSearchRadius());

        DijkstraMatrixAlgorithm algo = new DijkstraMatrixAlgorithm();
        algo.init(this, gh, mtxSearchCntx.getGraph(), flagEncoder, weighting);
        return algo.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), getMetrics());
    }

    /**
     * Compute a matrix based on a core contracted graph, which is slower than RPHAST, but offers all the flexibility of the core
     *
     * @param gh
     * @param flagEncoder
     * @param hintsMap
     * @param profileName
     * @param routingProfile
     * @return
     */
    private MatrixResult computeCoreMatrix(GraphHopper gh, FlagEncoder flagEncoder, PMap hintsMap, String profileName, RoutingProfile routingProfile) throws Exception {
        Weighting weighting = new ORSWeightingFactory(gh.getGraphHopperStorage(), gh.getEncodingManager()).createWeighting(gh.getProfile(profileName), hintsMap, false);
        RoutingCHGraph graph = ((ORSGraphHopperStorage) gh.getGraphHopperStorage()).getCoreGraph(profileName);
        RouteSearchContext searchCntx = TemporaryUtilShelter.createSearchContext(getSearchParameters(), routingProfile);
        PMap additionalHints = searchCntx.getProperties();
        EdgeFilter edgeFilter = new ORSEdgeFilterFactory().createEdgeFilter(additionalHints, flagEncoder, gh.getGraphHopperStorage());

        MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(gh.getGraphHopperStorage(), gh.getLocationIndex(), edgeFilter, getResolveLocations());
        MatrixSearchContext mtxSearchCntx = builder.create(graph.getBaseGraph(), graph, weighting, profileName, getSources(), getDestinations(), getMaximumSearchRadius());

        CoreMatrixAlgorithm algo = new CoreMatrixAlgorithm();
        algo.init(this, gh, mtxSearchCntx.getRoutingCHGraph(), flagEncoder, weighting, edgeFilter);
        return algo.compute(mtxSearchCntx.getSources(), mtxSearchCntx.getDestinations(), getMetrics());
    }
}
