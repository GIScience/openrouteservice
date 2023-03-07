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
package org.heigit.ors.fastisochrones.storage;

import com.carrotsearch.hppc.DoubleArrayList;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntLongHashMap;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.carrotsearch.hppc.cursors.IntLongCursor;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Storable;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.util.FileUtility;

import static org.heigit.ors.fastisochrones.storage.ByteConversion.*;

/**
 * Stores distances of bordernodes in a cell.
 *
 * @author Hendrik Leuschner
 */
public class BorderNodeDistanceStorage implements Storable<BorderNodeDistanceStorage> {
    private final DataAccess borderNodes;
    private final int byteCount;
    private int borderNodeIndexOffset;
    private final int nodeCount;
    private int borderNodeCount;
    private int necessaryCapacity = 0;
    private long borderNodePointer;
    private final IsochroneNodeStorage isochroneNodeStorage;
    private IntLongHashMap borderNodeToPointerMap;
    private final Weighting weighting;

    public BorderNodeDistanceStorage(Directory dir, Weighting weighting, IsochroneNodeStorage isochroneNodeStorage, int nodeCount) {
        final String name = FileUtility.weightingToFileName(weighting);
        this.isochroneNodeStorage = isochroneNodeStorage;
        borderNodes = dir.find("bordernodes_" + name);
        this.weighting = weighting;
        byteCount = 12; //adj bordernode id (int 4B) and distance (double 8B)
        this.nodeCount = nodeCount;
    }

    public boolean loadExisting() {
        if (borderNodes.loadExisting()) {
            borderNodeCount = borderNodes.getHeader(0);
            borderNodeIndexOffset = borderNodeCount * byteCount;
            borderNodePointer = borderNodeIndexOffset;
            borderNodeToPointerMap = new IntLongHashMap(borderNodeCount);
            fillBorderNodeToPointerMap();
            return true;
        }
        return false;
    }

    public void init() {
        borderNodes.create(1000);
        getNumBorderNodes();
        borderNodes.ensureCapacity((long) borderNodeCount * byteCount + necessaryCapacity * byteCount + borderNodeCount * 4);
        borderNodes.setHeader(0, borderNodeCount);
        borderNodeIndexOffset = borderNodeCount * byteCount;
        borderNodePointer = borderNodeIndexOffset;
        borderNodeToPointerMap = new IntLongHashMap();
    }

    private void getNumBorderNodes() {
        //Count number of border nodes and count number of border nodes per cell to calculate capacity later
        int count = 0;
        IntIntHashMap cellBNodes = new IntIntHashMap(isochroneNodeStorage.getCellIds().size());
        for (int node = 0; node < nodeCount; node++) {
            if (isochroneNodeStorage.getBorderness(node)) {
                count++;
                cellBNodes.putOrAdd(isochroneNodeStorage.getCellId(node), 1, 1);
            }
        }
        for (IntIntCursor cell : cellBNodes) {
            necessaryCapacity += cell.value * cell.value;
        }
        borderNodeCount = count;
    }

    public synchronized void storeBorderNodeDistanceSet(int nodeId, BorderNodeDistanceSet bnds) {
        if (bnds.getAdjBorderNodeDistances().length != bnds.getAdjBorderNodeIds().length)
            throw new IllegalArgumentException("Corrupted distance set");
        borderNodeToPointerMap.put(nodeId, borderNodePointer);

        for (int i = 0; i < bnds.getAdjBorderNodeIds().length; i++) {
            borderNodes.setInt(borderNodePointer, bnds.adjBorderNodeIds[i]);
            borderNodePointer += 4;
            borderNodes.setBytes(borderNodePointer, doubleToByteArray(bnds.adjBorderNodeDistances[i]), 8);
            borderNodePointer += 8;
        }
        //Add trailing -1
        borderNodes.setInt(borderNodePointer, -1);
        borderNodePointer += 4;
    }

    public BorderNodeDistanceSet getBorderNodeDistanceSet(int nodeId) {
        long pointer = borderNodeToPointerMap.get(nodeId);
        int currentNode = borderNodes.getInt(pointer);
        IntArrayList ids = new IntArrayList(5);
        DoubleArrayList distances = new DoubleArrayList(5);
        while (currentNode != -1) {
            byte[] buffer = new byte[8];
            ids.add(currentNode);
            pointer += 4;
            borderNodes.getBytes(pointer, buffer, 8);
            distances.add(byteArrayToDouble(buffer));
            pointer += 8;
            currentNode = borderNodes.getInt(pointer);
        }
        return new BorderNodeDistanceSet(ids.toArray(), distances.toArray());
    }

    public void storeBorderNodeToPointerMap() {
        long listPointer = 0;
        long nodePointer;
        //Store the number of contours (= num cells + num supercells)
        for (IntLongCursor borderNode : borderNodeToPointerMap) {
            borderNodes.setInt(listPointer, borderNode.key);
            listPointer = listPointer + 4;
            nodePointer = borderNode.value;
            borderNodes.setBytes(listPointer, longToByteArray(nodePointer), 8);
            listPointer = listPointer + 8;
        }
    }

    private void fillBorderNodeToPointerMap() {
        byte[] buffer = new byte[8];
        long listPointer = 0;
        for (int i = 0; i < borderNodeCount; i++) {
            int borderNode = borderNodes.getInt(listPointer);
            listPointer = listPointer + 4;

            borderNodes.getBytes(listPointer, buffer, 8);
            long nodePointer = byteArrayToLong(buffer);
            listPointer = listPointer + (long) 8;
            borderNodeToPointerMap.put(borderNode, nodePointer);
        }
    }

    public BorderNodeDistanceStorage create(long byteCount) {
        throw new IllegalStateException("Do not call BorderNodeDistanceStorage.create directly");
    }

    public void flush() {
        borderNodes.flush();
    }

    @Override
    public void close() {
        borderNodes.close();
    }

    @Override
    public boolean isClosed() {
        return borderNodes.isClosed();
    }

    public long getCapacity() {
        return borderNodes.getCapacity();
    }

    public Weighting getWeighting() {
        return weighting;
    }

    public boolean hasWeighting(Weighting weighting) {
        return getWeighting().getName() != null
                && getWeighting().getName().equals(weighting.getName())
                && getWeighting().getFlagEncoder().toString() != null
                && getWeighting().getFlagEncoder().toString().equals(weighting.getFlagEncoder().toString());
    }
}
