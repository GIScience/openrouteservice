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


import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntSet;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.Storable;

/**
 * Stores a mapping of nodeId->cellId/borderness
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class IsochroneNodeStorage implements Storable<IsochroneNodeStorage> {

    private final DataAccess isochroneNodes;
    private int CELLBYTES;
    private int nodeCount;
    private IntSet cellIdsSet = new IntHashSet();

    public IsochroneNodeStorage(GraphHopperStorage graph, Directory dir){//, Weighting weighting) {
//        final String name = AbstractWeighting.weightingToFileName(weighting);

//        isochroneNodes = dir.find("isochronenodes_" + name);
        isochroneNodes = dir.find("isochronenodes");
        nodeCount = graph.getNodes();
        //  1 byte for isBordernode,
        // 4 bytes per node for its cell id.
         //TODO CAN BE 3 probably
        // Maximum cell count of ~16M
        this.CELLBYTES = 5;
    }


    @Override
    public boolean loadExisting() {
        if (isochroneNodes.loadExisting()) {
            for(int node = 0; node < nodeCount; node ++)
                cellIdsSet.add(getCellId(node));
            return true;
        }
        return false;
    }

    public void setBorderness(boolean[] borderness){
        if (nodeCount != borderness.length)
            throw new IllegalStateException("Nodecount and borderness array do not match");
        isochroneNodes.ensureCapacity((long) CELLBYTES * nodeCount);
        for (int node = 0; node < borderness.length; node++){
            if (borderness[node])
                isochroneNodes.setBytes(node * CELLBYTES, new byte[] {(byte) 1}, 1);
            else
                isochroneNodes.setBytes(node * CELLBYTES, new byte[] {(byte) 0}, 1);
        }

    }

    public boolean getBorderness(int node){
        byte[] buffer = new byte[1];
        isochroneNodes.getBytes(node * CELLBYTES, buffer, 1);

        return buffer[0] == 1;
    }

    public void setCellId(int[] cellIds){
        if (nodeCount != cellIds.length)
            throw new IllegalStateException("Nodecount and cellIds array do not match");
        isochroneNodes.create(1000);
        isochroneNodes.ensureCapacity((long)CELLBYTES * nodeCount);
        int cellId;
        for (int node = 0; node < cellIds.length; node++){
            cellId = cellIds[node];
            cellIdsSet.add(cellId);
            byte b0 = (byte)((cellId >> 24));
            byte b1 = (byte)((cellId >> 16));
            byte b2 = (byte)((cellId >> 8));
            byte b3 = (byte)(cellId);
            isochroneNodes.setBytes(node * CELLBYTES + 1, new byte[] {
                    b0, b1, b2, b3,
            }, 4);

        }

    }

    public int getCellId(int node){
        //Not 100% sure if this byte bit shift does what I want it to do or if I need to promote to int first
        byte[] buffer = new byte[4];
        int cellId = 0;
        isochroneNodes.getBytes(node * CELLBYTES + 1, buffer, 4);
        cellId += (buffer[0] & 0x000000FF) << 24;
        cellId += (buffer[1] & 0x000000FF) << 16;
        cellId += (buffer[2] & 0x000000FF) << 8;
        cellId += (buffer[3] & 0x000000FF);

        return cellId;
    }

    public IntSet getCellIds(){
        return cellIdsSet;
    }


    @Override
    public IsochroneNodeStorage create(long byteCount) {
        throw new IllegalStateException("Do not call LandmarkStore.create directly");
    }

    @Override
    public void flush() {
        isochroneNodes.flush();
    }

    @Override
    public void close() {
        isochroneNodes.close();

    }

    @Override
    public boolean isClosed() {
        return isochroneNodes.isClosed();
    }

    public long getCapacity() {
        return isochroneNodes.getCapacity();
    }


}
