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

import com.graphhopper.storage.Graph;

public class MatrixSearchContext {
	private Graph graph;
	private MatrixLocations sources;
	private MatrixLocations destinations;

	public MatrixSearchContext(Graph graph, MatrixLocations sources, MatrixLocations destinations) {
		this.graph = graph;
		this.sources = sources;
		this.destinations = destinations;
	}
	
	public Graph getGraph()
	{
		return graph;
	}
	
	public MatrixLocations getSources()
	{
		return sources;
	}
	
	public MatrixLocations getDestinations()
	{
		return destinations;
	}
}
