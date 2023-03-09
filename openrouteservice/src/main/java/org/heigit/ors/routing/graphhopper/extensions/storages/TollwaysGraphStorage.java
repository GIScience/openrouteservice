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

import com.graphhopper.storage.*;

public class TollwaysGraphStorage implements GraphExtension {
	/* pointer for no entry */
	protected final int efTollways;

	protected DataAccess edges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount;

	public TollwaysGraphStorage()  {
		efTollways = nextBlockEntryIndex (1);

		edgeEntryBytes = edgeEntryIndex;
		edgesCount = 0;
	}

	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ext_tolls storage must be initialized only once.");

		this.edges = dir.find("ext_tolls");
	}

	protected final int nextBlockEntryIndex(int size) {
		int res = edgeEntryIndex;
		edgeEntryIndex += size;
		return res;
	}

	public void setSegmentSize(int bytes) {
		edges.setSegmentSize(bytes);
	}

	public TollwaysGraphStorage create(long initBytes) {
		edges.create(initBytes * edgeEntryBytes);
		return this;
	}

	public void flush() {
		edges.setHeader(0, edgeEntryBytes);
		edges.setHeader(4, edgesCount);
		edges.flush();
	}

	public void close() {
		edges.close();
	}

	@Override
	public long getCapacity() {
		return edges.getCapacity();
	}

	public int entries() {
		return edgesCount;
	}

	public boolean loadExisting() {
		if (!edges.loadExisting())
			throw new IllegalStateException("Unable to load storage 'ext_tolls'. corrupt file or directory? ");

		edgeEntryBytes = edges.getHeader(0);
		edgesCount = edges.getHeader(4);
		return true;
	}

	void ensureEdgesIndex(int edgeIndex) {
		edges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

	public void setEdgeValue(int edgeId, int value) {
		edgesCount++;
		ensureEdgesIndex(edgeId);
 
		byte byteValue = (byte) value;

		edges.setByte((long) edgeId * edgeEntryBytes + efTollways, byteValue);
	}

	public int getEdgeValue(int edgeId) {
		byte byteValue = edges.getByte((long) edgeId * edgeEntryBytes + efTollways);
		return byteValue & 0xFF;
	}

	public boolean isRequireNodeField() {
		return true;
	}

	public boolean isRequireEdgeField() {
		// we require the additional field in the graph to point to the first
		// entry in the node table
		return true;
	}

	public int getDefaultNodeFieldValue() {
		return -1;
	}

	public int getDefaultEdgeFieldValue() {
		return -1;
	}

	@Override
	public boolean isClosed() {
		return false;
	}
}
