package org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderElement;

import java.io.InvalidObjectException;

public interface OSMFeatureFilter {

    /**
     * Assign a feature to this filter for processing
     *
     * @param feature
     * @throws InvalidObjectException
     */
    void assignFeatureForFiltering(ReaderElement feature) throws InvalidObjectException;

    /**
     * Should the element that has been assigned to this filter be accepted into the graph
     * @return
     */
    boolean accept();

    /**
     * Prepare the element that has been assigned to the filter for processing
     * @return
     */
    ReaderElement prepareForProcessing();

    /**
     * Has processing of feature on this filter been completed.
     *
     * @return
     */
    boolean isWayProcessingComplete();

}
