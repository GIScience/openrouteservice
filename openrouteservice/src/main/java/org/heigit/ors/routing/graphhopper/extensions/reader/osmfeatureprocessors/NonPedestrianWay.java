package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

public class NonPedestrianWay extends Way {
    @Override
    public boolean hasWayBeenFullyProcessed() {
        return true;
    }

}
