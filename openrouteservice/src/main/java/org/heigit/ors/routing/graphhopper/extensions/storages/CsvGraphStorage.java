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
package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;

import java.util.Arrays;

    public class CsvGraphStorage implements GraphExtension {
        /* pointer for no entry */
        private final int efCsvIndex; // TODO: what is this?

        private DataAccess orsEdges;
        private int edgeEntryBytes;
        private int edgesCount; // number of edges with custom values
        private final int numEntries;
        private String[] columnNames;

        public CsvGraphStorage(String[] columnNames) {
            efCsvIndex = 0;
            this.columnNames = columnNames;
            numEntries = columnNames.length;

            int edgeEntryIndex = 0;
            edgeEntryBytes = edgeEntryIndex + numEntries;
            edgesCount = 0;
        }

        public void setEdgeValue(int edgeId, byte[] values) {
            edgesCount++;
            ensureEdgesIndex(edgeId);

            // add entry
            long edgePointer = (long) edgeId * edgeEntryBytes;
            orsEdges.setBytes(edgePointer + efCsvIndex, values, numEntries);
        }

        private void ensureEdgesIndex(int edgeId) {
            orsEdges.ensureCapacity(((long) edgeId + 1) * edgeEntryBytes);
        }

        public int getEdgeValue(int edgeId, int columnIndex, byte[] buffer) {
            long edgePointer = (long) edgeId * edgeEntryBytes;
            // TODO: maybe we don't need to get the whole buffer, but just the
            //       single element and can use a 1-element buffer at call-site
            orsEdges.getBytes(edgePointer + efCsvIndex, buffer, numEntries);

            return buffer[columnIndex];
        }

        public int columnIndex(String columnName) {
            int index = 0;
            for (String name: columnNames) {
                if (name.equals(columnName)) {
                    return index;
                }
                index++;
            }
            throw new IllegalArgumentException("Illegal column name: " + columnName);
        }

        /**
         * @return true, if and only if, if an additional field at the graphs node storage is required
         */
        @Override// TODO: true or false?
        public boolean isRequireNodeField() {
            return true;
        }

        /**
         * @return true, if and only if, if an additional field at the graphs edge storage is required
         */
        @Override
        public boolean isRequireEdgeField() {
            return true;
        }

        /**
         * @return the default field value which will be set for default when creating nodes
         */
        @Override
        public int getDefaultNodeFieldValue() {
            return -1;
        }

        /**
         * @return the default field value which will be set for default when creating edges
         */
        @Override
        public int getDefaultEdgeFieldValue() {
            return -1;
        }

        /**
         * initializes the extended storage by giving the base graph
         *
         * @param graph
         * @param dir
         */
        @Override
        public void init(Graph graph, Directory dir) {
            if (edgesCount > 0)
                throw new AssertionError("The ORS storage must be initialized only once.");

            this.orsEdges = dir.find("ext_csv");
        }

        /**
         * sets the segment size in all additional data storages
         *
         * @param bytes
         */
        @Override // TODO
        public void setSegmentSize(int bytes) { orsEdges.setSegmentSize(bytes); }

        /**
         * creates a copy of this extended storage
         *
         * @param clonedStorage
         */
        @Override // TODO
        public GraphExtension copyTo(GraphExtension clonedStorage) {
            if (!(clonedStorage instanceof org.heigit.ors.routing.graphhopper.extensions.storages.CsvGraphStorage)) {
                throw new IllegalStateException("the extended storage to clone must be the same");
            }

            CsvGraphStorage clonedTC = (CsvGraphStorage) clonedStorage;

            orsEdges.copyTo(clonedTC.orsEdges);
            clonedTC.edgesCount = edgesCount;

            return clonedStorage;
        }

        /**
         * @return true if successfully loaded from persistent storage.
         */
        @Override
        public boolean loadExisting() {
            if (!orsEdges.loadExisting())
                throw new IllegalStateException("Unable to load storage 'ext_csv'. corrupt file or directory?");

            edgeEntryBytes = orsEdges.getHeader(0);
            edgesCount = orsEdges.getHeader(4);
            return true;
        }

        /**
         * Creates the underlying storage. First operation if it cannot be loaded.
         *
         * @param initBytes
         */
        @Override
        public GraphExtension create(long initBytes) {
            orsEdges.create(initBytes * edgeEntryBytes);
            return this;
        }

        /**
         * This method makes sure that the underlying data is written to the storage. Keep in mind that
         * a disc normally has an IO cache so that flush() is (less) probably not save against power
         * loses.
         */
        @Override
        public void flush() {
            orsEdges.setHeader(0, edgeEntryBytes);
            orsEdges.setHeader(4, edgesCount);
            orsEdges.flush();
        }

        /**
         * This method makes sure that the underlying used resources are released. WARNING: it does NOT
         * flush on close!
         */
        @Override
        public void close() { orsEdges.close(); }

        @Override
        public boolean isClosed() {
            return false;
        }

        /**
         * @return the allocated storage size in bytes
         */
        @Override
        public long getCapacity() {
            return orsEdges.getCapacity();
        }

        public String[] columnNames() {
            return Arrays.copyOf(columnNames, columnNames.length);
        }

        public int numEntries() {
            return numEntries;
        }
}


