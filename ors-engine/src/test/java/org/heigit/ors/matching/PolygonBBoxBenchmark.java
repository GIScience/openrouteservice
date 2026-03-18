package org.heigit.ors.matching;

import com.graphhopper.util.shapes.BBox;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark: BBox.intersects() (plain AABB) vs PolygonBBox.intersects()
 * (AABB + PreparedGeometry) over the Heidelberg area bounding box.
 *
 * <p>Heidelberg polygon (GeoJSON ring):
 * [[[8.6761038698,49.4099139015],[8.677530805,49.4099139015],
 *   [8.677530805,49.411205303],[8.6761038698,49.411205303],
 *   [8.6761038698,49.4099139015]]]
 *
 * <p>Run via JUnit: {@code ./mvnw test -pl ors-engine -Dtest=JmhRunnerTest}
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class PolygonBBoxBenchmark {

    // Heidelberg rectangle polygon (closed ring)
    private static final double MIN_LON = 8.6761038698;
    private static final double MAX_LON = 8.677530805;
    private static final double MIN_LAT = 49.4099139015;
    private static final double MAX_LAT = 49.411205303;

    // Query cell inside the polygon (interior probe)
    private static final double CELL_MIN_LON = 8.6765;
    private static final double CELL_MAX_LON = 8.6770;
    private static final double CELL_MIN_LAT = 49.4101;
    private static final double CELL_MAX_LAT = 49.4105;

    // Query cell outside the polygon (fast-fail probe)
    private static final double OUT_MIN_LON = 9.0;
    private static final double OUT_MAX_LON = 9.1;
    private static final double OUT_MIN_LAT = 50.0;
    private static final double OUT_MAX_LAT = 50.1;

    private BBox plainBBox;
    private MatchingRequest.PolygonBBox polygonBBox;

    @Setup(Level.Trial)
    public void setUp() {
        // Build the Heidelberg polygon
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
        Coordinate[] ring = {
            new Coordinate(MIN_LON, MIN_LAT),
            new Coordinate(MAX_LON, MIN_LAT),
            new Coordinate(MAX_LON, MAX_LAT),
            new Coordinate(MIN_LON, MAX_LAT),
            new Coordinate(MIN_LON, MIN_LAT)   // closed
        };
        Polygon polygon = gf.createPolygon(ring);
        var preparedGeom = PreparedGeometryFactory.prepare(polygon);

        plainBBox   = new BBox(MIN_LON, MAX_LON, MIN_LAT, MAX_LAT);
        polygonBBox = new MatchingRequest.PolygonBBox(
                preparedGeom, MIN_LON, MAX_LON, MIN_LAT, MAX_LAT);
    }

    // ── 4-double overload benchmarks ──────────────────────────────────────

    @Benchmark
    public boolean bboxIntersects4dInside() {
        return plainBBox.intersects(CELL_MIN_LON, CELL_MAX_LON, CELL_MIN_LAT, CELL_MAX_LAT);
    }

    @Benchmark
    public boolean polygonBBoxIntersects4dInside() {
        return polygonBBox.intersects(CELL_MIN_LON, CELL_MAX_LON, CELL_MIN_LAT, CELL_MAX_LAT);
    }

    @Benchmark
    public boolean bboxIntersects4dOutside() {
        return plainBBox.intersects(OUT_MIN_LON, OUT_MAX_LON, OUT_MIN_LAT, OUT_MAX_LAT);
    }

    @Benchmark
    public boolean polygonBBoxIntersects4dOutside() {
        return polygonBBox.intersects(OUT_MIN_LON, OUT_MAX_LON, OUT_MIN_LAT, OUT_MAX_LAT);
    }

    // ── BBox overload benchmarks ──────────────────────────────────────────

    @Benchmark
    public boolean bboxIntersectsBBoxInside() {
        BBox probe = new BBox(CELL_MIN_LON, CELL_MAX_LON, CELL_MIN_LAT, CELL_MAX_LAT);
        return plainBBox.intersects(probe);
    }

    @Benchmark
    public boolean polygonBBoxIntersectsBBoxInside() {
        BBox probe = new BBox(CELL_MIN_LON, CELL_MAX_LON, CELL_MIN_LAT, CELL_MAX_LAT);
        return polygonBBox.intersects(probe);
    }
}
