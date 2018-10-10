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
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import java.util.HashMap;
import java.util.Map;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public abstract class AbstractGraphStorageBuilder implements GraphStorageBuilder
{
	protected Map<String, String> _parameters;
	
	public abstract GraphExtension init(GraphHopper graphhopper) throws Exception;

	public abstract void processWay(ReaderWay way);

	public void processWay(ReaderWay way, Coordinate[] coords, HashMap<Integer, HashMap<String,String>> nodeTags) { processWay(way);}
	
	public abstract void processEdge(ReaderWay way, EdgeIteratorState edge);

	public void processEdge(ReaderWay way, EdgeIteratorState edge, Coordinate[] coords) { processEdge(way, edge); }
	
	public void setParameters(Map<String, String> parameters)
	{
		_parameters = parameters;
	}
	
	public abstract String getName();
	
	public void finish()
	{
		
	}
}
