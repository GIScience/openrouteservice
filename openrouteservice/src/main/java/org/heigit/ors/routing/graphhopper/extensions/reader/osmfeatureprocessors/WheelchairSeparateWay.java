package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

public class WheelchairSeparateWay extends PedestrianWay {
    boolean hasBeenProcessed = false;

    public WheelchairSeparateWay(ReaderWay way) {
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
