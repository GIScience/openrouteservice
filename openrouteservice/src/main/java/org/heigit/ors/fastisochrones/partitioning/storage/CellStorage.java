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
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.Storable;
import com.graphhopper.util.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.isSupercellsEnabled;
import static org.heigit.ors.fastisochrones.partitioning.storage.ByteConversion.byteArrayToLong;
import static org.heigit.ors.fastisochrones.partitioning.storage.ByteConversion.longToByteArray;

/**
 * Stores nodes ordered by cell and contours of cells.
 *
 * @author Hendrik Leuschner
 */
public class CellStorage implements Storable<CellStorage> {

    private final DataAccess cells;
    private int BYTECOUNT;
    private int NODEINDEXOFFSET;
    private int CONTOURINDEXOFFSET;
    private int nodeCount;
    private long cellContourPointer;
    private IsochroneNodeStorage isochroneNodeStorage;
    private IntLongMap cellIdToNodesPointerMap;
    private IntLongMap cellIdToContourPointerMap;
    private IntIntMap cellIdToSuperCellMap = new IntIntHashMap();
    private IntObjectMap<IntHashSet> superCellIdToCellsMap = new IntObjectHashMap<>();

    /**
     * Instantiates a new Cell storage.
     *
     * @param graph                the graph
     * @param dir                  the dir
     * @param isochroneNodeStorage the isochrone node storage
     */
    public CellStorage(GraphHopperStorage graph, Directory dir, IsochroneNodeStorage isochroneNodeStorage) {
        this.isochroneNodeStorage = isochroneNodeStorage;
        cells = dir.find("cells");
        BYTECOUNT = 4;
        nodeCount = graph.getNodes();
    }

    @Override
    public boolean loadExisting() {
        if (cells.loadExisting()) {
            int cellCount = cells.getHeader(0);
            NODEINDEXOFFSET = cellCount * 12;
            CONTOURINDEXOFFSET = 2 * cellCount * 18;
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
        NODEINDEXOFFSET = cellCount * 12;
        //There are more contours than cells because of supercell contours
        CONTOURINDEXOFFSET = 2 * cellCount * 18;
        long nodePointer = (long) CONTOURINDEXOFFSET;

        //Put all the cell nodes in the storage
        for (IntCursor cellId : cellIdToNodesMap.keys()) {
            cells.ensureCapacity(nodePointer + (long) (cellIdToNodesMap.get(cellId.value).size() + 1) * BYTECOUNT);
            cellIdToNodesPointerMap.put(cellId.value, nodePointer);
            for (IntCursor nodeId : cellIdToNodesMap.get(cellId.value)) {
                cells.setInt(nodePointer, nodeId.value);
                nodePointer = nodePointer + (long) BYTECOUNT;
            }
            //Add a trailing -1 so we know when to stop
            cells.setInt(nodePointer, -1);
            nodePointer = nodePointer + (long) BYTECOUNT;
        }
        //Set the contour node pointer to the end of the nodes part
        cellContourPointer = nodePointer;

        //Put the cellId to pointer map into the storage
        //Layout: [cellId (4B), pointer to nodes (8B)]
        long listPointer = 0;
        for (IntCursor cellId : cellIdToNodesPointerMap.keys()) {
            cells.setInt(listPointer, cellId.value);
            listPointer = listPointer + (long) BYTECOUNT;
            nodePointer = cellIdToNodesPointerMap.get(cellId.value);
            cells.setBytes(listPointer, longToByteArray(nodePointer), 8);
            listPointer = listPointer + (long) BYTECOUNT + (long) BYTECOUNT;
        }
    }

    /**
     * Get nodes of cell int hash set.
     *
     * @param cellId the cell id
     * @return the int hash set
     */
    public IntHashSet getNodesOfCell(int cellId) {
        long nodePointer = cellIdToNodesPointerMap.get(cellId);
        int currentNode = cells.getInt(nodePointer);
        IntHashSet nodeIds = new IntHashSet();
        while (currentNode != -1) {
            nodeIds.add(currentNode);
            nodePointer = nodePointer + (long) BYTECOUNT;
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
            cellContourPointer = cellContourPointer + (long) BYTECOUNT;
            cells.setInt(cellContourPointer, Helper.degreeToInt(longitudes.get(i)));
            cellContourPointer = cellContourPointer + (long) BYTECOUNT;
        }
        //Add a trailing int max value so we know when to stop
        cells.setInt(cellContourPointer, Integer.MAX_VALUE);
        cellContourPointer = cellContourPointer + (long) BYTECOUNT;
        cells.setInt(cellContourPointer, Integer.MAX_VALUE);
        cellContourPointer = cellContourPointer + (long) BYTECOUNT;
    }

    /**
     * Get cell contour order list.
     *
     * @param cellId the cell id
     * @return the list
     */
    public List<Double> getCellContourOrder(int cellId) {
        List<Double> order = new ArrayList<>();
        long nodePointer = cellIdToContourPointerMap.get(cellId);

        double lat = Helper.intToDegree(cells.getInt(nodePointer));
        nodePointer = nodePointer + (long) (BYTECOUNT);
        double lon = Helper.intToDegree(cells.getInt(nodePointer));
        while (cells.getInt(nodePointer) != Integer.MAX_VALUE) {
            order.add(lat);
            order.add(lon);
            nodePointer = nodePointer + (long) (BYTECOUNT);
            lat = Helper.intToDegree(cells.getInt(nodePointer));
            nodePointer = nodePointer + (long) (BYTECOUNT);
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
        long listPointer = NODEINDEXOFFSET;
        long nodePointer;
        //Store the number of contours (= num cells + num supercells)
        cells.setHeader(4, cellIdToContourPointerMap.keys().size());
        for (IntCursor cellId : cellIdToContourPointerMap.keys()) {
            cells.setInt(listPointer, cellId.value);
            listPointer = listPointer + (long) BYTECOUNT;
            nodePointer = cellIdToContourPointerMap.get(cellId.value);
            cells.setBytes(listPointer, longToByteArray(nodePointer), 8);
            listPointer = listPointer + (long) BYTECOUNT + (long) BYTECOUNT;
        }
    }

    /**
     * Store super cells.
     *
     * @param superCells the super cells
     */
    public void storeSuperCells(IntObjectMap<IntHashSet> superCells) {
        //Store the beginning of the supercells information
        superCellIdToCellsMap = superCells;
        cells.setHeader(8, (int) (cellContourPointer >> 32));
        cells.setHeader(12, (int) cellContourPointer);
        for (IntObjectCursor<IntHashSet> superCell : superCells) {
            cells.setInt(cellContourPointer, superCell.key);
            cellContourPointer = cellContourPointer + (long) BYTECOUNT;
            for (IntCursor cellId : superCell.value) {
                cells.setInt(cellContourPointer, cellId.value);
                cellIdToSuperCellMap.put(cellId.value, superCell.key);
                cellContourPointer = cellContourPointer + (long) BYTECOUNT;
            }
            //Add trailing -1 to signal next entry
            cells.setInt(cellContourPointer, -1);
            cellContourPointer = cellContourPointer + (long) BYTECOUNT;
        }
        //Add second trailing -1 to mark end of superCell block
        cells.setInt(cellContourPointer, -1);
        cellContourPointer = cellContourPointer + (long) BYTECOUNT;
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
        long listPointer = NODEINDEXOFFSET;
        for (int i = 0; i < contourCount; i++) {
            int cellId = cells.getInt(listPointer);
            listPointer = listPointer + (long) BYTECOUNT;

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
            bytePos += (long) BYTECOUNT;
            while (cells.getInt(bytePos) != -1) {
                cellIds.add(cells.getInt(bytePos));
                bytePos += (long) BYTECOUNT;
            }
            superCellIdToCellsMap.put(superCellId, cellIds);
            bytePos += (long) BYTECOUNT;
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
        return cells.getHeader(16) > 0 ? true : false;
    }

    /**
     * Set contour prepared.
     *
     * @param prepared the prepared
     */
    public void setContourPrepared(boolean prepared) {
        cells.setHeader(16, prepared == true ? 1 : 0);
    }

    /**
     * Is corrupted boolean.
     *
     * @return the boolean
     */
    public boolean isCorrupted() {
        return cellIdToContourPointerMap.size() != cellIdToNodesPointerMap.size();
    }

    @Override
    public CellStorage create(long byteCount) {
        throw new IllegalStateException("Do not call CellStorage.create directly");
    }

    @Override
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
