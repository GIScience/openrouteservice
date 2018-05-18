package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderElement;

import java.io.InvalidObjectException;

public interface OSMFeatureFilter {
    boolean accept();
    ReaderElement prepareForProcessing();
    boolean isWayProcessingComplete();

    void assignFeatureForFiltering(ReaderElement feature) throws InvalidObjectException;
}
