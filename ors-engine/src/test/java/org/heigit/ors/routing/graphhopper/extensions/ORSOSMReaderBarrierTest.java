/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.reader.ReaderNode;
import com.graphhopper.routing.OSMReaderConfig;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ORSOSMReader#isBarrierNode(ReaderNode)}.
 *
 * Verifies the barrier-edge split policy: a topology split (and thus a barrier edge) is created
 * when ANY registered encoder is blocked by the node. The split is skipped only when EVERY encoder
 * can pass — those would be zero-length no-op edges. The barrier edge itself carries per-encoder
 * access via {@link EncodingManager#handleNodeTags}, so encoders that can pass simply traverse it.
 *
 * The single-encoder cases below exercise the common ORS model (one flag encoder per graph). The
 * {@code multiEncoder_*} cases guard the multi-encoder branch: a selective barrier (blocks one
 * profile, passes another) MUST still split, otherwise the blocked profile would leak through.
 */
class ORSOSMReaderBarrierTest {

    private ORSOSMReader reader;

    @BeforeEach
    void setUp() {
        CarFlagEncoder carEncoder = new CarFlagEncoder();
        EncodingManager encodingManager = new EncodingManager.Builder().add(carEncoder).build();
        reader = newReader(encodingManager);
    }

    /** Builds an ORSOSMReader over a graph using the given encoding manager. */
    private static ORSOSMReader newReader(EncodingManager encodingManager) {
        GraphHopperStorage storage = new GraphBuilder(encodingManager).create();

        GraphProcessContext mockCtx = Mockito.mock(GraphProcessContext.class);
        Mockito.when(mockCtx.getStorageBuilders()).thenReturn(Collections.emptyList());
        Mockito.when(mockCtx.isUseSidewalks()).thenReturn(false);

        return new ORSOSMReader(storage, new OSMReaderConfig(), mockCtx);
    }

    private static ReaderNode makeNode(long id, Map<String, Object> tags) {
        return new ReaderNode(id, 0.0, 0.0, tags);
    }

    // ---------------------------------------------------------------------------
    // Parameterized: isBarrierNode return value per tag combination
    //
    // Covers: blockByDefault barriers, passByDefault barriers, access overrides,
    // and nodes with no barrier tag at all.
    // ---------------------------------------------------------------------------

    static Stream<Arguments> barrierNodeExpectations() {
        return Stream.of(
            // blocked by default (blockByDefaultBarriers list in CarFlagEncoder)
            Arguments.of("bollard - always blocked",              Map.of("barrier", "bollard"),                       true),
            Arguments.of("fence - always blocked",                Map.of("barrier", "fence"),                         true),
            Arguments.of("stile - always blocked",                Map.of("barrier", "stile"),                         true),
            Arguments.of("cycle_barrier - always blocked",        Map.of("barrier", "cycle_barrier"),                 true),
            // blocked by default but access=yes overrides
            Arguments.of("bollard + access=yes - explicitly pass", Map.of("barrier", "bollard", "access", "yes"),     false),
            // passable by default (passByDefaultBarriers list in CarFlagEncoder)
            Arguments.of("gate - passes by default",              Map.of("barrier", "gate"),                          false),
            Arguments.of("lift_gate - passes by default",         Map.of("barrier", "lift_gate"),                     false),
            Arguments.of("cattle_grid - passes by default",       Map.of("barrier", "cattle_grid"),                   false),
            // passable by default but access=no overrides
            Arguments.of("gate + access=yes - explicitly pass",   Map.of("barrier", "gate", "access", "yes"),         false),
            Arguments.of("gate + access=no - explicitly blocked", Map.of("barrier", "gate", "access", "no"),          true),
            // no barrier tag - base class returns false, counters must not increment
            Arguments.of("highway=crossing - not a barrier",      Map.of("highway", "crossing"),                      false),
            Arguments.of("name tag only - not a barrier",         Map.of("name", "Main Street"),                      false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("barrierNodeExpectations")
    void isBarrierNode_matchesExpectedResult(String description, Map<String, Object> tags, boolean expected) {
        assertEquals(expected, reader.isBarrierNode(makeNode(1L, tags)),
            "isBarrierNode mismatch for: " + description);
    }

    // ---------------------------------------------------------------------------
    // Parameterized: counter state after processing a sequence of nodes
    //
    // Each scenario feeds a list of tag maps through isBarrierNode in order and
    // asserts the resulting total / skipped counts.
    // ---------------------------------------------------------------------------

    static Stream<Arguments> counterScenarios() {
        return Stream.of(
            Arguments.of(
                "1 blocked + 1 passable → total=2 skipped=1",
                List.of(
                    Map.of("barrier", "bollard"),               // blocked
                    Map.of("barrier", "gate", "access", "yes")  // passable
                ),
                2, 1
            ),
            Arguments.of(
                "all blocked → total=3 skipped=0",
                List.of(
                    Map.of("barrier", "bollard"),
                    Map.of("barrier", "fence"),
                    Map.of("barrier", "gate", "access", "no")
                ),
                3, 0
            ),
            Arguments.of(
                "non-barrier nodes only → total=0 skipped=0",
                List.of(
                    Map.of("highway", "crossing"),
                    Map.of("name", "Main Street")
                ),
                0, 0
            ),
            Arguments.of(
                "all passable → total=3 skipped=3",
                List.of(
                    Map.of("barrier", "gate"),
                    Map.of("barrier", "lift_gate"),
                    Map.of("barrier", "bollard", "access", "yes")
                ),
                3, 3
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("counterScenarios")
    void counters_reflectBarrierDecisions(String description,
                                          List<Map<String, Object>> nodeTags,
                                          int expectedTotal,
                                          int expectedSkipped) {
        for (int i = 0; i < nodeTags.size(); i++) {
            reader.isBarrierNode(makeNode(i, nodeTags.get(i)));
        }
        assertEquals(expectedTotal,   reader.getBarrierNodesTotal(),   description + ": total mismatch");
        assertEquals(expectedSkipped, reader.getBarrierNodesSkipped(), description + ": skipped mismatch");
    }

    // ---------------------------------------------------------------------------
    // Multi-encoder branch: a graph with both car and foot encoders.
    //
    // Car blocks bollard/fence/stile by default; foot passes a bollard but blocks a fence.
    // A selective barrier (bollard) MUST still split because car is blocked — otherwise the car
    // restriction would be silently dropped. The split is skipped only when BOTH encoders pass.
    // ---------------------------------------------------------------------------

    static Stream<Arguments> multiEncoderExpectations() {
        return Stream.of(
            // selective: car blocked, foot passes → must split (regression guard)
            Arguments.of("bollard - car blocked, foot passes → split",   Map.of("barrier", "bollard"),               true),
            // both blocked → split
            Arguments.of("fence - both blocked → split",                 Map.of("barrier", "fence"),                 true),
            // car blocked via access=no, foot blocked via access=no on a pass-by-default gate → split
            Arguments.of("gate + access=no - both blocked → split",      Map.of("barrier", "gate", "access", "no"),  true),
            // passable for both encoders → no split
            Arguments.of("gate - both pass → no split",                  Map.of("barrier", "gate"),                  false),
            Arguments.of("cattle_grid - both pass → no split",           Map.of("barrier", "cattle_grid"),           false),
            // explicit access=yes lets both pass → no split
            Arguments.of("bollard + access=yes - both pass → no split",  Map.of("barrier", "bollard", "access", "yes"), false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("multiEncoderExpectations")
    void multiEncoder_selectiveBarrierStillSplits(String description, Map<String, Object> tags, boolean expected) {
        EncodingManager carAndFoot = new EncodingManager.Builder()
                .add(new CarFlagEncoder())
                .add(new FootFlagEncoder())
                .build();
        ORSOSMReader multiReader = newReader(carAndFoot);

        assertEquals(expected, multiReader.isBarrierNode(makeNode(1L, tags)),
            "multi-encoder isBarrierNode mismatch for: " + description);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("multiEncoderExpectations")
    void multiEncoder_skippedCountReflectsAllPass(String description, Map<String, Object> tags, boolean expected) {
        EncodingManager carAndFoot = new EncodingManager.Builder()
                .add(new CarFlagEncoder())
                .add(new FootFlagEncoder())
                .build();
        ORSOSMReader multiReader = newReader(carAndFoot);

        multiReader.isBarrierNode(makeNode(1L, tags));

        // A node is "skipped" only when no encoder is blocked, i.e. when it does NOT split.
        assertEquals(1, multiReader.getBarrierNodesTotal(), description + ": total mismatch");
        assertEquals(expected ? 0 : 1, multiReader.getBarrierNodesSkipped(), description + ": skipped mismatch");
    }
}
