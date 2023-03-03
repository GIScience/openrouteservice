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

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Storable;
import com.graphhopper.util.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.isSupercellsEnabled;
import static org.heigit.ors.fastisochrones.storage.ByteConversion.byteArrayToLong;
import static org.heigit.ors.fastisochrones.storage.ByteConversion.longToByteArray;

/**
 * Stores nodes ordered by cell and contours of cells.
 *
 * @author Hendrik Leuschner
 */
public class CellStorage implements Storable<CellStorage> {
    private final DataAccess cells;
    private final int byteCount;
    private int nodeIndexOffset;
    private int contourIndexOffset;
    private final int nodeCount;
    private long cellContourPointer;
    private final IsochroneNodeStorage isochroneNodeStorage;
    private IntLongMap cellIdToNodesPointerMap;
    private IntLongMap cellIdToContourPointerMap;
    private IntIntMap cellIdToSuperCellMap = new IntIntHashMap();
    private IntObjectMap<IntHashSet> superCellIdToCellsMap = new IntObjectHashMap<>();

    /**
     * Instantiates a new Cell storage.
     *
     * @param dir                  the dir
     * @param isochroneNodeStorage the isochrone node storage
     */
    public CellStorage(int nodeCount, Directory dir, IsochroneNodeStorage isochroneNodeStorage) {
        this.isochroneNodeStorage = isochroneNodeStorage;
        cells = dir.find("cells");
        byteCount = 4;
        this.nodeCount = nodeCount;
    }

    public boolean loadExisting() {
        if (cells.loadExisting()) {
            int cellCount = cells.getHeader(0);
            nodeIndexOffset = cellCount * 12;
            contourIndexOffset = 2 * cellCount * 18;
            cellIdToNodesPointerMap = new IntLongHashMap(cellCount);
            cellIdToContourPointerMap = new IntLongHashMap(cellCount);
            fillCellIdToNodesPointerMap();
            fillCellIdToContourPointerMap();
            if (isSupercellsEnabled()) {
                fillSuperCellMap();
                fillCellIdToSuperCellMap();
            }
            return true;
        }
        return false;
    }

    /**
     * Init.
     */
    public void init() {
        cells.create(1000);
        int cellCount = isochroneNodeStorage.getCellIds().size();
        cellIdToNodesPointerMap = new IntLongHashMap(cellCount);
        cellIdToContourPointerMap = new IntLongHashMap(cellCount);
        cellIdToSuperCellMap = new IntIntHashMap(cellCount);
    }

    /**
     * Iterate over all nodes in graph and create a mapping of cells -> nodeIds.
     * Create a mapping of cellId -> pointer, as the NodeId count per cell is different and the storage irregular.
     * Store both maps in the storage.
     */
    public void calcCellNodesMap() {
        IntObjectMap<IntHashSet> cellIdToNodesMap = new IntObjectHashMap<>(isochroneNodeStorage.getCellIds().size());
        //Calc a hashmap of the cells
        for (int node = 0; node < nodeCount; node++) {
            int cellId = isochroneNodeStorage.getCellId(node);
            if (!cellIdToNodesMap.containsKey(cellId))
                cellIdToNodesMap.put(cellId, new IntHashSet());
            cellIdToNodesMap.get(cellId).add(node);
        }
        int cellCount = cellIdToNodesMap.size();
        cells.setHeader(0, cellCount);
        // 2 12-byte pointer sets for each cellId
        // Store pointers in front that point to where the nodes for each cell start
        nodeIndexOffset = cellCount * 12;
        //There are more contours than cells because of supercell contours
        contourIndexOffset = 2 * cellCount * 18;
        long nodePointer = contourIndexOffset;

        //Put all the cell nodes in the storage
        for (IntCursor cellId : cellIdToNodesMap.keys()) {
            cells.ensureCapacity(nodePointer + (long) (cellIdToNodesMap.get(cellId.value).size() + 1) * byteCount);
            cellIdToNodesPointerMap.put(cellId.value, nodePointer);
            for (IntCursor nodeId : cellIdToNodesMap.get(cellId.value)) {
                cells.setInt(nodePointer, nodeId.value);
                nodePointer = nodePointer + (long) byteCount;
            }
            //Add a trailing -1 so we know when to stop
            cells.setInt(nodePointer, -1);
            nodePointer = nodePointer + (long) byteCount;
        }
        //Set the contour node pointer to the end of the nodes part
        cellContourPointer = nodePointer;

        //Put the cellId to pointer map into the storage
        //Layout: [cellId (4B), pointer to nodes (8B)]
        long listPointer = 0;
        for (IntCursor cellId : cellIdToNodesPointerMap.keys()) {
            cells.setInt(listPointer, cellId.value);
            listPointer = listPointer + (long) byteCount;
            nodePointer = cellIdToNodesPointerMap.get(cellId.value);
            cells.setBytes(listPointer, longToByteArray(nodePointer), 8);
            listPointer = listPointer + (long) byteCount + (long) byteCount;
        }
    }

    /**
     * Get nodes of cell int hash set.
     *
     * @param cellId the cell id
     * @return the int hash set
     */
    public IntHashSet getNodesOfCell(int cellId) {
        if (cellIdToNodesPointerMap.isEmpty())
            throw new IllegalStateException("CellStorage not filled yet. Was calcCellNodesMap run?");
        long nodePointer = cellIdToNodesPointerMap.get(cellId);
        int currentNode = cells.getInt(nodePointer);
        IntHashSet nodeIds = new IntHashSet();
        while (currentNode != -1) {
            nodeIds.add(currentNode);
            nodePointer = nodePointer + (long) byteCount;
            currentNode = cells.getInt(nodePointer);
        }
        return nodeIds;
    }

    /**
     * Sets cell contour order.
     *
     * @param cellId     the cell id
     * @param latitudes  the latitudes
     * @param longitudes the longitudes
     */
    public void setCellContourOrder(int cellId, List<Double> latitudes, List<Double> longitudes) {
        if (latitudes.size() != longitudes.size())
            throw new IllegalStateException("lat and lon must be same size");
        cellIdToContourPointerMap.put(cellId, cellContourPointer);
        cells.ensureCapacity(cellContourPointer + (long) 8 * (latitudes.size() + 1));
        for (int i = 0; i < latitudes.size(); i++) {
            cells.setInt(cellContourPointer, Helper.degreeToInt(latitudes.get(i)));
            cellContourPointer = cellContourPointer + (long) byteCount;
            cells.setInt(cellContourPointer, Helper.degreeToInt(longitudes.get(i)));
            cellContourPointer = cellContourPointer + (long) byteCount;
        }
        //Add a trailing int max value so we know when to stop
        cells.setInt(cellContourPointer, Integer.MAX_VALUE);
        cellContourPointer = cellContourPointer + (long) byteCount;
        cells.setInt(cellContourPointer, Integer.MAX_VALUE);
        cellContourPointer = cellContourPointer + (long) byteCount;
    }

    /**
     * Get cell contour order list.
     *
     * @param cellId the cell id
     * @return the list
     */
    public List<Double> getCellContourOrder(int cellId) {
        if (cellIdToContourPointerMap.isEmpty())
            throw new IllegalStateException("Cell contours not stored yet.");
        List<Double> order = new ArrayList<>();
        long nodePointer = cellIdToContourPointerMap.get(cellId);

        double lat = Helper.intToDegree(cells.getInt(nodePointer));
        nodePointer = nodePointer + (long) (byteCount);
        double lon = Helper.intToDegree(cells.getInt(nodePointer));
        while (cells.getInt(nodePointer) != Integer.MAX_VALUE) {
            order.add(lat);
            order.add(lon);
            nodePointer = nodePointer + (long) (byteCount);
            lat = Helper.intToDegree(cells.getInt(nodePointer));
            nodePointer = nodePointer + (long) (byteCount);
            lon = Helper.intToDegree(cells.getInt(nodePointer));
        }
        return order;
    }

    /**
     * Get cells of super cell int hash set.
     *
     * @param superCell the super cell
     * @return the int hash set
     */
    public IntHashSet getCellsOfSuperCell(int superCell) {
        return superCellIdToCellsMap.get(superCell);
    }

    /**
     * Get cells of super cell as list list.
     *
     * @param superCell the super cell
     * @return the list
     */
    public List<Integer> getCellsOfSuperCellAsList(int superCell) {
        if (superCellIdToCellsMap.isEmpty())
            throw new IllegalStateException("Supercells not calculated yet.");
        return Arrays.stream(superCellIdToCellsMap.get(superCell).toArray()).boxed().collect(Collectors.toList());
    }

    /**
     * Get super cell of cell int.
     *
     * @param cell the cell
     * @return the int
     */
    public int getSuperCellOfCell(int cell) {
        return cellIdToSuperCellMap.getOrDefault(cell, -1);
    }

    /**
     * Store contour pointer map.
     */
    public void storeContourPointerMap() {
        long listPointer = nodeIndexOffset;
        long nodePointer;
        //Store the number of contours (= num cells + num supercells)
        cells.setHeader(4, cellIdToContourPointerMap.keys().size());
        for (IntCursor cellId : cellIdToContourPointerMap.keys()) {
            cells.setInt(listPointer, cellId.value);
            listPointer = listPointer + (long) byteCount;
            nodePointer = cellIdToContourPointerMap.get(cellId.value);
            cells.setBytes(listPointer, longToByteArray(nodePointer), 8);
            listPointer = listPointer + (long) byteCount + (long) byteCount;
        }
    }

    /**
     * Store super cells. It's a block at the end of all other data that stores [superCellId0, subcell0, subcell1, ..., -1, superCellId1, subcell0, subcell1, ..., -1, ..., -1]
     * End denomination is by -1 for each supercell block and for the whole block we add another trailing -1.
     *
     * @param superCells the super cells
     */
    public void storeSuperCells(IntObjectMap<IntHashSet> superCells) {
        //Store the beginning of the supercells information
        superCellIdToCellsMap = superCells;
        cells.setHeader(8, (int) (cellContourPointer >> 32));
        cells.setHeader(12, (int) cellContourPointer);
        for (IntObjectCursor<IntHashSet> superCell : superCells) {
            // + 1 for supercellId and + 1 for trailing -1
            cells.ensureCapacity(cellContourPointer + (long) (superCell.value.size() + 2) * byteCount);
            cells.setInt(cellContourPointer, superCell.key);
            cellContourPointer = cellContourPointer + (long) byteCount;
            for (IntCursor cellId : superCell.value) {
                cells.setInt(cellContourPointer, cellId.value);
                cellIdToSuperCellMap.put(cellId.value, superCell.key);
                cellContourPointer = cellContourPointer + (long) byteCount;
            }
            //Add trailing -1 to signal next entry
            cells.setInt(cellContourPointer, -1);
            cellContourPointer = cellContourPointer + (long) byteCount;
        }
        //Add second trailing -1 to mark end of superCell block
        cells.ensureCapacity(cellContourPointer + (long) byteCount);
        cells.setInt(cellContourPointer, -1);
        cellContourPointer = cellContourPointer + (long) byteCount;
    }

    private void fillCellIdToNodesPointerMap() {
        int cellCount = cells.getHeader(0);
        byte[] buffer = new byte[8];

        for (int i = 0; i < cellCount; i++) {
            int cellId = cells.getInt((long) i * 12);
            cells.getBytes((long) i * 12 + 4, buffer, 8);
            long nodePointer = byteArrayToLong(buffer);
            cellIdToNodesPointerMap.put(cellId, nodePointer);
        }
    }

    private void fillCellIdToContourPointerMap() {
        int contourCount = cells.getHeader(4);
        byte[] buffer = new byte[8];
        long listPointer = nodeIndexOffset;
        for (int i = 0; i < contourCount; i++) {
            int cellId = cells.getInt(listPointer);
            listPointer = listPointer + (long) byteCount;

            cells.getBytes(listPointer, buffer, 8);
            long nodePointer = byteArrayToLong(buffer);
            listPointer = listPointer + (long) 8;
            cellIdToContourPointerMap.put(cellId, nodePointer);
        }
    }

    private void fillSuperCellMap() {
        long bytePos = (long) cells.getHeader(8) << 32 | cells.getHeader(12) & 0xFFFFFFFFL;
        while (cells.getInt(bytePos) != -1) {
            int superCellId = cells.getInt(bytePos);
            IntHashSet cellIds = new IntHashSet();
            bytePos += byteCount;
            while (cells.getInt(bytePos) != -1) {
                cellIds.add(cells.getInt(bytePos));
                bytePos += byteCount;
            }
            superCellIdToCellsMap.put(superCellId, cellIds);
            bytePos += byteCount;
        }
    }

    private void fillCellIdToSuperCellMap() {
        for (IntObjectCursor<IntHashSet> superCell : superCellIdToCellsMap) {
            for (IntCursor cellId : superCell.value) {
                cellIdToSuperCellMap.put(cellId.value, superCell.key);
            }
        }
    }

    /**
     * Is contour prepared boolean.
     *
     * @return the boolean
     */
    public boolean isContourPrepared() {
        return cells.getHeader(16) > 0;
    }

    /**
     * Set contour prepared.
     *
     * @param prepared the prepared
     */
    public void setContourPrepared(boolean prepared) {
        cells.setHeader(16, prepared ? 1 : 0);
    }

    public CellStorage create(long byteCount) {
        throw new IllegalStateException("Do not call CellStorage.create directly");
    }

    public void flush() {
        cells.flush();
    }

    @Override
    public void close() {
        cells.close();
    }

    @Override
    public boolean isClosed() {
        return cells.isClosed();
    }

    public long getCapacity() {
        return cells.getCapacity();
    }
}
