package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

public abstract class PedestrianWay extends Way{
    public PedestrianWay(ReaderWay way) {
        this.way = way;
    }

    @Override
    public boolean isPedestrianised() {
        return true;
    }
}
