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

import heigit.ors.routing.RoutingProfileType;

public class AccessRestrictionsGraphStorage implements GraphExtension {
	/* pointer for no entry */
	protected final int NO_ENTRY = -1;
	protected final int EF_RESTRICTIONS;

	protected DataAccess edges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount; 
	private byte[] byteValues;
	private boolean _hasMotorVehicles = false;
	private boolean _hasNonMotorVehicles = false;

	public AccessRestrictionsGraphStorage(int[] profileTypes) 
	{
		for (int i = 0; i < profileTypes.length; i++)
		{
			int rp = profileTypes[i];
			if (RoutingProfileType.isCycling(rp) || RoutingProfileType.isWalking(rp) )
				_hasNonMotorVehicles = true;
			else if (RoutingProfileType.isDriving(rp))
				_hasMotorVehicles = true;
		}
		
		// we allocate 1 or 2 bytes for 4 profiles (motorcar, motorcycle, bicycle, foot), each profile might occupy maximum 4 bits
		EF_RESTRICTIONS = nextBlockEntryIndex (_hasMotorVehicles && _hasNonMotorVehicles ? 2 : 1);

		edgeEntryBytes = edgeEntryIndex;
		edgesCount = 0;
		byteValues = new byte[2];
	}

	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ext_access_restrictions storage must be initialized only once.");

		this.edges = dir.find("ext_access_restrictions");
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
			throw new IllegalStateException("Unable to load storage 'ext_access_restrictions'. corrupt file or directory? ");

		edgeEntryBytes = edges.getHeader(0);
		edgesCount = edges.getHeader(4);
		return true;
	}

	void ensureEdgesIndex(int edgeIndex) {
		edges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

	public void setEdgeValue(int edgeId, int[] restrictions) {
		edgesCount++;
		ensureEdgesIndex(edgeId);

		long edgePointer = (long) edgeId * edgeEntryBytes;

		if (restrictions != null)
		{
			if (_hasMotorVehicles && _hasNonMotorVehicles)
			{
				byteValues[0] = (byte)(restrictions[0] << 4 | (0x0F & restrictions[1]));
				byteValues[1] = (byte)(restrictions[2] << 4 | (0x0F & restrictions[3]));
				edges.setBytes(edgePointer + EF_RESTRICTIONS, byteValues, 2);
			}
			else
			{
				if (_hasMotorVehicles)
					byteValues[0] = (byte)(restrictions[0] << 4 | (0x0F & restrictions[1]));
				else
					byteValues[0] = (byte)(restrictions[2] << 4 | (0x0F & restrictions[3]));
				
				edges.setBytes(edgePointer + EF_RESTRICTIONS, byteValues, 1);
			}			
		}
	}

	//vehicleType can take the following values
	// motorcar = 0
	// motorcycle = 1
	// bicycle = 2
	// foot = 3
	public int getEdgeValue(int edgeId, int vehicleType, byte[] buffer) {
		long edgeBase = (long) edgeId * edgeEntryBytes;

		if (vehicleType == 0 || vehicleType == 1)
		{
			edges.getBytes(edgeBase + EF_RESTRICTIONS, buffer, 1);

			byte value = buffer[0];
			if (value != 0)
			{
				if (vehicleType == 1)
					return value & 0xF;
				else
					return (value >> 4) & 0xF;
			}
		}
		else
		{
			if (_hasMotorVehicles)
				edges.getBytes(edgeBase + EF_RESTRICTIONS + 1, buffer, 1);
			else
				edges.getBytes(edgeBase + EF_RESTRICTIONS, buffer, 1);

			byte value = buffer[0];
			if (value != 0)
			{
				if (vehicleType == 2)
					return value & 0xF;
				else
					return (value >> 4) & 0xF;
			}
		}

		return 0;
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
		if (!(clonedStorage instanceof AccessRestrictionsGraphStorage)) {
			throw new IllegalStateException("the extended storage to clone must be the same");
		}

		AccessRestrictionsGraphStorage clonedTC = (AccessRestrictionsGraphStorage) clonedStorage;

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
