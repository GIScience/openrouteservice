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
import org.heigit.ors.routing.graphhopper.extensions.VehicleDimensionRestrictions;

public class HeavyVehicleAttributesGraphStorage implements GraphExtension {
	private static final int  EF_RESTRICTION_BYTES = 2;
	private static final String MSG_EF_RESTRICTION_IS_NOT_SUPPORTED = "EF_RESTRICTION is not supported.";

	private final int efVehicleType;
	private final int efDestinationType;
	private final int efRestrictions;

	private DataAccess orsEdges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount;

	private static final double FACTOR = 100.0;

	public HeavyVehicleAttributesGraphStorage(boolean includeRestrictions) {
		efVehicleType = nextBlockEntryIndex(1);
		efDestinationType = nextBlockEntryIndex(1);

		if (includeRestrictions)
			// first byte indicates whether any restrictions are given 
			efRestrictions = nextBlockEntryIndex(VehicleDimensionRestrictions.COUNT * EF_RESTRICTION_BYTES);
		else
			efRestrictions = -1;

		edgeEntryBytes = edgeEntryIndex;
		edgesCount = 0;
	}
	
	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ext_hgv storage must be initialized only once.");

		this.orsEdges = dir.find("ext_hgv");
	}

	private int nextBlockEntryIndex(int size) {
		int res = edgeEntryIndex;
		edgeEntryIndex += size;
		return res;
	}

	public void setSegmentSize(int bytes) {
		orsEdges.setSegmentSize(bytes);
	}

	public GraphExtension create(long initBytes) {
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

	private void ensureEdgesIndex(int edgeIndex) {
		orsEdges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

	public void setEdgeValue(int edgeId, int vehicleType, int heavyVehicleDestination, double[] restrictionValues) {
		edgesCount++;
		ensureEdgesIndex(edgeId);

		long edgePointer = (long) edgeId * edgeEntryBytes;

		byte [] byteValues = {(byte) vehicleType, (byte) heavyVehicleDestination};
		orsEdges.setBytes(edgePointer + efVehicleType, byteValues, 2);

		if (efRestrictions == -1)
			throw new IllegalStateException(MSG_EF_RESTRICTION_IS_NOT_SUPPORTED);

		for (int i = 0; i < VehicleDimensionRestrictions.COUNT; i++) {
			short shortValue = (short) (restrictionValues[i] * FACTOR);
			orsEdges.setShort(edgePointer + efRestrictions + i * EF_RESTRICTION_BYTES, shortValue);
		}
	}

	public double getEdgeRestrictionValue(int edgeId, int valueIndex) {
		long edgeBase = (long) edgeId * edgeEntryBytes;

		if (efRestrictions == -1)
			throw new IllegalStateException(MSG_EF_RESTRICTION_IS_NOT_SUPPORTED);

		return orsEdges.getShort(edgeBase + efRestrictions + valueIndex * EF_RESTRICTION_BYTES) / FACTOR;
	}

	public boolean getEdgeRestrictionValues(int edgeId, double[] retValues) {
		long edgeBase = (long) edgeId * edgeEntryBytes;

		if (efRestrictions == -1)
			throw new IllegalStateException(MSG_EF_RESTRICTION_IS_NOT_SUPPORTED);

		for (int i = 0; i < VehicleDimensionRestrictions.COUNT; i++)
			retValues[i] = orsEdges.getShort(edgeBase + efRestrictions + i * EF_RESTRICTION_BYTES) / FACTOR;

		return true;
	}

	public int getEdgeVehicleType(int edgeId, byte[] buffer) {
		long edgeBase = (long) edgeId * edgeEntryBytes;
		orsEdges.getBytes(edgeBase + efVehicleType, buffer, 2);
		
		int result = buffer[0];
	    if (result < 0)
	    	result = (byte)(result & 0xff);
	    
	    return result;
	}

	public boolean hasEdgeRestriction(int edgeId) {
		long edgeBase = (long) edgeId * edgeEntryBytes;

		byte[] buffer = new byte[2];
		orsEdges.getBytes(edgeBase + efVehicleType, buffer, 2);

		if (buffer[0] != 0 || buffer[1] != 0)
			return true;

		if (efRestrictions > 0)
			for (int i = 0; i < VehicleDimensionRestrictions.COUNT; i++)
				if (orsEdges.getShort(edgeBase + efRestrictions + i * EF_RESTRICTION_BYTES) != 0)
					return true;

		return false;
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
		return false;
	}
}
