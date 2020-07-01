/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.heigit.ors.fastisochrones.partitioning.storage;

import com.carrotsearch.hppc.IntLongHashMap;
import com.carrotsearch.hppc.cursors.IntLongCursor;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.Storable;

import static org.heigit.ors.fastisochrones.partitioning.storage.ByteConversion.*;

/**
 * Stores eccentricities of cell border nodes for fast isochrones. Eccentricities are weighting dependent, therefore they are stored separately from cells.
 *
 * @author Hendrik Leuschner
 */
public class EccentricityStorage implements Storable<EccentricityStorage> {
    private final DataAccess eccentricities;
    private final int eccentricityBytes;
    private final int fullyReachablePosition;
    private final int eccentricityPosition;
    private final int nodeCount;
    private final Weighting weighting;
    private final IsochroneNodeStorage isochroneNodeStorage;
    private int borderNodeIndexOffset;
    private int borderNodePointer;
    private IntLongHashMap borderNodeToPointerMap;
    private int borderNodeCount;

    /**
     * Instantiates a new Eccentricity storage.
     *
     * @param graph                the graph
     * @param dir                  the dir
     * @param weighting            the weighting
     * @param isochroneNodeStorage the isochrone node storage
     */
    public EccentricityStorage(GraphHopperStorage graph, Directory dir, Weighting weighting, IsochroneNodeStorage isochroneNodeStorage) {
        final String name = AbstractWeighting.weightingToFileName(weighting);
        eccentricities = dir.find("eccentricities_" + name);
        this.weighting = weighting;
        this.isochroneNodeStorage = isochroneNodeStorage;
        nodeCount = graph.getNodes();
        //  1 int per eccentricity value
        this.eccentricityBytes = 8;
        this.fullyReachablePosition = 0;
        this.eccentricityPosition = fullyReachablePosition + 4;
    }

    @Override
    public boolean loadExisting() {
        if (eccentricities.loadExisting()) {
            borderNodeCount = eccentricities.getHeader(0);
            borderNodeIndexOffset = borderNodeCount * 8;
            borderNodePointer = borderNodeIndexOffset;
            borderNodeToPointerMap = new IntLongHashMap(borderNodeCount);
            fillBorderNodeToPointerMap();
            return true;
        }
        return false;
    }

    /**
     * Init.
     */
    public void init() {
        eccentricities.create(1000);
        getNumBorderNodes();
        eccentricities.setHeader(0, borderNodeCount);
        borderNodeIndexOffset = borderNodeCount * 12;
        borderNodePointer = borderNodeIndexOffset;
        borderNodeToPointerMap = new IntLongHashMap();
        fillMap();
        eccentricities.ensureCapacity((long) borderNodeIndexOffset + borderNodeCount * eccentricityBytes);
    }

    private void getNumBorderNodes() {
        int count = 0;
        for (int node = 0; node < nodeCount; node++) {
            if (isochroneNodeStorage.getBorderness(node)) {
                count++;
            }
        }
        borderNodeCount = count;
    }

    private void fillMap() {
        for (int node = 0; node < nodeCount; node++) {
            if (isochroneNodeStorage.getBorderness(node)) {
                borderNodeToPointerMap.put(node, borderNodePointer);
                borderNodePointer += (long) eccentricityBytes;
            }
        }
    }

    /**
     * Sets eccentricity.
     *
     * @param node         the node
     * @param eccentricity the eccentricity
     */
    public void setEccentricity(int node, double eccentricity) {
        eccentricities.setInt(borderNodeToPointerMap.get(node) + eccentricityPosition, (int) Math.ceil(eccentricity));
    }

    /**
     * Gets eccentricity.
     *
     * @param node the node
     * @return the eccentricity
     */
    public int getEccentricity(int node) {
        return eccentricities.getInt(borderNodeToPointerMap.get(node) + eccentricityPosition);
    }

    /**
     * Sets fully reachable.
     *
     * @param node             the node
     * @param isFullyReachable the is fully reachable
     */
    public void setFullyReachable(int node, boolean isFullyReachable) {
        if (isFullyReachable)
            eccentricities.setInt(borderNodeToPointerMap.get(node) + fullyReachablePosition, 1);
        else
            eccentricities.setInt(borderNodeToPointerMap.get(node) + fullyReachablePosition, 0);
    }

    /**
     * Gets fully reachable.
     *
     * @param node the node
     * @return the fully reachable
     */
    public boolean getFullyReachable(int node) {
        int isFullyReachable = eccentricities.getInt(borderNodeToPointerMap.get(node) + fullyReachablePosition);
        return isFullyReachable == 1;
    }

    /**
     * Store border node to pointer map.
     */
    public void storeBorderNodeToPointerMap() {
        long listPointer = 0;
        long nodePointer;
        //Store the number of contours (= num cells + num supercells)
        for (IntLongCursor borderNode : borderNodeToPointerMap) {
            eccentricities.setInt(listPointer, borderNode.key);
            listPointer = listPointer + 4;
            nodePointer = borderNode.value;
            eccentricities.setBytes(listPointer, longToByteArray(nodePointer), 8);
            listPointer = listPointer + 8;
        }
    }

    private void fillBorderNodeToPointerMap() {
        byte[] buffer = new byte[8];
        long listPointer = 0;
        for (int i = 0; i < borderNodeCount; i++) {
            int borderNode = eccentricities.getInt(listPointer);
            listPointer = listPointer + 4;
            eccentricities.getBytes(listPointer, buffer, 8);
            long nodePointer = byteArrayToLong(buffer);
            listPointer = listPointer + (long) 8;
            borderNodeToPointerMap.put(borderNode, nodePointer);
        }
    }

    @Override
    public EccentricityStorage create(long byteCount) {
        throw new IllegalStateException("Do not call EccentricityStorage.create directly");
    }

    @Override
    public void flush() {
        eccentricities.flush();
    }

    @Override
    public void close() {
        eccentricities.close();
    }

    @Override
    public boolean isClosed() {
        return eccentricities.isClosed();
    }

    public long getCapacity() {
        return eccentricities.getCapacity();
    }

    /**
     * Gets weighting.
     *
     * @return the weighting
     */
    public Weighting getWeighting() {
        return weighting;
    }
}
