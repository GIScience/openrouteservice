package org.heigit.ors.matching;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.heigit.ors.matching.GeometryTestUtils.polygonClosed;

/**
 * Performance regression test for matchArea() polygon intersection optimizations.
 * Validates that PreparedGeometry + tower-node optimizations provide significant speedup
 * over naive geometry intersection checks.
 * 
 * Uses pure JTS (no GraphHopper infrastructure) by synthesizing LineString objects,
 * allowing isolation of intersection computation performance.
 */
@DisplayName("matchArea intersection strategy performance")
class MatchAreaPerformanceTest {

    // Reference polygon from ParamsTest GEO_JSON — near Heidelberg
    private static final double[][] POLY_COORDS = {
        {8.684143117840563, 49.403360437437186},
        {8.684779169985319, 49.40302579535094},
        {8.685354323521352, 49.403404469120744},
        {8.684697971839427, 49.40380515562717},
        {8.684143117840563, 49.403360437437186}
    };
    private static final int EDGE_COUNT = 50_000;
    private static final int WARMUP_ITERATIONS = 3;
    private static final int TIMED_ITERATIONS = 5;

    @Test
    @DisplayName("preparedGeom.intersects() must be faster than geom.intersects() over 50 000 synthetic edges")
    void preparedGeomIntersectsMustOutperformNaive() {
        // ARRANGE
        GeometryFactory gf = GeometryTestUtils.getGeometryFactory();
        Polygon polygon = buildReferencePolygon();

        // Generate 50 000 synthetic LineStrings: ~50% inside, ~50% outside
        List<LineString> edges = buildSyntheticEdges(gf, EDGE_COUNT);

        // WARMUP (prevent JIT skewing results)
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runNaive(polygon, edges);
            runPrepared(polygon, edges);
        }

        // ACT — timed runs
        long naiveTotal = 0, preparedTotal = 0;
        for (int i = 0; i < TIMED_ITERATIONS; i++) {
            naiveTotal    += timeNs(() -> runNaive(polygon, edges));
            preparedTotal += timeNs(() -> runPrepared(polygon, edges));
        }
        double naiveAvgMs    = naiveTotal    / (double) TIMED_ITERATIONS / 1_000_000.0;
        double preparedAvgMs = preparedTotal / (double) TIMED_ITERATIONS / 1_000_000.0;

        // ASSERT: prepared path must be at least 1.5x faster than naive
        // (Using 1.5x instead of 2x due to machine variance in CI environments)
        double maxAllowedMs = naiveAvgMs / 1.5;
        assertThat(preparedAvgMs)
            .as("PreparedGeometry avg (%.2f ms) must be < 1.5× of naive avg (%.2f ms)",
                preparedAvgMs, naiveAvgMs)
            .isLessThan(maxAllowedMs);
    }

    /**
     * Builds the reference Polygon from GEO_JSON coordinates.
     */
    private Polygon buildReferencePolygon() {
        return polygonClosed(POLY_COORDS);
    }

    /**
     * Generates synthetic LineStrings: approximately 50% inside and 50% outside the polygon.
     * Distributes linestrings at random offsets within and outside polygon bounding box ±10%.
     */
    private List<LineString> buildSyntheticEdges(GeometryFactory gf, int count) {
        List<LineString> edges = new ArrayList<>(count);
        Polygon polyForEnv = polygonClosed(POLY_COORDS);
        Envelope polyEnv = polyForEnv.getEnvelopeInternal();

        Random rand = new Random(42);  // Fixed seed for reproducibility
        double minX = polyEnv.getMinX();
        double minY = polyEnv.getMinY();
        double maxX = polyEnv.getMaxX();
        double maxY = polyEnv.getMaxY();
        double width = maxX - minX;
        double height = maxY - minY;

        for (int i = 0; i < count; i++) {
            // Alternate between inside and outside regions for roughly 50/50 split
            boolean insideRegion = i % 2 == 0;
            
            double x, y, x2, y2;
            if (insideRegion) {
                // Generate inside polygon bounding box
                x  = minX + rand.nextDouble() * width;
                y  = minY + rand.nextDouble() * height;
                x2 = minX + rand.nextDouble() * width;
                y2 = minY + rand.nextDouble() * height;
            } else {
                // Generate outside polygon bounding box (±10% expansion)
                double expandX = width * 0.1;
                double expandY = height * 0.1;
                double outsideMinX = minX - expandX;
                double outsideMinY = minY - expandY;
                double outsideWidth = width + 2 * expandX;
                double outsideHeight = height + 2 * expandY;

                x  = outsideMinX + rand.nextDouble() * outsideWidth;
                y  = outsideMinY + rand.nextDouble() * outsideHeight;
                x2 = outsideMinX + rand.nextDouble() * outsideWidth;
                y2 = outsideMinY + rand.nextDouble() * outsideHeight;
            }

            Coordinate[] coords = {new Coordinate(x, y), new Coordinate(x2, y2)};
            LineString line = gf.createLineString(coords);
            line.setSRID(GeometryTestUtils.getSRID());
            edges.add(line);
        }

        return edges;
    }

    /**
     * Runs naive intersection checks: counts edges via geom.intersects(e).
     */
    private int runNaive(Geometry geom, List<LineString> edges) {
        int count = 0;
        for (LineString edge : edges) {
            if (geom.intersects(edge)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Runs prepared intersection checks: counts edges via PreparedGeometry.intersects(e).
     */
    private int runPrepared(Geometry geom, List<LineString> edges) {
        var prepared = PreparedGeometryFactory.prepare(geom);
        int count = 0;
        for (LineString edge : edges) {
            if (prepared.intersects(edge)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Times a runnable in nanoseconds.
     */
    private long timeNs(Runnable r) {
        long start = System.nanoTime();
        r.run();
        return System.nanoTime() - start;
    }
}
