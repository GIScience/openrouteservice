package org.heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

import org.heigit.ors.routing.graphhopper.extensions.TrafficRelevantWayType;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;

import java.util.HashSet;


public class TrafficEdgeFilter implements EdgeFilter {
    private int hereFunctionalClass;
    private TrafficGraphStorage trafficGraphStorage;
    private HashSet<Integer> originalEdgeIds;

    public TrafficEdgeFilter(GraphStorage graphStorage) {
        this.trafficGraphStorage = GraphStorageUtils.getGraphExtension(graphStorage, TrafficGraphStorage.class);
        originalEdgeIds = new HashSet<>();
    }


    @Override
    public boolean accept(EdgeIteratorState edgeIteratorState) {
        int edgeId = EdgeIteratorStateHelper.getOriginalEdge(edgeIteratorState);
        short osmWayTypeValue = (short) this.trafficGraphStorage.getOrsRoadProperties(edgeId, TrafficGraphStorage.Property.ROAD_TYPE);
        short osmTrafficClassConverted = TrafficRelevantWayType.getHereTrafficClassFromOSMRoadType(osmWayTypeValue);
        // TODO RAD
        // TODO RAD
        if (!this.originalEdgeIds.isEmpty()) {
            return this.originalEdgeIds.contains(edgeId) && (osmTrafficClassConverted == hereFunctionalClass || osmTrafficClassConverted == TrafficRelevantWayType.UNCLASSIFIED);
        } else if (osmTrafficClassConverted == 0) {
            return false;
        } else {
            return osmTrafficClassConverted == hereFunctionalClass;
        }
    }

    public void setHereFunctionalClass(int hereFunctionalClass) {
        this.hereFunctionalClass = hereFunctionalClass;
    }

    public void setOriginalEdgeIds(HashSet<Integer> matchIds) {
        if (matchIds != null) {
            this.originalEdgeIds = matchIds;
        } else {
            this.originalEdgeIds = new HashSet<>();
        }
    }

    public int getHereFunctionalClass() {
        return this.hereFunctionalClass;
    }

    public void lowerFunctionalClass() {
        if (hereFunctionalClass < TrafficRelevantWayType.CLASS5) {
            // We don't want to decrease the functional class lower than 5.
            this.hereFunctionalClass += 1;
        } else if (hereFunctionalClass >= TrafficRelevantWayType.CLASS1LINK && hereFunctionalClass < TrafficRelevantWayType.CLASS4LINK) {
            this.hereFunctionalClass += 1;
        }
    }

    public void higherFunctionalClass() {
        if (hereFunctionalClass > TrafficRelevantWayType.CLASS1 && hereFunctionalClass <= TrafficRelevantWayType.CLASS5) {
            // We don't want to increase the functional class higher than CLASS1 and not lower than CLASS4 to not collide with non-links.
            this.hereFunctionalClass -= 1;
        } else if (hereFunctionalClass > TrafficRelevantWayType.CLASS1LINK && hereFunctionalClass <= TrafficRelevantWayType.CLASS4LINK) {
            this.hereFunctionalClass -= 1;
        }
    }

    public boolean hasOriginalEdgeIds() {
        return !this.originalEdgeIds.isEmpty();
    }
}
