package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

public abstract class Way {
    protected ReaderWay way;

    public ReaderWay getReaderWay() {
        return this.way;
    }

    /**
     * Is the way a pedestrianised way
     * @return
     */
    public boolean isPedestrianised() {
        return false;
    }

    /**
     * Determine if the way has been completely processed
     *
     * @return
     */
    public abstract boolean hasWayBeenFullyProcessed();

    /**
     * Prepare the way ready for processing, such as adding side tags
     */
    public void prepare() {}
}
