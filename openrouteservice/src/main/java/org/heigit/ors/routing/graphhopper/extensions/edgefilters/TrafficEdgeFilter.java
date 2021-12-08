package org.heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

import org.heigit.ors.routing.graphhopper.extensions.TrafficRelevantWayType;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.HereTrafficGraphStorage;


public class TrafficEdgeFilter implements EdgeFilter {
    private int hereFunctionalClass;
    private HereTrafficGraphStorage hereTrafficGraphStorage;

    public TrafficEdgeFilter(GraphStorage graphStorage) {
        this.hereTrafficGraphStorage = GraphStorageUtils.getGraphExtension(graphStorage, HereTrafficGraphStorage.class);
    }


    @Override
    public boolean accept(EdgeIteratorState edgeIteratorState) {
        int edgeId = EdgeIteratorStateHelper.getOriginalEdge(edgeIteratorState);
        short osmWayTypeValue = (short) this.hereTrafficGraphStorage.getOrsRoadProperties(edgeId, HereTrafficGraphStorage.Property.ROAD_TYPE);
        return osmWayTypeValue == hereFunctionalClass;
    }

    public void setHereFunctionalClass(int hereFunctionalClass) {
        this.hereFunctionalClass = hereFunctionalClass;
    }

    public int getHereFunctionalClass() {
        return this.hereFunctionalClass;
    }

    public void lowerFunctionalClass() {
        if (hereFunctionalClass < TrafficRelevantWayType.CLASS4) {
            // We don't want to decrease the functional class lower than 4.
            this.hereFunctionalClass += 1;
        } else if (hereFunctionalClass >= TrafficRelevantWayType.CLASS1LINK && hereFunctionalClass < TrafficRelevantWayType.CLASS4LINK) {
            this.hereFunctionalClass += 1;
        }
    }

    public void higherFunctionalClass() {
        if (hereFunctionalClass > TrafficRelevantWayType.CLASS1 && hereFunctionalClass <= TrafficRelevantWayType.CLASS4) {
            // We don't want to increase the functional class higher than CLASS1 and not lower than CLASS4 to not collide with non-links.
            this.hereFunctionalClass -= 1;
        } else if (hereFunctionalClass == TrafficRelevantWayType.CLASS5) {
            // Go directly to class 4. Skip unclassified when upgrading.
            this.hereFunctionalClass -= 2;
        } else if (hereFunctionalClass > TrafficRelevantWayType.CLASS1LINK && hereFunctionalClass <= TrafficRelevantWayType.CLASS4LINK) {
            this.hereFunctionalClass -= 1;
        }
    }
}
