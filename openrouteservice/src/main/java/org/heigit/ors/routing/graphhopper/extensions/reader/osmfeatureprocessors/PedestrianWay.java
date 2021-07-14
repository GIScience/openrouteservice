package org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

public abstract class PedestrianWay extends Way{
    protected PedestrianWay(ReaderWay way) {
        this.readerWay = way;
    }

    @Override
    public boolean isPedestrianised() {
        return true;
    }
}
