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

public class TrailDifficultyScaleGraphStorage implements GraphExtension {
	protected final int efDifficultyScale;

	protected DataAccess edges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount; 
	private final byte[] byteValues;

	public TrailDifficultyScaleGraphStorage()  {
		efDifficultyScale = nextBlockEntryIndex (2);

		edgeEntryBytes = edgeEntryIndex;
		edgesCount = 0;
		byteValues = new byte[2];
	}

	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ext_traildifficulty storage must be initialized only once.");

		this.edges = dir.find("ext_traildifficulty");
	}

	protected final int nextBlockEntryIndex(int size) {
		int res = edgeEntryIndex;
		edgeEntryIndex += size;
		return res;
	}

	public void setSegmentSize(int bytes) {
		edges.setSegmentSize(bytes);
	}

	public TrailDifficultyScaleGraphStorage create(long initBytes) {
		edges.create(initBytes * edgeEntryBytes);
		return this;
	}

	@Override
	public long getCapacity() {
		return edges.getCapacity();
	}
	public void flush() {
		edges.setHeader(0, edgeEntryBytes);
		edges.setHeader(4, edgesCount);
		edges.flush();
	}

	public void close() {
		edges.close();
	}

	public int entries() {
		return edgesCount;
	}

	public boolean loadExisting() {
		if (!edges.loadExisting())
			throw new IllegalStateException("Unable to load storage 'ext_traildifficulty'. corrupt file or directory? ");

		edgeEntryBytes = edges.getHeader(0);
		edgesCount = edges.getHeader(4);
		return true;
	}

	void ensureEdgesIndex(int edgeIndex) {
		edges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

	public void setEdgeValue(int edgeId, int sacScale, int mtbScale, int mtbUphillScale) {
		edgesCount++;
		ensureEdgesIndex(edgeId);

		long edgePointer = (long) edgeId * edgeEntryBytes;
 
		byteValues[0] = (byte)sacScale;
		byteValues[1] = (byte)(mtbScale << 4 | (0x0F & mtbUphillScale));
		
		edges.setBytes(edgePointer + efDifficultyScale, byteValues, 2);
	}

	public int getHikingScale(int edgeId, byte[] buffer) {
		long edgeBase = (long) edgeId * edgeEntryBytes;
		
		edges.getBytes(edgeBase + efDifficultyScale, buffer, 1);

		return buffer[0];
	}
	
	public int getMtbScale(int edgeId, byte[] buffer, boolean uphill) {
		long edgeBase = (long) edgeId * edgeEntryBytes;
		
		edges.getBytes(edgeBase + efDifficultyScale + 1, buffer, 1);
		
		if (uphill)
			return  (byte)(buffer[0] & 0x0F);
		else
			return (byte)((buffer[0] >> 4) & (byte) 0x0F);
	}

	@Override
	public boolean isClosed() {
		return false;
	}
}
