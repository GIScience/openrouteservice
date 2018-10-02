package heigit.ors.api.responses.routing.BoundingBox;

import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.api.requests.routing.CoordinateListWrapper;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxFactory;
import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXBounds;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSON3DBoundingBox;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONBoundingBox;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BoundingBoxFactoryTest {
    @Test
    public void testCorrectTypeCreated() throws Exception {
        /*List<Double[]> coords = new ArrayList<>();
        coords.add(new Double[] {24.5,39.2});
        coords.add(new Double[] {27.4,38.6});
        List<Double> coord1 = new ArrayList<>();
        coord1.add(24.5);
        coord1.add(39.2);
        coords.add(coord1);
        List<Double> coord2 = new ArrayList<>();
        coord2.add(27.4);
        coord2.add(38.6);*/

        Double[][] coords = new Double[2][2];
        coords[0] = new Double[] {24.5,39.2};
        coords[1] = new Double[] {27.4,38.6};

        RouteRequest request = new RouteRequest(coords);

        BBox bbox = new BBox(1,2,3,4,5,6);

        request.setReturnElevationForPoints(true);
        request.setResponseType(APIRoutingEnums.RouteResponseType.JSON);
        BoundingBox boundingBox = BoundingBoxFactory.constructBoundingBox(bbox, request);
        Assert.assertTrue(boundingBox instanceof JSON3DBoundingBox);

        request.setReturnElevationForPoints(false);
        boundingBox = BoundingBoxFactory.constructBoundingBox(bbox, request);
        Assert.assertTrue(boundingBox instanceof JSONBoundingBox);

        request.setReturnElevationForPoints(true);
        request.setResponseType(APIRoutingEnums.RouteResponseType.GEOJSON);
        boundingBox = BoundingBoxFactory.constructBoundingBox(bbox, request);
        Assert.assertTrue(boundingBox instanceof JSON3DBoundingBox);

        request.setReturnElevationForPoints(false);
        boundingBox = BoundingBoxFactory.constructBoundingBox(bbox, request);
        Assert.assertTrue(boundingBox instanceof JSONBoundingBox);

        request.setResponseType(APIRoutingEnums.RouteResponseType.GPX);
        boundingBox = BoundingBoxFactory.constructBoundingBox(bbox, request);
        Assert.assertTrue(boundingBox instanceof GPXBounds);
    }
}
