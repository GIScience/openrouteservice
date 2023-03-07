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

public class WayCategoryGraphStorage implements GraphExtension {
	/* pointer for no entry */
	protected final int efWaytype;

	protected DataAccess orsEdges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount; // number of edges with custom values

	public WayCategoryGraphStorage() {
		efWaytype = 0;
	
		edgeEntryBytes = edgeEntryIndex + 1;
		edgesCount = 0;
	}

	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ORS storage must be initialized only once.");

		this.orsEdges = dir.find("ext_waycategory");
	}

	public void setSegmentSize(int bytes) {
		orsEdges.setSegmentSize(bytes);
	}

	public WayCategoryGraphStorage create(long initBytes) {
		orsEdges.create(initBytes * edgeEntryBytes);
		return this;
	}

	public void flush() {
		orsEdges.setHeader(0, edgeEntryBytes);
		orsEdges.setHeader(4, edgesCount);
		orsEdges.flush();
	}

	public void close() {
		orsEdges.close();
	}

	@Override
	public long getCapacity() {
		return orsEdges.getCapacity();
	}

	public int entries() {
		return edgesCount;
	}

	public boolean loadExisting() {
		if (!orsEdges.loadExisting())
			throw new IllegalStateException("Unable to load storage 'ext_waycategory'. corrupt file or directory? " );

		edgeEntryBytes = orsEdges.getHeader(0);
		edgesCount = orsEdges.getHeader(4);
		return true;
	}

	void ensureEdgesIndex(int edgeIndex) {
		orsEdges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

	public void setEdgeValue(int edgeId, int wayType) {
		edgesCount++;
		ensureEdgesIndex(edgeId);

		// add entry
		long edgePointer = (long) edgeId * edgeEntryBytes;
		byte byteValue = (byte) wayType;
		orsEdges.setByte(edgePointer + efWaytype, byteValue);
	}

	public int getEdgeValue(int edgeId, byte[] buffer) {
		long edgePointer = (long) edgeId * edgeEntryBytes;
		byte byteValue = orsEdges.getByte(edgePointer + efWaytype);

		int result = byteValue;
	    if (result < 0)
	    	result = result & 0xff;
		
		return result;
	}

	@Override
	public boolean isClosed() {
		return false;
	}
}
