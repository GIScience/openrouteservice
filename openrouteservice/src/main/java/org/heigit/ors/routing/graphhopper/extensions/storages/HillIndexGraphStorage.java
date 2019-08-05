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

import java.util.Map;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;

public class HillIndexGraphStorage implements GraphExtension {
	protected final int NO_ENTRY = -1;
	protected final int EF_HILLINDEX;

	protected DataAccess orsEdges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount; // number of edges with custom values

	protected int _maxHillIndex = 15;

	private byte[] byteValues;

	public HillIndexGraphStorage(Map<String, String> parameters) {
		EF_HILLINDEX = 0;

		if (parameters.containsKey("maximum_slope"))
			_maxHillIndex = (int)Double.parseDouble(parameters.get("maximum_slope"));

		edgeEntryBytes = edgeEntryIndex + (_maxHillIndex > 15 ? 2 : 1);
		edgesCount = 0;
		byteValues = new byte[2];
	}

	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ORS storage must be initialized only once.");

		this.orsEdges = dir.find("ext_hillindex");
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
			throw new IllegalStateException("Unable to load storage 'ext_hillindex'. corrupt file or directory?");

		edgeEntryBytes = orsEdges.getHeader(0);
		edgesCount = orsEdges.getHeader(4);
		return true;
	}

	void ensureEdgesIndex(int edgeIndex) {
		orsEdges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

	private int getHillIndex(int value)
	{
		return value > _maxHillIndex ? _maxHillIndex : value;
	}

	public void setEdgeValue(int edgeId, int hillIndex, int reverseHillIndex) {
		edgesCount++;
		ensureEdgesIndex(edgeId);

		if (hillIndex != 0 || reverseHillIndex != 0)
		{
			// add entry
			long edgePointer = (long) edgeId * edgeEntryBytes;
			if (_maxHillIndex <= 15)
			{
				byteValues[0] = (byte)(getHillIndex(hillIndex) << 4 | (0x0F & getHillIndex(reverseHillIndex))); //hillIndex | (reverseHillIndex << 4));
				orsEdges.setBytes(edgePointer + EF_HILLINDEX, byteValues, 1);
			}
			else
			{
				byteValues[0] = (byte)getHillIndex(hillIndex);
				byteValues[1] = (byte)getHillIndex(reverseHillIndex);
				orsEdges.setBytes(edgePointer + EF_HILLINDEX, byteValues, 2);
			}
		}
	}

	public int getEdgeValue(int edgeId, boolean reverse, byte[] buffer) {
		long edgePointer = (long) edgeId * edgeEntryBytes;

		if (_maxHillIndex <= 15)
		{
			orsEdges.getBytes(edgePointer + EF_HILLINDEX, buffer, 1);

			int value = buffer[0];
			if (value < 0)
				value = 256 + value;

			if (reverse)
				return (value >> 4) & 0xF;
			else
				return value & 0xF;
		}
		else
		{
			orsEdges.getBytes(edgePointer + EF_HILLINDEX, buffer, 2);

			return reverse ? buffer[1] : buffer[0];
		}
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
		return -1; //throw new UnsupportedOperationException("Not supported by this storage");
	}

	public int getDefaultEdgeFieldValue() {
		return -1;
	}

	public GraphExtension copyTo(GraphExtension clonedStorage) {
		if (!(clonedStorage instanceof HillIndexGraphStorage)) {
			throw new IllegalStateException("the extended storage to clone must be the same");
		}

		HillIndexGraphStorage clonedTC = (HillIndexGraphStorage) clonedStorage;

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
