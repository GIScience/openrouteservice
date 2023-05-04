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
package org.heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.ch.CHPreparationHandler;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.*;
import org.heigit.ors.routing.RoutingProfileCategory;
import org.heigit.ors.routing.graphhopper.extensions.GraphProcessContext;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperConfig;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.*;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters.Core;

/**
 * This class implements the Core Algo decorator and provides several helper methods related to core
 * preparation and its vehicle profiles.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 * @author Andrzej Oles
 */
public class CorePreparationHandler extends CHPreparationHandler {

    private GraphProcessContext processContext;

    public CorePreparationHandler() {
        super();
        PREPARE = Core.PREPARE;
        DISABLE = Core.DISABLE;
    }

    public void init(ORSGraphHopperConfig ghConfig) {
        setPreparationThreads(ghConfig.getInt(PREPARE + "threads", getPreparationThreads()));
        setCHProfiles(ghConfig.getCoreProfiles());
        pMap = ghConfig.asPMap();
    }

    @Override
    public void createPreparations(GraphHopperStorage ghStorage) {
        if (processContext==null)
            throw new IllegalStateException("Set processContext first!");
        super.createPreparations(ghStorage);
    }

    @Override
    protected PrepareContractionHierarchies createCHPreparation(GraphHopperStorage ghStorage, CHConfig chConfig) {
        EdgeFilter restrictionFilter = createCoreEdgeFilter(chConfig, ghStorage, processContext);
        PrepareContractionHierarchies pch = new PrepareCore(ghStorage, chConfig, restrictionFilter);
        pch.setParams(pMap);
        return pch;
    }

    public CorePreparationHandler setProcessContext(GraphProcessContext processContext) {
        this.processContext = processContext;
        return this;
    }

    private EdgeFilter createCoreEdgeFilter(CHConfig chProfile, GraphHopperStorage gs, GraphProcessContext processContext) {
        EncodingManager encodingManager = gs.getEncodingManager();

        int routingProfileCategory = RoutingProfileCategory.getFromEncoder(encodingManager);

        /* Initialize edge filter sequence */
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();

        /* Heavy vehicle filter */
        if (encodingManager.hasEncoder(FlagEncoderNames.HEAVYVEHICLE)) {
            edgeFilterSequence.add(new HeavyVehicleCoreEdgeFilter(gs));
        }

        /* Avoid features */
        if ((routingProfileCategory & (RoutingProfileCategory.DRIVING | RoutingProfileCategory.CYCLING | RoutingProfileCategory.WALKING | RoutingProfileCategory.WHEELCHAIR)) != 0) {
            edgeFilterSequence.add(new AvoidFeaturesCoreEdgeFilter(gs, routingProfileCategory));
        }

        /* Avoid borders */
        if ((routingProfileCategory & (RoutingProfileCategory.DRIVING | RoutingProfileCategory.CYCLING)) != 0) {
            edgeFilterSequence.add(new AvoidBordersCoreEdgeFilter(gs));
        }

        if (routingProfileCategory == RoutingProfileCategory.WHEELCHAIR) {
            edgeFilterSequence.add(new WheelchairCoreEdgeFilter(gs));
        }

        /* Maximum speed & turn restrictions */
        if ((routingProfileCategory & RoutingProfileCategory.DRIVING) !=0) {
            String[] encoders = {FlagEncoderNames.CAR_ORS, FlagEncoderNames.HEAVYVEHICLE};
            for (String encoderName: encoders) {
                if (encodingManager.hasEncoder(encoderName)) {
                    FlagEncoder flagEncoder = encodingManager.getEncoder(encoderName);
                    edgeFilterSequence.add(new MaximumSpeedCoreEdgeFilter(flagEncoder, processContext.getMaximumSpeedLowerBound()));
                    if (chProfile.isEdgeBased() && flagEncoder.supportsTurnCosts())
                        edgeFilterSequence.add(new TurnRestrictionsCoreEdgeFilter(flagEncoder, gs));
                    break;
                }
            }
        }

        /* Conditional edges */
        if (TimeDependentCoreEdgeFilter.hasConditionals(encodingManager)) {
            edgeFilterSequence.add(new TimeDependentCoreEdgeFilter(gs));
        }

        if (TrafficSpeedCoreEdgeFilter.hasTrafficGraphStorage(gs)) {
            edgeFilterSequence.add(new TrafficSpeedCoreEdgeFilter(gs));
        }

        return edgeFilterSequence;
    }

}
