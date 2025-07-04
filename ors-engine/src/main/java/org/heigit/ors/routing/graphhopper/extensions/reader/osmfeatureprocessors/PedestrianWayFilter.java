package org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderElement;
import com.graphhopper.reader.ReaderWay;

import java.io.InvalidObjectException;

public class PedestrianWayFilter implements OSMFeatureFilter {
    private final OSMAttachedSidewalkProcessor osmAttachedSidewalkProcessor;

    private Way osmWay;

    public PedestrianWayFilter() {
        super();
        osmAttachedSidewalkProcessor = new OSMAttachedSidewalkProcessor();
    }

    @Override
    public void assignFeatureForFiltering(ReaderElement element) throws InvalidObjectException {
        if (element instanceof ReaderWay way) {

            if (osmAttachedSidewalkProcessor.hasSidewalkInfo(way)) {
                this.osmWay = new PedestrianSidewalkWay(way);
            } else {
                this.osmWay = new PedestrianSeparateWay(way);
            }
        } else {
            throw new InvalidObjectException("Pedestrian filtering can only be applied to ways");
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
