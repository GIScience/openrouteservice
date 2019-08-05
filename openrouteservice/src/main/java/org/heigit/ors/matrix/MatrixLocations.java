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
package heigit.ors.matrix;

public class MatrixLocations {
	private int[] _nodeIds;
	private ResolvedLocation[] _locations;
	private boolean _hasValidNodes = false;

	public MatrixLocations(int size, boolean resolveNames)
	{
		_nodeIds = new int[size];
		_locations = new ResolvedLocation[size];
	}

	public ResolvedLocation[] getLocations()
	{
		return _locations;
	}
	
	public int size()
	{
		return _nodeIds.length;
	}

	public int[] getNodeIds()
	{
		return _nodeIds;
	}
	
	public int getNodeId(int index)
	{
		return _nodeIds[index];
	}
	
	public void setData(int index, int nodeId, ResolvedLocation location)
	{
		_nodeIds[index] = nodeId;
		_locations[index] = location;
		
		if (nodeId >= 0 && !_hasValidNodes)
			_hasValidNodes = true;
	}
	
	public boolean hasValidNodes()
	{
		return _hasValidNodes;
	}
}
