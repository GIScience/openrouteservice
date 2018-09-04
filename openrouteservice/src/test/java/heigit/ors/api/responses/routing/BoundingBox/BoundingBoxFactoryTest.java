package heigit.ors.api.responses.routing.BoundingBox;

import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxFactory;
import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXBounds;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSON3DBoundingBox;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONBoundingBox;
import org.junit.Assert;
import org.junit.Test;

public class BoundingBoxFactoryTest {
    @Test
    public void testCorrectTypeCreated() throws Exception {
        RouteRequest request = new RouteRequest(new Double[] {123.4, 567.8}, new Double[] {321.4, 765.8});
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
