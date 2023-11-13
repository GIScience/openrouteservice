package org.heigit.ors.util;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.GHUtility;

public class ToyGraphCreationUtil {
    private static GraphHopperStorage createGHStorage(EncodingManager encodingManager) {
        return new GraphBuilder(encodingManager).create();
    }

    public static GraphHopperStorage createMediumGraph(EncodingManager encodingManager) {
        return createMediumGraph(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createMediumGraph(GraphHopperStorage g, EncodingManager encodingManager) {
        //    3---4--5
        //   /\   |  |
        //  2--0  6--7
        //  | / \   /
        //  |/   \ /
        //  1-----8
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 1).setDistance(1), //0
                g.edge(0, 2).setDistance(1), //1
                g.edge(0, 3).setDistance(5), //2
                g.edge(0, 8).setDistance(1), //3
                g.edge(1, 2).setDistance(1), //4
                g.edge(1, 8).setDistance(2), //5
                g.edge(2, 3).setDistance(2), //6
                g.edge(3, 4).setDistance(2), //7
                g.edge(4, 5).setDistance(1), //8
                g.edge(4, 6).setDistance(1), //9
                g.edge(5, 7).setDistance(1), //10
                g.edge(6, 7).setDistance(2), //11
                g.edge(7, 8).setDistance(3) //12
        );
        //Set test lat lon
        g.getBaseGraph().getNodeAccess().setNode(0, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(1, 1, 1);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 1);
        g.getBaseGraph().getNodeAccess().setNode(3, 4, 2);
        g.getBaseGraph().getNodeAccess().setNode(4, 4, 4);
        g.getBaseGraph().getNodeAccess().setNode(5, 4, 5);
        g.getBaseGraph().getNodeAccess().setNode(6, 3, 4);
        g.getBaseGraph().getNodeAccess().setNode(7, 3, 5);
        g.getBaseGraph().getNodeAccess().setNode(8, 1, 4);
        return g;
    }

    public static GraphHopperStorage createMediumGraph2(EncodingManager encodingManager) {
        return createMediumGraph2(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createMediumGraph2(GraphHopperStorage g, EncodingManager encodingManager) {
        //    3---4--5
        //   /\   |  |
        //  2--0  6--7
        //  | / \   /
        //  |/   \ /
        //  1-----8
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 1).setDistance(1),
                g.edge(0, 2).setDistance(1),
                g.edge(0, 3).setDistance(5),
                g.edge(0, 8).setDistance(1),
                g.edge(1, 2).setDistance(1),
                g.edge(1, 8).setDistance(2),
                g.edge(2, 3).setDistance(2),
                g.edge(3, 4).setDistance(2),
                g.edge(4, 5).setDistance(1),
                g.edge(4, 6).setDistance(1),
                g.edge(5, 7).setDistance(1),
                g.edge(6, 7).setDistance(2),
                g.edge(7, 8).setDistance(3)
        );
        //Set test lat lon
        g.getBaseGraph().getNodeAccess().setNode(0, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(1, 1, 1);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 1);
        g.getBaseGraph().getNodeAccess().setNode(3, 4, 2);
        g.getBaseGraph().getNodeAccess().setNode(4, 4, 5);
        g.getBaseGraph().getNodeAccess().setNode(5, 4, 6);
        g.getBaseGraph().getNodeAccess().setNode(6, 3, 5);
        g.getBaseGraph().getNodeAccess().setNode(7, 3, 6);
        g.getBaseGraph().getNodeAccess().setNode(8, 1, 4);
        return g;
    }

    public static GraphHopperStorage createMediumGraphWithAdditionalEdge(EncodingManager encodingManager) {
        return createMediumGraphWithAdditionalEdge(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createMediumGraphWithAdditionalEdge(GraphHopperStorage g, EncodingManager encodingManager) {
        //    3---4--5--9
        //   /\   |  |
        //  2--0  6--7
        //  | / \   /
        //  |/   \ /
        //  1-----8
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 1).setDistance(1),
                g.edge(0, 2).setDistance(1),
                g.edge(0, 3).setDistance(5),
                g.edge(0, 8).setDistance(1),
                g.edge(1, 2).setDistance(1),
                g.edge(1, 8).setDistance(2),
                g.edge(2, 3).setDistance(2),
                g.edge(3, 4).setDistance(2),
                g.edge(4, 5).setDistance(1),
                g.edge(4, 6).setDistance(1),
                g.edge(5, 7).setDistance(1),
                g.edge(5, 9).setDistance(1),
                g.edge(6, 7).setDistance(2),
                g.edge(7, 8).setDistance(3)
        );
        //Set test lat lon
        g.getBaseGraph().getNodeAccess().setNode(0, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(1, 1, 1);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 1);
        g.getBaseGraph().getNodeAccess().setNode(3, 4, 2);
        g.getBaseGraph().getNodeAccess().setNode(4, 4, 4);
        g.getBaseGraph().getNodeAccess().setNode(5, 4, 5);
        g.getBaseGraph().getNodeAccess().setNode(6, 3, 4);
        g.getBaseGraph().getNodeAccess().setNode(7, 3, 5);
        g.getBaseGraph().getNodeAccess().setNode(8, 1, 4);
        g.getBaseGraph().getNodeAccess().setNode(9, 4, 6);
        return g;
    }

    public static GraphHopperStorage createSingleEdgeGraph(EncodingManager encodingManager) {
        return createSingleEdgeGraph(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createSingleEdgeGraph(GraphHopperStorage g, EncodingManager encodingManager) {
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"), g.edge(0, 1).setDistance(1));

        g.getBaseGraph().getNodeAccess().setNode(0, 0, 0);
        g.getBaseGraph().getNodeAccess().setNode(1, 1, 1);
        return g;
    }

    public static GraphHopperStorage createSimpleGraph(EncodingManager encodingManager) {
        return createSimpleGraph(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createSimpleGraph(GraphHopperStorage g, EncodingManager encodingManager) {
        // 5--1---2
        //     \ /|
        //      0 |
        //     /  |
        //    4---3
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 1).setDistance(1),
                g.edge(0, 2).setDistance(1),
                g.edge(0, 4).setDistance(3),
                g.edge(1, 2).setDistance(2),
                g.edge(2, 3).setDistance(1),
                g.edge(4, 3).setDistance(2),
                g.edge(5, 1).setDistance(2)
        );
        g.getBaseGraph().getNodeAccess().setNode(0, 2, 2);
        g.getBaseGraph().getNodeAccess().setNode(1, 3, 2);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(3, 1, 3);
        g.getBaseGraph().getNodeAccess().setNode(4, 1, 2);
        g.getBaseGraph().getNodeAccess().setNode(5, 3, 1);
        return g;
    }

    public static GraphHopperStorage createSimpleGraph2(EncodingManager encodingManager) {
        return createSimpleGraph2(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createSimpleGraph2(GraphHopperStorage g, EncodingManager encodingManager) {
        // 5--1---2
        //     \ /
        //      0
        //     /
        //    4--6--3
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 1).setDistance(1),
                g.edge(0, 2).setDistance(1),
                g.edge(0, 4).setDistance(3),
                g.edge(1, 2).setDistance(2),
                g.edge(4, 6).setDistance(2),
                g.edge(6, 3).setDistance(2),
                g.edge(5, 1).setDistance(2)
        );
        g.getBaseGraph().getNodeAccess().setNode(0, 2, 2);
        g.getBaseGraph().getNodeAccess().setNode(1, 3, 2);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(3, 1, 4);
        g.getBaseGraph().getNodeAccess().setNode(4, 1, 2);
        g.getBaseGraph().getNodeAccess().setNode(5, 3, 1);
        g.getBaseGraph().getNodeAccess().setNode(6, 3, 3);
        return g;
    }

    public static GraphHopperStorage createSimpleGraphWithoutLatLon(EncodingManager encodingManager) {
        return createSimpleGraphWithoutLatLon(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createSimpleGraphWithoutLatLon(GraphHopperStorage g, EncodingManager encodingManager) {
        // 5--1---2
        //     \ /|
        //      0 |
        //     /  |
        //    4---3
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 1).setDistance(1),
                g.edge(0, 2).setDistance(1),
                g.edge(0, 4).setDistance(3),
                g.edge(1, 2).setDistance(2),
                g.edge(2, 3).setDistance(1),
                g.edge(4, 3).setDistance(2),
                g.edge(5, 1).setDistance(2)
        );
        return g;
    }

    public static GraphHopperStorage createDisconnectedGraph(EncodingManager encodingManager) {
        return createDisconnectedGraph(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createDisconnectedGraph(GraphHopperStorage g, EncodingManager encodingManager) {
        //   5--1---2
        //       \ /
        //        0
        //       /
        //      /
        //     / 6  9
        //    /  |  |
        //   /   7--8
        //  4---3
        //  |   |
        //  11  10
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 1).setDistance(1),
                g.edge(0, 2).setDistance(1),
                g.edge(0, 4).setDistance(3),
                g.edge(1, 2).setDistance(2),
                g.edge(4, 3).setDistance(2),
                g.edge(5, 1).setDistance(2),
                g.edge(6, 7).setDistance(1),
                g.edge(7, 8).setDistance(1),
                g.edge(8, 9).setDistance(1),
                g.edge(3, 10).setDistance(1),
                g.edge(4, 11).setDistance(1)
        );
        g.getBaseGraph().getNodeAccess().setNode(0, 2, 2);
        g.getBaseGraph().getNodeAccess().setNode(1, 3, 2);
        g.getBaseGraph().getNodeAccess().setNode(2, 3, 3);
        g.getBaseGraph().getNodeAccess().setNode(3, 1, 3);
        g.getBaseGraph().getNodeAccess().setNode(4, 1, 2);
        g.getBaseGraph().getNodeAccess().setNode(5, 3, 1);
        g.getBaseGraph().getNodeAccess().setNode(6, 1.2, 3);
        g.getBaseGraph().getNodeAccess().setNode(7, 1.1, 3);
        g.getBaseGraph().getNodeAccess().setNode(8, 1.1, 2);
        g.getBaseGraph().getNodeAccess().setNode(9, 1.2, 2);
        g.getBaseGraph().getNodeAccess().setNode(10, 0.8, 2.2);
        g.getBaseGraph().getNodeAccess().setNode(11, 0.8, 2);
        return g;
    }

    public static GraphHopperStorage createDiamondGraph(EncodingManager encodingManager) {
        return createDiamondGraph(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createDiamondGraph(GraphHopperStorage g, EncodingManager encodingManager) {
        //     4
        //   /   \
        //  2--0--3
        //   \   /
        //    \ /
        //     1
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 2).setDistance(1), // 0
                g.edge(0, 3).setDistance(3), // 1
                g.edge(1, 2).setDistance(5), // 2
                g.edge(1, 3).setDistance(3), // 3
                g.edge(2, 4).setDistance(1), // 4
                g.edge(3, 4).setDistance(1) // 5
        );
        return g;
    }

    public static GraphHopperStorage createUpDownGraph(EncodingManager encodingManager) {
        return createUpDownGraph(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createUpDownGraph(GraphHopperStorage g, EncodingManager encodingManager) {
        //      8------9
        //       \    /
        //0---1---3  5---6---7
        //       / \/
        //      2  4
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 1).setDistance(1), // 0
                g.edge(1, 3).setDistance(1), // 1
                g.edge(2, 3).setDistance(1), // 2
                g.edge(3, 4).setDistance(1), // 3
                g.edge(3, 8).setDistance(5), // 4
                g.edge(4, 5).setDistance(1), // 5
                g.edge(5, 6).setDistance(1), // 6
                g.edge(5, 9).setDistance(5), // 7
                g.edge(6, 7).setDistance(1), // 8
                g.edge(8, 9).setDistance(1) // 9
        );
        return g;
    }

    public static GraphHopperStorage createTwoWayGraph(EncodingManager encodingManager) {
        return createTwoWayGraph(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createTwoWayGraph(GraphHopperStorage g, EncodingManager encodingManager) {
        // 0<----------<-1
        // |             |
        // 2             |
        // | R           |
        // 3---4---5     |
        // |             |
        // 6-----7-------8
        // |
        // 9
        FlagEncoder carEncoder = encodingManager.getEncoder("car");
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 2).setDistance(1).setReverse(carEncoder.getAccessEnc(), false), //0
                g.edge(1, 0).setDistance(1).setReverse(carEncoder.getAccessEnc(), false), //1
                g.edge(2, 3).setDistance(1).setReverse(carEncoder.getAccessEnc(), false), //2
                g.edge(3, 4).setDistance(1).setReverse(carEncoder.getAccessEnc(), false), //3
                g.edge(4, 5).setDistance(1), //4
                g.edge(3, 6).setDistance(1), //5
                g.edge(7, 8).setDistance(1), //6
                g.edge(6, 9).setDistance(1), //7
                g.edge(6, 7).setDistance(10), //8
                g.edge(8, 1).setDistance(1), //9
                g.edge(8, 1).setDistance(1), //10
                g.edge(1, 0).setDistance(1).setReverse(carEncoder.getAccessEnc(), false) //11
        );
        return g;
    }

    public static GraphHopperStorage createUpdatedGraph(EncodingManager encodingManager) {
        return createUpdatedGraph(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createUpdatedGraph(GraphHopperStorage g, EncodingManager encodingManager) {
        //     2---3
        //    / \
        //   1  |
        //    \ |
        //     0
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 1).setDistance(5), //0
                g.edge(0, 2).setDistance(1), //1
                g.edge(1, 2).setDistance(1), //2
                g.edge(2, 3).setDistance(1) //3
        );
        return g;
    }

    public static GraphHopperStorage createDirectedGraph(EncodingManager encodingManager) {
        return createDirectedGraph(createGHStorage(encodingManager), encodingManager);
    }

    public static GraphHopperStorage createDirectedGraph(GraphHopperStorage g, EncodingManager encodingManager) {
        // 0----->1<-----2
        // |     / \     |
        // |-<--/   \-->-|
        GHUtility.setSpeed(60, 60, encodingManager.getEncoder("car"),
                g.edge(0, 1).setDistance(1), //0
                g.edge(1, 0).setDistance(5), //1
                g.edge(1, 2).setDistance(6), //2
                g.edge(2, 1).setDistance(2) //3
        );
        return g;
    }
}
