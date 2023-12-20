package org.heigit.ors.api.responses.routing.boundingbox;

import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBoxFactory;
import org.heigit.ors.api.responses.routing.gpx.GPXBounds;
import org.heigit.ors.api.responses.routing.json.JSON3DBoundingBox;
import org.heigit.ors.api.responses.routing.json.JSONBoundingBox;
import org.heigit.ors.api.APIEnums;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundingBoxFactoryTest {
    @Test
    void testCorrectTypeCreated() throws Exception {
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
        coords[0] = new Double[]{24.5, 39.2};
        coords[1] = new Double[]{27.4, 38.6};

        RouteRequest request = new RouteRequest(coords);

        BBox bbox = new BBox(1, 2, 3, 4, 5, 6);

        request.setUseElevation(true);
        request.setResponseType(APIEnums.RouteResponseType.JSON);
        BoundingBox boundingBox = BoundingBoxFactory.constructBoundingBox(bbox, request);
        assertTrue(boundingBox instanceof JSON3DBoundingBox);

        request.setUseElevation(false);
        boundingBox = BoundingBoxFactory.constructBoundingBox(bbox, request);
        assertTrue(boundingBox instanceof JSONBoundingBox);

        request.setUseElevation(true);
        request.setResponseType(APIEnums.RouteResponseType.GEOJSON);
        boundingBox = BoundingBoxFactory.constructBoundingBox(bbox, request);
        assertTrue(boundingBox instanceof JSON3DBoundingBox);

        request.setUseElevation(false);
        boundingBox = BoundingBoxFactory.constructBoundingBox(bbox, request);
        assertTrue(boundingBox instanceof JSONBoundingBox);

        request.setResponseType(APIEnums.RouteResponseType.GPX);
        boundingBox = BoundingBoxFactory.constructBoundingBox(bbox, request);
        assertTrue(boundingBox instanceof GPXBounds);
    }
}
