package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Created by lliu on 13/03/2017.
 */
public class GreenIndexGraphStorageBuilder extends AbstractGraphStorageBuilder {
    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        return null;
    }

    @Override
    public void processWay(OSMWay way) {

    }

    @Override
    public void processEdge(OSMWay way, EdgeIteratorState edge) {

    }

    @Override
    public String getName() {
        return null;
    }
}
