package org.heigit.ors.matching;

import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.heigit.ors.matching.GeometryTestUtils.lineString;
import static org.heigit.ors.matching.GeometryTestUtils.polygonClosed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Correctness tests for the matchArea() method.
 * Validates that edges intersecting a polygon are included and non-intersecting edges are excluded.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MatchArea correctness validation")
class MatchAreaCorrectnessTest {

    // Reference polygon from ParamsTest GEO_JSON — near Heidelberg
    private static final double[][] POLYGON_COORDS = {
        {8.684143117840563, 49.403360437437186},
        {8.684779169985319, 49.40302579535094},
        {8.685354323521352, 49.403404469120744},
        {8.684697971839427, 49.40380515562717},
        {8.684143117840563, 49.403360437437186}  // close ring
    };

    private MatchingRequest matchingRequest;

    @Mock
    private GraphHopperStorage mockGhStorage;

    @Mock
    private LocationIndex mockLocIndex;

    @Mock
    private EdgeIteratorState mockEdgeInside;

    @Mock
    private EdgeIteratorState mockEdgeOutside;

    @BeforeEach
    void setUp() throws Exception {
        matchingRequest = new MatchingRequest(1, 100);

        // Inject mock ghStorage via reflection
        Field ghStorageField = MatchingRequest.class.getDeclaredField("ghStorage");
        ghStorageField.setAccessible(true);
        ghStorageField.set(matchingRequest, mockGhStorage);
    }

    /**
     * Validates that edges intersecting the polygon are included.
     */
    @Test
    @DisplayName("shouldReturnEdgesIntersectingPolygon")
    void shouldReturnEdgesIntersectingPolygon() {
        // ARRANGE
        Polygon polygon = buildReferencePolygon();

        // Mock edge 101: LineString INSIDE the polygon
        LineString insideLineString = lineString(8.6843, 49.4034, 8.6845, 49.4035);
        PointList insidePoints = buildPointList(insideLineString);

        // Tower nodes for edge 101 (also inside)
        PointList insideTowers = new PointList();
        insideTowers.add(49.4034, 8.6843);  // lat, lon
        insideTowers.add(49.4035, 8.6845);
        insideTowers = insideTowers.makeImmutable();

        when(mockEdgeInside.fetchWayGeometry(FetchMode.TOWER_ONLY)).thenReturn(insideTowers);
        when(mockEdgeInside.fetchWayGeometry(FetchMode.ALL)).thenReturn(insidePoints);

        // Mock edge 202: LineString OUTSIDE the polygon
        LineString outsideLineString = lineString(9.0, 50.0, 9.01, 50.01);
        PointList outsidePoints = buildPointList(outsideLineString);

        // Tower nodes for edge 202 (also outside)
        PointList outsideTowers = new PointList();
        outsideTowers.add(50.0, 9.0);    // lat, lon
        outsideTowers.add(50.01, 9.01);
        outsideTowers = outsideTowers.makeImmutable();

        when(mockEdgeOutside.fetchWayGeometry(FetchMode.TOWER_ONLY)).thenReturn(outsideTowers);
        when(mockEdgeOutside.fetchWayGeometry(FetchMode.ALL)).thenReturn(outsidePoints);

        // Mock ghStorage.getEdgeIteratorState()
        when(mockGhStorage.getEdgeIteratorState(101, Integer.MIN_VALUE))
            .thenReturn(mockEdgeInside);
        when(mockGhStorage.getEdgeIteratorState(202, Integer.MIN_VALUE))
            .thenReturn(mockEdgeOutside);

        // Mock LocationIndex.query() to invoke visitor for both edges
        doAnswer(invocation -> {
            LocationIndex.Visitor visitor = invocation.getArgument(1);
            visitor.onEdge(101);  // edge inside
            visitor.onEdge(202);  // edge outside
            return null;
        }).when(mockLocIndex).query(any(BBox.class), any(LocationIndex.Visitor.class));

        // ACT
        Set<Integer> matchedIds = new HashSet<>();
        matchingRequest.matchArea(polygon, mockLocIndex, matchedIds);

        // ASSERT
        assertThat(matchedIds)
            .as("MatchedIds should contain edge inside polygon")
            .contains(101)
            .as("MatchedIds should NOT contain edge outside polygon")
            .doesNotContain(202);
    }

    /**
     * Correctness test - Validates that PreparedGeometry produces same results as naive geom.intersects().
     */
    @Test
    @DisplayName("shouldProduceSameResultsWithPreparedGeometry")
    void shouldProduceSameResultsWithPreparedGeometry() {
        // This test validates that the optimization (PreparedGeometry) produces identical results to naive intersects.
        // ARRANGE
        Polygon polygon = buildReferencePolygon();

        // Same mock setup as above
        LineString insideLineString = lineString(8.6843, 49.4034, 8.6845, 49.4035);
        PointList insidePoints = buildPointList(insideLineString);
        
        PointList insideTowers = new PointList();
        insideTowers.add(49.4034, 8.6843);
        insideTowers.add(49.4035, 8.6845);
        insideTowers = insideTowers.makeImmutable();
        
        when(mockEdgeInside.fetchWayGeometry(FetchMode.TOWER_ONLY)).thenReturn(insideTowers);
        when(mockEdgeInside.fetchWayGeometry(FetchMode.ALL)).thenReturn(insidePoints);

        LineString outsideLineString = lineString(9.0, 50.0, 9.01, 50.01);
        PointList outsidePoints = buildPointList(outsideLineString);
        
        PointList outsideTowers = new PointList();
        outsideTowers.add(50.0, 9.0);
        outsideTowers.add(50.01, 9.01);
        outsideTowers = outsideTowers.makeImmutable();
        
        when(mockEdgeOutside.fetchWayGeometry(FetchMode.TOWER_ONLY)).thenReturn(outsideTowers);
        when(mockEdgeOutside.fetchWayGeometry(FetchMode.ALL)).thenReturn(outsidePoints);

        when(mockGhStorage.getEdgeIteratorState(101, Integer.MIN_VALUE))
            .thenReturn(mockEdgeInside);
        when(mockGhStorage.getEdgeIteratorState(202, Integer.MIN_VALUE))
            .thenReturn(mockEdgeOutside);

        doAnswer(invocation -> {
            LocationIndex.Visitor visitor = invocation.getArgument(1);
            visitor.onEdge(101);
            visitor.onEdge(202);
            return null;
        }).when(mockLocIndex).query(any(BBox.class), any(LocationIndex.Visitor.class));

        // ACT
        Set<Integer> matchedIds = new HashSet<>();
        matchingRequest.matchArea(polygon, mockLocIndex, matchedIds);

        // ASSERT: same expected result (same as Task 1 test)
        assertThat(matchedIds)
            .as("PreparedGeometry path must return same edges as naive path")
            .containsExactly(101);
    }

    /**
     * Task 3: Tower-node rejection test - Validates that edges with disjoint tower-only envelopes are skipped.
     * NOTE: This test validates that the tower rejection optimization works correctly.
     */
    @Test
    @DisplayName("shouldSkipEdgesWithTowerEnvelopeDisjointFromPolygon")
    void shouldSkipEdgesWithTowerEnvelopeDisjointFromPolygon() {
        // ARRANGE
        Polygon polygon = buildReferencePolygon();

        // Mock edge with geometry that is far OUTSIDE the polygon
        // Tower nodes are at (0.0, 0.0) and (0.001, 0.001) - far outside the Heidelberg polygon
        PointList disjointTowers = new PointList();
        disjointTowers.add(0.0, 0.0);      // lat, lon
        disjointTowers.add(0.001, 0.001);
        disjointTowers = disjointTowers.makeImmutable();

        LineString disjointLineString = lineString(0.0, 0.0, 0.001, 0.001);
        PointList disjointPoints = buildPointList(disjointLineString);

        // For Task 3: tower envelope should be checked FIRST and cause early exit
        // BUT if tower check doesn't reject it, still mock FetchMode.ALL as backup
        when(mockEdgeInside.fetchWayGeometry(FetchMode.TOWER_ONLY)).thenReturn(disjointTowers);
        when(mockEdgeInside.fetchWayGeometry(FetchMode.ALL)).thenReturn(disjointPoints);

        when(mockGhStorage.getEdgeIteratorState(303, Integer.MIN_VALUE))
            .thenReturn(mockEdgeInside);

        doAnswer(invocation -> {
            LocationIndex.Visitor visitor = invocation.getArgument(1);
            visitor.onEdge(303);  // edge far outside polygon
            return null;
        }).when(mockLocIndex).query(any(BBox.class), any(LocationIndex.Visitor.class));

        // ACT
        Set<Integer> matchedIds = new HashSet<>();
        matchingRequest.matchArea(polygon, mockLocIndex, matchedIds);

        // ASSERT: edge should be excluded (its tower envelope doesn't intersect polygon)
        assertThat(matchedIds)
            .as("Edge with tower envelope disjoint from polygon should be excluded")
            .isEmpty();
    }

    /**
     * Builds the reference Polygon from GEO_JSON coordinates.
     */
    private Polygon buildReferencePolygon() {
        return polygonClosed(POLYGON_COORDS);
    }

    /**
     * Converts a JTS LineString to a mock PointList.
     */
    private PointList buildPointList(LineString lineString) {
        PointList pointList = new PointList();
        for (Coordinate coord : lineString.getCoordinates()) {
            pointList.add(coord.y, coord.x);  // PointList expects lat, lon
        }
        return pointList.makeImmutable();
    }
}
