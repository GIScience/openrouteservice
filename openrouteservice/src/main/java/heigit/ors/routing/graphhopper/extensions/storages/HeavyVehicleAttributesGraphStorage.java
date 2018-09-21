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

public class HeavyVehicleAttributesGraphStorage implements GraphExtension {
	/* pointer for no entry */
	protected final int NO_ENTRY = -1;
	protected final int EF_VEHICLETYPE, EF_DESTINATIONTYPE, EF_RESTRICTION;

	protected DataAccess orsEdges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount; 
	private byte[] byteValues;

	private int attrTypes;

	public HeavyVehicleAttributesGraphStorage(boolean includeRestrictions) 
	{
		EF_VEHICLETYPE = nextBlockEntryIndex(1);
		EF_DESTINATIONTYPE = nextBlockEntryIndex(1);

		if (includeRestrictions)
			// first byte indicates whether any restrictions are given 
			EF_RESTRICTION = nextBlockEntryIndex(6);
		else
			EF_RESTRICTION = -1;

		edgeEntryBytes = edgeEntryIndex;
		edgesCount = 0;
		byteValues = new byte[10];
	}
	
	public int getAttributeTypes() {
		return this.attrTypes;
	}
	
	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ext_hgv storage must be initialized only once.");

		this.orsEdges = dir.find("ext_hgv");
	}

	protected final int nextBlockEntryIndex(int size) {
		int res = edgeEntryIndex;
		edgeEntryIndex += size;
		return res;
	}

	public void setSegmentSize(int bytes) {
		orsEdges.setSegmentSize(bytes);
	}

	public GraphExtension create(long initBytes) {
		orsEdges.create((long) initBytes * edgeEntryBytes);
		return this;
	}

	public void flush() {
		orsEdges.setHeader(0, edgeEntryBytes);
		orsEdges.setHeader(1 * 4, edgesCount);
		orsEdges.flush();
	}

	public void close() {
		orsEdges.close();
	}

	public long getCapacity() {
		return orsEdges.getCapacity();
	}

	public int entries() {
		return edgesCount;
	}

	public boolean loadExisting() {
		if (!orsEdges.loadExisting())
			throw new IllegalStateException("Unable to load storage 'ext_hgv'. corrupt file or directory? ");

		edgeEntryBytes = orsEdges.getHeader(0);
		edgesCount = orsEdges.getHeader(4);
		return true;
	}

	void ensureEdgesIndex(int edgeIndex) {
		orsEdges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

	public void setEdgeValue(int edgeId, int vehicleType, int heavyVehicleDestination, double[] restrictionValues) {
		edgesCount++;
		ensureEdgesIndex(edgeId);

		long edgePointer = (long) edgeId * edgeEntryBytes;
		
	    byteValues[0] = (byte)vehicleType;
		orsEdges.setBytes(edgePointer + EF_VEHICLETYPE, byteValues, 1);
		
		byteValues[0] = (byte)heavyVehicleDestination;
		orsEdges.setBytes(edgePointer + EF_DESTINATIONTYPE, byteValues, 1);

		if (EF_RESTRICTION == -1)
			throw new IllegalStateException("EF_RESTRICTION is not supported.");
	
		if (restrictionValues == null)
		{
			byteValues[0] = 0;
			byteValues[1] = 0;
			byteValues[2] = 0;
			byteValues[3] = 0;
			byteValues[4] = 0;
			byteValues[5] = 0;
		}
		else
		{
			byteValues[0] = 1;
			byteValues[1] = (byte)(restrictionValues[0] * 10);
			byteValues[2] = (byte)(restrictionValues[1] * 10);
			byteValues[3] = (byte)(restrictionValues[2] * 10);
			byteValues[4] = (byte)(restrictionValues[3] * 10);
			byteValues[5] = (byte)(restrictionValues[4] * 10);
		}
		
		orsEdges.setBytes(edgePointer + EF_RESTRICTION, byteValues, 6);
	}

	public double getEdgeRestrictionValue(int edgeId, int valueIndex, byte[] buffer) {
		long edgeBase = (long) edgeId * edgeEntryBytes;

		if (EF_RESTRICTION == -1)
			throw new IllegalStateException("EF_RESTRICTION is not supported.");

		orsEdges.getBytes(edgeBase + EF_RESTRICTION + 1, buffer, 5);
		int retValue = buffer[valueIndex];
		if (retValue == 0)
			return 0.0;

		return retValue / 10d;
	}

	public boolean getEdgeRestrictionValues(int edgeId, byte[] buffer, double[] retValues) {
		long edgeBase = (long) edgeId * edgeEntryBytes;

		if (EF_RESTRICTION == -1)
			throw new IllegalStateException("EF_RESTRICTION is not supported.");

		orsEdges.getBytes(edgeBase + EF_RESTRICTION, buffer, 1);
		if (buffer[0] == 0)
			return false;
		
		orsEdges.getBytes(edgeBase + EF_RESTRICTION + 1, buffer, 5);
		
		retValues[0] = buffer[0] / 10d; 
		retValues[1] = buffer[1] / 10d;
		retValues[2] = buffer[2] / 10d;
		retValues[3] = buffer[3] / 10d;
		retValues[4] = buffer[4] / 10d;
		
		return true;
	}

	public int getEdgeVehicleType(int edgeId, byte[] buffer) {
		long edgeBase = (long) edgeId * edgeEntryBytes;
		orsEdges.getBytes(edgeBase + EF_VEHICLETYPE, buffer, 2);
		
		int result = buffer[0];
	    if (result < 0)
	    	result = (byte)((int)result & 0xff);
	    
	    return result;
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
		if (!(clonedStorage instanceof HeavyVehicleAttributesGraphStorage)) {
			throw new IllegalStateException("the extended storage to clone must be the same");
		}

		HeavyVehicleAttributesGraphStorage clonedTC = (HeavyVehicleAttributesGraphStorage) clonedStorage;

		orsEdges.copyTo(clonedTC.orsEdges);
		clonedTC.edgesCount = edgesCount;

		return clonedStorage;
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}
}
