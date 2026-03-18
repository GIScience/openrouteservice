package org.heigit.ors.matching;

import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.matching.MatchingRequest.PolygonBBox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PolygonBBox spatial filtering.
 */
@DisplayName("PolygonBBox spatial filtering features")
class PolygonBBoxTest {

    private static final double[][] POLY_COORDS = {
            {8.684143117840563, 49.403360437437186},
            {8.684779169985319, 49.40302579535094},
            {8.685354323521352, 49.403404469120744},
            {8.684697971839427, 49.40380515562717},
            {8.684143117840563, 49.403360437437186}
    };

    static Polygon buildReferencePolygon() {
        Coordinate[] coords = new Coordinate[POLY_COORDS.length];
        for (int i = 0; i < POLY_COORDS.length; i++) {
            coords[i] = new Coordinate(POLY_COORDS[i][0], POLY_COORDS[i][1]);
        }
        Polygon polygon = GeometryTestUtils.getGeometryFactory().createPolygon(coords);
        polygon.setSRID(GeometryTestUtils.getSRID());
        return polygon;
    }

    @Test
    @DisplayName("Given cell outside BBox envelope, then intersects(4d) returns false")
    void cellOutsideEnvelopeShouldNotIntersect() {
        Polygon polygon = buildReferencePolygon();
        var env = polygon.getEnvelopeInternal();
        var preparedGeom = PreparedGeometryFactory.prepare(polygon);
        var polyBBox = new PolygonBBox(preparedGeom,
                env.getMinX(), env.getMaxX(),
                env.getMinY(), env.getMaxY());

        assertThat(polyBBox.intersects(env.getMaxX() +0.5, env.getMaxX() + 0.6,
                env.getMinY(), env.getMaxY())).isFalse();
    }

    @Test
    @DisplayName("Given cell in polygon interior, then intersects(4d) returns true")
    void cellInteriorShouldIntersect() {
        Polygon polygon = buildReferencePolygon();
        var env = polygon.getEnvelopeInternal();
        var preparedGeom = PreparedGeometryFactory.prepare(polygon);
        var polyBBox = new PolygonBBox(preparedGeom,
                env.getMinX(), env.getMaxX(),
                env.getMinY(), env.getMaxY());

        double mid = (env.getMinX() + env.getMaxX()) / 2;
        double midLat = (env.getMinY() + env.getMaxY()) / 2;
        assertThat(polyBBox.intersects(mid - 0.0001, mid + 0.0001,
                midLat - 0.0001, midLat + 0.0001)).isTrue();
    }

    @Test
    @DisplayName("Given overlapping BBox, then intersects(BBox) returns true")
    void overlappingBBoxShouldIntersect() {
        Polygon polygon = buildReferencePolygon();
        var env = polygon.getEnvelopeInternal();
        var preparedGeom = PreparedGeometryFactory.prepare(polygon);
        var polyBBox = new PolygonBBox(preparedGeom,
                env.getMinX(), env.getMaxX(),
                env.getMinY(), env.getMaxY());

        BBox other = new BBox((env.getMinX() + env.getMaxX()) / 2 - 0.0001,
                (env.getMinX() + env.getMaxX()) / 2 + 0.0001,
                (env.getMinY() + env.getMaxY()) / 2 - 0.0001,
                (env.getMinY() + env.getMaxY()) / 2 + 0.0001);

        assertThat(polyBBox.intersects(other)).isTrue();
    }

    @Test
    @DisplayName("Given small BBox inside polygon, then contains(BBox) returns true")
    void smallBBoxInsideShouldBeContained() {
        Polygon polygon = buildReferencePolygon();
        var env = polygon.getEnvelopeInternal();
        var preparedGeom = PreparedGeometryFactory.prepare(polygon);
        var polyBBox = new PolygonBBox(preparedGeom,
                env.getMinX(), env.getMaxX(),
                env.getMinY(), env.getMaxY());

        double mid = (env.getMinX() + env.getMaxX()) / 2;
        double midLat = (env.getMinY() + env.getMaxY()) / 2;
        BBox tiny = new BBox(mid - 0.00001, mid + 0.00001,
                midLat - 0.00001, midLat + 0.00001);

        assertThat(polyBBox.contains(tiny)).isTrue();
    }
}
