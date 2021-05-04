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
package org.heigit.ors.matrix;

public class MatrixLocations {
	private final int[] nodeIds;
	private final ResolvedLocation[] locations;
	private boolean hasValidNodes = false;

	public MatrixLocations(int size) {
		nodeIds = new int[size];
		locations = new ResolvedLocation[size];
	}

	public ResolvedLocation[] getLocations()
	{
		return locations;
	}
	
	public int size()
	{
		return nodeIds.length;
	}

	public int[] getNodeIds()
	{
		return nodeIds;
	}
	
	public int getNodeId(int index)
	{
		return nodeIds[index];
	}
	
	public void setData(int index, int nodeId, ResolvedLocation location)
	{
		nodeIds[index] = nodeId;
		locations[index] = location;
		
		if (nodeId >= 0 && !hasValidNodes)
			hasValidNodes = true;
	}
	
	public boolean hasValidNodes()
	{
		return hasValidNodes;
	}
}
