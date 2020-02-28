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
package org.heigit.ors.partitioning;


import com.carrotsearch.hppc.DoubleArrayList;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntLongHashMap;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.carrotsearch.hppc.cursors.IntLongCursor;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.Storable;
import java.nio.ByteBuffer;

/**
 * Stores distances of bordernodes in a cell
 *
 * @author Hendrik Leuschner
 */
public class BorderNodeDistanceStorage implements Storable<BorderNodeDistanceStorage> {

    private final DataAccess borderNodes;
    private int BYTECOUNT;
    private int BORDERNODEINDEXOFFSET;
    private int nodeCount;
    private int borderNodeCount;
    private int necessaryCapacity = 0;
    private long borderNodePointer;
    private IsochroneNodeStorage isochroneNodeStorage;
    private IntLongHashMap borderNodeToPointerMap;
    private Weighting weighting;


    public BorderNodeDistanceStorage(GraphHopperStorage graph, Directory dir, Weighting weighting, IsochroneNodeStorage isochroneNodeStorage) {
        final String name = AbstractWeighting.weightingToFileName(weighting);
        this.isochroneNodeStorage = isochroneNodeStorage;
        borderNodes = dir.find("bordernodes_" + name);
        this.weighting = weighting;
        BYTECOUNT = 12; //adj bordernode id (int) and distance (double)
        nodeCount = graph.getNodes();
    }


    @Override
    public boolean loadExisting() {
        if (borderNodes.loadExisting()) {
            borderNodeCount = borderNodes.getHeader(0);
            BORDERNODEINDEXOFFSET = borderNodeCount * BYTECOUNT;
            borderNodePointer = BORDERNODEINDEXOFFSET;
            borderNodeToPointerMap = new IntLongHashMap(borderNodeCount);
            fillBorderNodeToPointerMap();
            return true;
        }
        return false;
    }

    public void init() {
        borderNodes.create(1000);
        getNumBorderNodes();
        borderNodes.ensureCapacity(borderNodeCount * BYTECOUNT + necessaryCapacity * BYTECOUNT + borderNodeCount * 4);
        borderNodes.setHeader(0, borderNodeCount);
        BORDERNODEINDEXOFFSET = borderNodeCount * BYTECOUNT;
        borderNodePointer = BORDERNODEINDEXOFFSET;
        borderNodeToPointerMap = new IntLongHashMap();
    }

    private void getNumBorderNodes(){
        //Count number of border nodes and count number of border nodes per cell to calculate capacity later
        int count = 0;
        IntIntHashMap cellBNodes = new IntIntHashMap(isochroneNodeStorage.getCellIds().size());
        for(int node = 0; node < nodeCount; node++) {
            if (isochroneNodeStorage.getBorderness(node)) {
                count++;
                cellBNodes.putOrAdd(isochroneNodeStorage.getCellId(node), 1, 1);
            }
        }
        for(IntIntCursor cell : cellBNodes){
            necessaryCapacity += cell.value * cell.value;
        }
        borderNodeCount = count;
    }

    public synchronized void storeBorderNodeDistanceSet(int nodeId, BorderNodeDistanceSet bnds){
        if(bnds.getAdjBorderNodeDistances().length != bnds.getAdjBorderNodeIds().length)
            throw new IllegalArgumentException("Corrupted distance set");
//        borderNodes.ensureCapacity(borderNodePointer + (long)(bnds.getAdjBorderNodeIds().length * BYTECOUNT));
        borderNodeToPointerMap.put(nodeId, borderNodePointer);

        for(int i = 0; i < bnds.getAdjBorderNodeIds().length; i++){
            borderNodes.setInt(borderNodePointer, bnds.adjBorderNodeIds[i]);
            borderNodePointer += 4;
            borderNodes.setBytes(borderNodePointer, toByteArray(bnds.adjBorderNodeDistances[i]), 8);
            borderNodePointer += 8;
        }
        //Add trailing -1
        borderNodes.setInt(borderNodePointer, -1);
        borderNodePointer += 4;
    }

    public BorderNodeDistanceSet getBorderNodeDistanceSet(int nodeId){
        long pointer = borderNodeToPointerMap.get(nodeId);
        int currentNode = borderNodes.getInt(pointer);
        IntArrayList ids = new IntArrayList(5);
        DoubleArrayList distances = new DoubleArrayList(5);
        while(currentNode != -1){
            byte[] buffer = new byte[8];
            ids.add(currentNode);
            pointer += 4;
            borderNodes.getBytes(pointer, buffer, 8);
            distances.add(toDouble(buffer));
            pointer += 8;
            currentNode = borderNodes.getInt(pointer);
        }
        return new BorderNodeDistanceSet(ids.toArray(), distances.toArray());
    }

    public void storeBorderNodeToPointerMap(){
        long listPointer = 0;
        long nodePointer;
        //Store the number of contours (= num cells + num supercells)
        for(IntLongCursor borderNode : borderNodeToPointerMap){
            borderNodes.setInt(listPointer, borderNode.key);
            listPointer = listPointer + 4;
            nodePointer = borderNode.value;
            byte b0 = (byte)((nodePointer >> 56));
            byte b1 = (byte)((nodePointer >> 48));
            byte b2 = (byte)((nodePointer >> 40));
            byte b3 = (byte)((nodePointer >> 32));
            byte b4 = (byte)((nodePointer >> 24));
            byte b5 = (byte)((nodePointer >> 16));
            byte b6 = (byte)((nodePointer >> 8));
            byte b7 = (byte)(nodePointer);
            borderNodes.setBytes(listPointer, new byte[] {
                    b0, b1, b2, b3, b4, b5, b6, b7
            }, 8);
            listPointer = listPointer + 8;
        }
    }


    private void fillBorderNodeToPointerMap(){
        byte[] buffer = new byte[8];
        long listPointer = 0;
        for(int i = 0; i < borderNodeCount; i ++){
            int borderNode = borderNodes.getInt(listPointer);
            listPointer = listPointer + 4;

            long nodePointer = 0;
            borderNodes.getBytes(listPointer, buffer, 8);
            nodePointer += (buffer[0] & 0x000000FF) << 56;
            nodePointer += (buffer[1] & 0x000000FF) << 48;
            nodePointer += (buffer[2] & 0x000000FF) << 40;
            nodePointer += (buffer[3] & 0x000000FF) << 32;
            nodePointer += (buffer[4] & 0x000000FF) << 24;
            nodePointer += (buffer[5] & 0x000000FF) << 16;
            nodePointer += (buffer[6] & 0x000000FF) << 8;
            nodePointer += (buffer[7] & 0x000000FF);

            listPointer = listPointer + (long)8;
            borderNodeToPointerMap.put(borderNode, nodePointer);
        }
    }

    @Override
    public BorderNodeDistanceStorage create(long byteCount) {
        throw new IllegalStateException("Do not call BorderNodeDistanceStorage.create directly");
    }

    @Override
    public void flush() {
        borderNodes.flush();
    }

    @Override
    public void close() {
        borderNodes.close();

    }

    private byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    private double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
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


}
