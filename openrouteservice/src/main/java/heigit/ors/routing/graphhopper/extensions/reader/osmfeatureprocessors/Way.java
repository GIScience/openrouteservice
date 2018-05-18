package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

public abstract class Way {
    protected ReaderWay way;

    public ReaderWay getReaderWay() {
        return this.way;
    }

    public boolean isPedestrianised() {
        return false;
    }

    public abstract boolean hasWayBeenFullyProcessed();

    public void prepare() {}


}
