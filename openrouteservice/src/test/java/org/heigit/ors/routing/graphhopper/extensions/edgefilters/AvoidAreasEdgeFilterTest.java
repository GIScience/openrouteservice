package org.heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.DAType;
import com.graphhopper.storage.GHDirectory;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.storages.NoOpExtension;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AvoidAreasEdgeFilterTest {
    private PMap properties = new PMap();
    private final EncodingManager encoder = EncodingManager.create(new ORSDefaultFlagEncoderFactory().createFlagEncoder(FlagEncoderNames.CAR_ORS, properties));

    private final RouteSearchParameters _searchParams;
    private final GraphHopperStorage _graphStorage;

    public AvoidAreasEdgeFilterTest() {
        _graphStorage = new GraphHopperStorage(new GHDirectory("", DAType.RAM_STORE), encoder, false);
        _graphStorage.create(3);


        this._searchParams = new RouteSearchParameters();
    }

    @Test
    public void TestAvoidPolygons() {
        EdgeIteratorState iter1 = _graphStorage.edge(0, 1).setDistance(100);
        iter1.setWayGeometry(Helper.createPointList(0, 0, 10, 0));
        EdgeIteratorState iter2 = _graphStorage.edge(0, 2).setDistance(200);

        iter2.setWayGeometry(Helper.createPointList(0, 0, -10, 0));

        GeometryFactory gf = new GeometryFactory();

        Polygon poly = gf.createPolygon(new Coordinate[]{ new Coordinate(-1,5),
                new Coordinate(1,5),
                new Coordinate(1,6),
                new Coordinate(-1,5)});

        AvoidAreasEdgeFilter filter = new AvoidAreasEdgeFilter(new Polygon[] {poly});
        assertFalse(filter.accept(iter1));
        assertTrue(filter.accept(iter2));
    }
}
