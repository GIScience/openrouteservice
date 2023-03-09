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

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.plugins.Plugin;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;

import java.util.Map;

public interface GraphStorageBuilder extends Plugin {
	GraphExtension init(GraphHopper graphhopper) throws Exception;

	void processWay(ReaderWay way);

	void processWay(ReaderWay way, Coordinate[] coords, Map<Integer, Map<String, String>> nodeTags);
	
	void processEdge(ReaderWay way, EdgeIteratorState edge);

	void processEdge(ReaderWay way, EdgeIteratorState edge, Coordinate[] coords);

	void setParameters(Map<String, String> parameters);

	String getName();
	
	void finish();
}
