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

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntSet;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Storable;

import static org.heigit.ors.fastisochrones.storage.ByteConversion.byteArrayToInteger;
import static org.heigit.ors.fastisochrones.storage.ByteConversion.intToByteArray;

/**
 * Storage that maps nodeIds to their respective cells and borderness.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class IsochroneNodeStorage implements Storable<IsochroneNodeStorage> {
    private final DataAccess isochroneNodes;
    private final int cellBytes;
    private final int nodeCount;
    private final IntSet cellIdsSet = new IntHashSet();

    public IsochroneNodeStorage(int nodeCount, Directory dir) {
        isochroneNodes = dir.find("isochronenodes");
        this.nodeCount = nodeCount;
        // 5 bytes per node for its cell id.
        // 1 byte for isBordernode,
        // Maximum cell id = Integer.MAX_VALUE
        // Borderness of nodes is stored in a block after cellIds block. As it is one bit per node, it is condensed into blocks of 8 node information per byte.
        this.cellBytes = 5;
    }

    public boolean loadExisting() {
        if (isochroneNodes.loadExisting()) {
            for (int node = 0; node < nodeCount; node++)
                cellIdsSet.add(getCellId(node));
            return true;
        }
        return false;
    }

    public void setBorderness(boolean[] borderness) {
        if (nodeCount != borderness.length)
            throw new IllegalStateException("Nodecount and borderness array do not match");
        isochroneNodes.ensureCapacity((long) cellBytes * nodeCount);
        for (int node = 0; node < borderness.length; node++) {
            if (borderness[node])
                isochroneNodes.setBytes((long) node * cellBytes, new byte[]{(byte) 1}, 1);
            else
                isochroneNodes.setBytes((long) node * cellBytes, new byte[]{(byte) 0}, 1);
        }
    }

    public boolean getBorderness(int node) {
        byte[] buffer = new byte[1];
        isochroneNodes.getBytes((long) node * cellBytes, buffer, 1);
        return buffer[0] == 1;
    }

    public int getCellId(int node) {
        byte[] buffer = new byte[4];
        isochroneNodes.getBytes((long) node * cellBytes + 1, buffer, 4);
        return byteArrayToInteger(buffer);
    }

    public IntSet getCellIds() {
        return cellIdsSet;
    }

    public void setCellIds(int[] cellIds) {
        if (nodeCount != cellIds.length)
            throw new IllegalStateException("Nodecount and cellIds array do not match");
        isochroneNodes.create(1000);
        isochroneNodes.ensureCapacity((long) cellBytes * nodeCount);
        for (int node = 0; node < cellIds.length; node++) {
            int cellId = cellIds[node];
            cellIdsSet.add(cellId);
            isochroneNodes.setBytes((long) node * cellBytes + 1, intToByteArray(cellId), 4);
        }
    }

    public IsochroneNodeStorage create(long byteCount) {
        throw new IllegalStateException("Do not call IsochroneNodeStorage.create directly");
    }

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
