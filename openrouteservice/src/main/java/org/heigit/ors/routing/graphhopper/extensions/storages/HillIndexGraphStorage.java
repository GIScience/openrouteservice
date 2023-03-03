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

import java.util.Map;

import com.graphhopper.storage.*;

public class HillIndexGraphStorage implements GraphExtension {
	private final int efHillIndex;

	private DataAccess orsEdges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount; // number of edges with custom values

	private int maxHillIndex = 15;

	private final byte[] byteValues;

	public HillIndexGraphStorage(Map<String, String> parameters) {
		efHillIndex = 0;

		if (parameters.containsKey("maximum_slope"))
			maxHillIndex = (int)Double.parseDouble(parameters.get("maximum_slope"));

		edgeEntryBytes = edgeEntryIndex + (maxHillIndex > 15 ? 2 : 1);
		edgesCount = 0;
		byteValues = new byte[2];
	}

	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ORS storage must be initialized only once.");

		this.orsEdges = dir.find("ext_hillindex");
	}

	public HillIndexGraphStorage create(long initBytes) {
		orsEdges.create(initBytes * edgeEntryBytes);
		return this;
	}

	public void flush() {
		orsEdges.setHeader(0, edgeEntryBytes);
		orsEdges.setHeader(4, edgesCount);
		orsEdges.flush();
	}

	@Override
	public long getCapacity() {
		return orsEdges.getCapacity();
	}

	public void close() {
		orsEdges.close();
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

	private void ensureEdgesIndex(int edgeIndex) {
		orsEdges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

	private int getHillIndex(int value)
	{
		return Math.min(value, maxHillIndex);
	}

	public void setEdgeValue(int edgeId, int hillIndex, int reverseHillIndex) {
		edgesCount++;
		ensureEdgesIndex(edgeId);

		if (hillIndex != 0 || reverseHillIndex != 0) {
			// add entry
			long edgePointer = (long) edgeId * edgeEntryBytes;
			if (maxHillIndex <= 15) {
				byteValues[0] = (byte)(getHillIndex(hillIndex) << 4 | (0x0F & getHillIndex(reverseHillIndex))); //hillIndex | (reverseHillIndex << 4))
				orsEdges.setBytes(edgePointer + efHillIndex, byteValues, 1);
			} else {
				byteValues[0] = (byte)getHillIndex(hillIndex);
				byteValues[1] = (byte)getHillIndex(reverseHillIndex);
				orsEdges.setBytes(edgePointer + efHillIndex, byteValues, 2);
			}
		}
	}

	public int getEdgeValue(int edgeId, boolean reverse, byte[] buffer) {
		long edgePointer = (long) edgeId * edgeEntryBytes;

		if (maxHillIndex <= 15) {
			orsEdges.getBytes(edgePointer + efHillIndex, buffer, 1);

			int value = buffer[0];
			if (value < 0)
				value = 256 + value;

			if (reverse)
				return (value >> 4) & 0xF;
			else
				return value & 0xF;
		} else {
			orsEdges.getBytes(edgePointer + efHillIndex, buffer, 2);

			return reverse ? buffer[1] : buffer[0];
		}
	}

	@Override
	public boolean isClosed() {
		return false;
	}
}
