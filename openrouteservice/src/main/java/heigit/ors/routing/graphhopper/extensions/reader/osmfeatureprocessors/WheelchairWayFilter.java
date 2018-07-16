package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderElement;
import com.graphhopper.reader.ReaderWay;

import java.io.InvalidObjectException;

public class WheelchairWayFilter extends OSMWayFilter {
    private OSMAttachedSidewalkProcessor osmAttachedSidewalkProcessor;
    private OSMPedestrianProcessor osmPedestrianProcessor;

    private Way osmWay;

    public WheelchairWayFilter() {
        super();
        osmAttachedSidewalkProcessor = new OSMAttachedSidewalkProcessor();
        osmPedestrianProcessor = new OSMPedestrianProcessor();

    }

    @Override
    public void assignFeatureForFiltering(ReaderElement element) throws InvalidObjectException {
        if(element instanceof ReaderWay) {
            ReaderWay way = (ReaderWay) element;

            if (osmAttachedSidewalkProcessor.hasSidewalkInfo(way)) {
                this.osmWay = new WheelchairSidewalkWay(way);
            } else if (osmPedestrianProcessor.isPedestrianisedWay(way)) {
                this.osmWay = new WheelchairSeparateWay(way);
            } else {
                this.osmWay = new NonPedestrianWay();
            }
        } else {
            throw new InvalidObjectException("Wheelchair Filtering can only be applied to ways");
        }
    }

    @Override
    public boolean accept() {
        return osmWay.isPedestrianised();
    }

    @Override
    public ReaderElement prepareForProcessing() {
        osmWay.prepare();

        return osmWay.getReaderWay();
    }

    @Override
    public boolean isWayProcessingComplete() {
        return osmWay.hasWayBeenFullyProcessed();
    }

}
