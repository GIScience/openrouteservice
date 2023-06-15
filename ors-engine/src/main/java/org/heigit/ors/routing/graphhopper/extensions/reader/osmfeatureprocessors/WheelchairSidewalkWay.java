package org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;



public class WheelchairSidewalkWay extends PedestrianWay {
    private final OSMAttachedSidewalkProcessor sidewalkProcessor;
    private final OSMAttachedSidewalkProcessor.Side side;
    private OSMAttachedSidewalkProcessor.Side lastPrepared = OSMAttachedSidewalkProcessor.Side.NONE;

    public WheelchairSidewalkWay(ReaderWay way) {
        super(way);

        sidewalkProcessor = new OSMAttachedSidewalkProcessor();

        this.side = sidewalkProcessor.identifySidesWhereSidewalkIsPresent(this.readerWay);
    }

    @Override
    public boolean hasWayBeenFullyProcessed() {
        if(side == OSMAttachedSidewalkProcessor.Side.BOTH && lastPrepared == OSMAttachedSidewalkProcessor.Side.RIGHT) {
            return true;
        }

        return side == lastPrepared;
    }

    @Override
    public void prepare() {
        // Find out if anything has been done yet

        if(side == OSMAttachedSidewalkProcessor.Side.BOTH && lastPrepared == OSMAttachedSidewalkProcessor.Side.RIGHT) {
            // The right sidewalk is the last to be processed when we are looking at both sides
            lastPrepared = OSMAttachedSidewalkProcessor.Side.BOTH;
        }

        if(lastPrepared != side) {
            this.readerWay = sidewalkProcessor.attachSidewalkTag(this.readerWay, side);
            lastPrepared = sidewalkProcessor.getPreparedSide(this.readerWay);
        }
    }
}
