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
package heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;

public class TollwaysGraphStorage implements GraphExtension {
	/* pointer for no entry */
	protected final int NO_ENTRY = -1;
	protected final int EF_TOLLWAYS;

	protected DataAccess edges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount; 
	private byte[] byteValue;

	public TollwaysGraphStorage() 
	{
		EF_TOLLWAYS = nextBlockEntryIndex (1);

		edgeEntryBytes = edgeEntryIndex;
		edgesCount = 0;
		byteValue = new byte[1];
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

	public GraphExtension create(long initBytes) {
		edges.create((long) initBytes * edgeEntryBytes);
		return this;
	}

	public void flush() {
		edges.setHeader(0, edgeEntryBytes);
		edges.setHeader(1 * 4, edgesCount);
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
 
		byteValue[0] = (byte) value;

		edges.setBytes((long) edgeId * edgeEntryBytes + EF_TOLLWAYS, byteValue, 1);
	}

	public int getEdgeValue(int edgeId) {
		edges.getBytes((long) edgeId * edgeEntryBytes + EF_TOLLWAYS, byteValue, 1);
		
		return byteValue[0] & 0xFF;
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
		//		throw new UnsupportedOperationException("Not supported by this storage");
	}

	public int getDefaultEdgeFieldValue() {
		return -1;
	}

	public GraphExtension copyTo(GraphExtension clonedStorage) {
		if (!(clonedStorage instanceof TollwaysGraphStorage)) {
			throw new IllegalStateException("the extended storage to clone must be the same");
		}

		TollwaysGraphStorage clonedTC = (TollwaysGraphStorage) clonedStorage;

		edges.copyTo(clonedTC.edges);
		clonedTC.edgesCount = edgesCount;

		return clonedStorage;
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}
}
