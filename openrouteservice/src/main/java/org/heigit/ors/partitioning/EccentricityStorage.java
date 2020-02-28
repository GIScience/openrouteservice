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

/**
 * Stores eccentricities of cell border nodes for fast isochrones. eccentricities are weighting dependent, therefore they are stored separately
 *
 * @author Hendrik Leuschner
 */
public class EccentricityStorage implements Storable<EccentricityStorage> {

    private final DataAccess eccentricities;
    private int ECCENTRICITYBYTES;
    private int FULLYREACHABLEPOSITION;
    private int ECCENTRICITYPOSITION;
    private int BORDERNODEINDEXOFFSET;
    private int borderNodePointer;
    private int nodeCount;

    private Weighting weighting;
    private IntLongHashMap borderNodeToPointerMap;
    private int borderNodeCount;
    private IsochroneNodeStorage isochroneNodeStorage;

    public EccentricityStorage(GraphHopperStorage graph, Directory dir, Weighting weighting, IsochroneNodeStorage isochroneNodeStorage) {

        final String name = AbstractWeighting.weightingToFileName(weighting);
        eccentricities = dir.find("eccentricities_" + name);
        this.weighting = weighting;
        this.isochroneNodeStorage = isochroneNodeStorage;

        nodeCount = graph.getNodes();
        //  1 int per eccentricity value
        this.ECCENTRICITYBYTES = 8;
        this.FULLYREACHABLEPOSITION = 0;
        this.ECCENTRICITYPOSITION = FULLYREACHABLEPOSITION + 4;

    }


    @Override
    public boolean loadExisting() {
        if (eccentricities.loadExisting()) {
            borderNodeCount = eccentricities.getHeader(0);
            BORDERNODEINDEXOFFSET = borderNodeCount * 8;
            borderNodePointer = BORDERNODEINDEXOFFSET;
            borderNodeToPointerMap = new IntLongHashMap(borderNodeCount);
            fillBorderNodeToPointerMap();
            return true;
        }
        return false;
    }

    public void init() {
        eccentricities.create(1000);
        getNumBorderNodes();
        eccentricities.setHeader(0, borderNodeCount);
        BORDERNODEINDEXOFFSET = borderNodeCount * 12;
        borderNodePointer = BORDERNODEINDEXOFFSET;
        borderNodeToPointerMap = new IntLongHashMap();
        fillMap();
        eccentricities.ensureCapacity(BORDERNODEINDEXOFFSET + borderNodeCount * ECCENTRICITYBYTES);
    }

    private void getNumBorderNodes(){
        int count = 0;
        for(int node = 0; node < nodeCount; node++) {
            if (isochroneNodeStorage.getBorderness(node)) {
                count++;
            }
        }
        borderNodeCount = count;
    }

    private void fillMap(){
        for(int node = 0; node < nodeCount; node++) {
            if (isochroneNodeStorage.getBorderness(node)) {
                borderNodeToPointerMap.put(node, borderNodePointer);
                borderNodePointer += (long) ECCENTRICITYBYTES;
            }
        }
    }


    public void setEccentricity(int node, double eccentricity){

        eccentricities.setInt(borderNodeToPointerMap.get(node) + ECCENTRICITYPOSITION, (int)Math.ceil(eccentricity));
    }

    public int getEccentricity(int node){
        return eccentricities.getInt(borderNodeToPointerMap.get(node) + ECCENTRICITYPOSITION);
    }

    public void setFullyReachable(int node, boolean isFullyReachable){
        if(isFullyReachable)
            eccentricities.setInt(borderNodeToPointerMap.get(node) + FULLYREACHABLEPOSITION, 1);
        else
            eccentricities.setInt(borderNodeToPointerMap.get(node) + FULLYREACHABLEPOSITION, 0);
    }

    public boolean getFullyReachable(int node){
        int isFullyReachable = eccentricities.getInt(borderNodeToPointerMap.get(node) + FULLYREACHABLEPOSITION);

        return isFullyReachable == 1;
    }
    public void storeBorderNodeToPointerMap(){
        long listPointer = 0;
        long nodePointer;
        //Store the number of contours (= num cells + num supercells)
        for(IntLongCursor borderNode : borderNodeToPointerMap){
            eccentricities.setInt(listPointer, borderNode.key);
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
            eccentricities.setBytes(listPointer, new byte[] {
                    b0, b1, b2, b3, b4, b5, b6, b7
            }, 8);
            listPointer = listPointer + 8;
        }
    }


    private void fillBorderNodeToPointerMap(){
        byte[] buffer = new byte[8];
        long listPointer = 0;
        for(int i = 0; i < borderNodeCount; i ++){
            int borderNode = eccentricities.getInt(listPointer);
            listPointer = listPointer + 4;

            long nodePointer = 0;
            eccentricities.getBytes(listPointer, buffer, 8);
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

    public Weighting getWeighting() {
        return weighting;
    }
}
