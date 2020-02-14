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

public class StreetCrossingGraphStorage implements GraphExtension {
	/* pointer for no entry */
	protected final int efStreetCrossings;

	protected DataAccess edges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount;
	private byte[] byteValues;

	public StreetCrossingGraphStorage()  {
		efStreetCrossings = nextBlockEntryIndex (2);
		edgeEntryBytes = edgeEntryIndex;
		edgesCount = 0;
		byteValues = new byte[2];
	}

	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ext_street_crossing storage must be initialized only once.");

		this.edges = dir.find("ext_street_crossing");
	}

	protected final int nextBlockEntryIndex(int size) {
		int res = edgeEntryIndex;
		edgeEntryIndex += size;
		return res;
	}

	public void setSegmentSize(int bytes) {
		edges.setSegmentSize(bytes);
	}

	public GraphExtension create(long initBytes) {
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

	public long getCapacity() {
		return edges.getCapacity();
	}

	public int entries() {
		return edgesCount;
	}

	public boolean loadExisting() {
		if (!edges.loadExisting())
			throw new IllegalStateException("Unable to load storage 'ext_street_crossing'. corrupt file or directory? ");

		edgeEntryBytes = edges.getHeader(0);
		edgesCount = edges.getHeader(4);
		return true;
	}

	void ensureEdgesIndex(int edgeIndex) {
		edges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

	public void setEdgeValue(int edgeId, int trafficLights, int crossings) {
		edgesCount++;
		ensureEdgesIndex(edgeId);
		byteValues[0] = (byte) trafficLights;
		byteValues[1] = (byte) crossings;
		edges.setBytes((long) edgeId * edgeEntryBytes + efStreetCrossings, byteValues, 2);
	}

	public int getTrafficLights(int edgeId) {
		edges.getBytes((long) edgeId * edgeEntryBytes + efStreetCrossings, byteValues, 1);
		return byteValues[0];
	}

	public int getCrossings(int edgeId) {
		edges.getBytes((long) edgeId * edgeEntryBytes + efStreetCrossings + 1, byteValues, 1);
		return byteValues[0];
	}

	public int[] getTrafficLightsAndCrossings(int edgeId) {
		edges.getBytes((long) edgeId * edgeEntryBytes + efStreetCrossings, byteValues, 2);
		return new int[]{byteValues[0], byteValues[1]};
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

	public GraphExtension copyTo(GraphExtension clonedStorage) {
		if (!(clonedStorage instanceof StreetCrossingGraphStorage)) {
			throw new IllegalStateException("the extended storage to clone must be the same");
		}

		StreetCrossingGraphStorage clonedTC = (StreetCrossingGraphStorage) clonedStorage;

		edges.copyTo(clonedTC.edges);
		clonedTC.edgesCount = edgesCount;

		return clonedStorage;
	}

	@Override
	public boolean isClosed() {
		return false;
	}
}
