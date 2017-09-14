/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
