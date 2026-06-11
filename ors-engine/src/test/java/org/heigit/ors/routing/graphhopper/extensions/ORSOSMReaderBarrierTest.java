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
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.CarFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.PedestrianFlagEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * <p>
 * Verifies the barrier-edge split policy: a topology split (and thus a barrier edge) is created
 * when ANY registered encoder is blocked by the node. The split is skipped only when EVERY encoder
 * can pass — those would be zero-length no-op edges. The barrier edge itself carries per-encoder
 * access via {@link EncodingManager#handleNodeTags}, so encoders that can pass simply traverse it.
 * <p>
 * ORS-specific barrier behaviour (differs from upstream GH CarFlagEncoder):
 * - {@code block_barriers=true} by default in {@code VehicleFlagEncoder.setProperties()} causes
 *   passable-by-default barriers (gate, lift_gate) to block when no access tag permits them.
 * - {@code fence} is not in ORS VehicleFlagEncoder's barrier lists, so it is not a barrier for cars.
 * <p>
 * The single-encoder cases exercise the common ORS model (one flag encoder per graph). The
 * {@code multiEncoder_*} cases guard the multi-encoder branch: a selective barrier (blocks one
 * profile, passes another) MUST still split, otherwise the blocked profile would leak through.
 */
class ORSOSMReaderBarrierTest {

    private ORSOSMReader reader;

    @BeforeEach
    void setUp() {
        CarFlagEncoder carEncoder = (CarFlagEncoder) new ORSDefaultFlagEncoderFactory()
                .createFlagEncoder(FlagEncoderNames.CAR_ORS, new PMap());
        EncodingManager encodingManager = new EncodingManager.Builder()
                .add(carEncoder)
                .add(Subnetwork.create(FlagEncoderNames.CAR_ORS))
                .build();
        reader = newReader(encodingManager);
    }

    /** Builds an ORSOSMReader over a graph using the given encoding manager. */
    private static ORSOSMReader newReader(EncodingManager encodingManager) {
        ORSGraphHopperStorage storage = new ORSGraphHopperStorage(
                new RAMDirectory(), encodingManager, false, false, -1);
        storage.create(1000);

        GraphProcessContext mockCtx = Mockito.mock(GraphProcessContext.class);
        Mockito.when(mockCtx.getStorageBuilders()).thenReturn(Collections.emptyList());
        Mockito.when(mockCtx.isUseSidewalks()).thenReturn(false);

        return new ORSOSMReader(storage, new OSMReaderConfig(), mockCtx);
    }

    private static ReaderNode makeNode(long id, Map<String, Object> tags) {
        return new ReaderNode(id, 0.0, 0.0, tags);
    }

    static Stream<Arguments> barrierNodeExpectations() {
        return Stream.of(
            // blocked by default (blockByDefaultBarriers in ORS VehicleFlagEncoder)
            Arguments.of("bollard - always blocked",                        Map.of("barrier", "bollard"),                       true),
            // fence is NOT in ORS VehicleFlagEncoder barrier lists (unlike upstream GH CarFlagEncoder)
            Arguments.of("fence - not in ORS car barrier lists",            Map.of("barrier", "fence"),                         false),
            Arguments.of("stile - always blocked",                          Map.of("barrier", "stile"),                         true),
            Arguments.of("cycle_barrier - always blocked",                  Map.of("barrier", "cycle_barrier"),                 true),
            // blocked by default but access=yes overrides via intendedValues
            Arguments.of("bollard + access=yes - explicitly pass",          Map.of("barrier", "bollard", "access", "yes"),      false),
            // passByDefaultBarriers, but ORS block_barriers=true means no access tag → blocked
            Arguments.of("gate - blocked (ORS block_barriers=true)",        Map.of("barrier", "gate"),                          true),
            Arguments.of("lift_gate - blocked (ORS block_barriers=true)",   Map.of("barrier", "lift_gate"),                     true),
            // cattle_grid not in any ORS VehicleFlagEncoder list → not a recognized barrier
            Arguments.of("cattle_grid - not in ORS car barrier lists",      Map.of("barrier", "cattle_grid"),                   false),
            // passByDefaultBarriers + access=yes → intendedValues → explicitly pass
            Arguments.of("gate + access=yes - explicitly pass",             Map.of("barrier", "gate", "access", "yes"),         false),
            // passByDefaultBarriers + access=no → restrictedValues → blocked
            Arguments.of("gate + access=no - explicitly blocked",           Map.of("barrier", "gate", "access", "no"),          true),
            // no barrier tag → super.isBarrierNode returns false, counters must not increment
            Arguments.of("highway=crossing - not a barrier",                Map.of("highway", "crossing"),                      false),
            Arguments.of("name tag only - not a barrier",                   Map.of("name", "Main Street"),                      false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("barrierNodeExpectations")
    @DisplayName("Given a single-encoder ORS car graph, when isBarrierNode is called, then the result matches ORS barrier policy")
    void isBarrierNodeMatchesExpectedResult(String description, Map<String, Object> tags, boolean expected) {
        assertEquals(expected, reader.isBarrierNode(makeNode(1L, tags)),
            "isBarrierNode mismatch for: " + description);
    }

    static Stream<Arguments> counterScenarios() {
        return Stream.of(
            Arguments.of(
                // bollard and gate+access=no are blocked; fence is not in ORS car lists → skipped
                "bollard+gate(no) blocked, fence not ORS barrier → total=3 skipped=1",
                List.of(
                    Map.of("barrier", "bollard"),               // blocked
                    Map.of("barrier", "gate", "access", "no"),  // blocked
                    Map.of("barrier", "fence")                  // not in ORS car lists → skipped
                ),
                3, 1
            ),
            Arguments.of(
                // all are blockByDefault barriers
                "three blockByDefault barriers → total=3 skipped=0",
                List.of(
                    Map.of("barrier", "bollard"),
                    Map.of("barrier", "stile"),
                    Map.of("barrier", "cycle_barrier")
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
                // gate and lift_gate blocked by ORS block_barriers=true; bollard+access=yes passes
                "gate+lift_gate blocked (block_barriers=true), bollard+access=yes passes → total=3 skipped=1",
                List.of(
                    Map.of("barrier", "gate"),
                    Map.of("barrier", "lift_gate"),
                    Map.of("barrier", "bollard", "access", "yes")
                ),
                3, 1
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("counterScenarios")
    @DisplayName("Given a sequence of barrier nodes, when isBarrierNode is called for each, then total and skipped counters are accurate")
    void countersReflectBarrierDecisions(String description,
                                          List<Map<String, Object>> nodeTags,
                                          int expectedTotal,
                                          int expectedSkipped) {
        for (int i = 0; i < nodeTags.size(); i++) {
            reader.isBarrierNode(makeNode(i, nodeTags.get(i)));
        }
        assertEquals(expectedTotal,   reader.getBarrierNodesTotal(),   description + ": total mismatch");
        assertEquals(expectedSkipped, reader.getBarrierNodesSkipped(), description + ": skipped mismatch");
    }

    static Stream<Arguments> multiEncoderExpectations() {
        return Stream.of(
            // selective: car blocked, foot passes → must split (regression guard for multi-encoder correctness)
            Arguments.of("bollard - car blocked, foot passes → split",             Map.of("barrier", "bollard"),               true),
            // fence: ORS car does NOT block it, but ORS foot (GH base) does → ANY blocks → split
            Arguments.of("fence - ORS car passes, foot blocks → split",            Map.of("barrier", "fence"),                 true),
            // both blocked via access=no
            Arguments.of("gate + access=no - both blocked → split",               Map.of("barrier", "gate", "access", "no"),  true),
            // ORS car blocks gate (block_barriers=true); foot (GH base) passes gate → ANY blocks → split
            Arguments.of("gate - ORS car blocks (block_barriers=true), foot passes → split",
                                                                                    Map.of("barrier", "gate"),                  true),
            // cattle_grid: not in ORS car lists (passes), GH foot passByDefault (passes) → no split
            Arguments.of("cattle_grid - both pass → no split",                     Map.of("barrier", "cattle_grid"),           false),
            // explicit access=yes lets both pass
            Arguments.of("bollard + access=yes - both pass → no split",            Map.of("barrier", "bollard", "access", "yes"), false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("multiEncoderExpectations")
    @DisplayName("Given a car+pedestrian graph, when a selective barrier blocks only one encoder, then isBarrierNode still returns true")
    void multiEncoderSelectiveBarrierStillSplits(String description, Map<String, Object> tags, boolean expected) {
        ORSOSMReader multiReader = newReader(buildCarAndPedestrianEM());

        assertEquals(expected, multiReader.isBarrierNode(makeNode(1L, tags)),
            "multi-encoder isBarrierNode mismatch for: " + description);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("multiEncoderExpectations")
    @DisplayName("Given a car+pedestrian graph, when isBarrierNode is called, then skipped count reflects all-pass nodes only")
    void multiEncoderSkippedCountReflectsAllPass(String description, Map<String, Object> tags, boolean expected) {
        ORSOSMReader multiReader = newReader(buildCarAndPedestrianEM());

        multiReader.isBarrierNode(makeNode(1L, tags));

        // A node is "skipped" only when no encoder is blocked, i.e. when it does NOT split.
        assertEquals(1, multiReader.getBarrierNodesTotal(), description + ": total mismatch");
        assertEquals(expected ? 0 : 1, multiReader.getBarrierNodesSkipped(), description + ": skipped mismatch");
    }

    private static EncodingManager buildCarAndPedestrianEM() {
        CarFlagEncoder car = (CarFlagEncoder) new ORSDefaultFlagEncoderFactory()
                .createFlagEncoder(FlagEncoderNames.CAR_ORS, new PMap());
        PedestrianFlagEncoder foot = (PedestrianFlagEncoder) new ORSDefaultFlagEncoderFactory()
                .createFlagEncoder(FlagEncoderNames.PEDESTRIAN_ORS, new PMap());
        return new EncodingManager.Builder()
                .add(car).add(Subnetwork.create(FlagEncoderNames.CAR_ORS))
                .add(foot).add(Subnetwork.create(FlagEncoderNames.PEDESTRIAN_ORS))
                .build();
    }
}
