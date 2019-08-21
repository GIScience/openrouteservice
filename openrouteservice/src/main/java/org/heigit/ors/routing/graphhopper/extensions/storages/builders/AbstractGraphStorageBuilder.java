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
package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import java.util.HashMap;
import java.util.Map;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.Coordinate;

public abstract class AbstractGraphStorageBuilder implements GraphStorageBuilder
{
	protected Map<String, String> parameters;
	
	public void processWay(ReaderWay way, Coordinate[] coords, HashMap<Integer, HashMap<String,String>> nodeTags) { processWay(way);}
	
	public void processEdge(ReaderWay way, EdgeIteratorState edge, Coordinate[] coords) { processEdge(way, edge); }
	
	public void setParameters(Map<String, String> parameters)
	{
		this.parameters = parameters;
	}
	
	public void finish(){}
}
