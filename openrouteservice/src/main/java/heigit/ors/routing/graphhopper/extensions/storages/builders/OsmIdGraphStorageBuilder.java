package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.graphhopper.extensions.storages.OsmIdGraphStorage;

public class OsmIdGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private OsmIdGraphStorage osmIdGraphStorage;

    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (osmIdGraphStorage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        osmIdGraphStorage = new OsmIdGraphStorage();

        return osmIdGraphStorage;
    }

    public void processWay(ReaderWay way) {
    }

    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        osmIdGraphStorage.setEdgeValue(edge.getEdge(), way.getId());
    }

    @Override
    public String getName() {
        return "OsmId";
    }
}
