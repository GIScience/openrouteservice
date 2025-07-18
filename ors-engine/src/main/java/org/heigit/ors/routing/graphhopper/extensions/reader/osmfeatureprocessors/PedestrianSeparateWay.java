package org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

public class PedestrianSeparateWay extends PedestrianWay {
    boolean hasBeenProcessed = false;

    public PedestrianSeparateWay(ReaderWay way) {
        super(way);
    }

    @Override
    public boolean hasWayBeenFullyProcessed() {
        return hasBeenProcessed;
    }

    @Override
    public void prepare() {
        hasBeenProcessed = true;
    }
}
