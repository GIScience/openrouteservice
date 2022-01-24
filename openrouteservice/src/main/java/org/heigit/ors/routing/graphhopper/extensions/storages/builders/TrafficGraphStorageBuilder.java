package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import org.geotools.feature.SchemaException;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;

public class TrafficGraphStorageBuilder extends AbstractGraphStorageBuilder {
    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        return null;
    }

    @Override
    public void processWay(ReaderWay way) {

    }


    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {

    }

    @Override
    public String getName() {
        return null;
    }

    public void postProcess(ORSGraphHopper graphHopper) throws SchemaException {

    }
}
