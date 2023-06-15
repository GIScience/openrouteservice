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
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import org.heigit.ors.routing.graphhopper.extensions.storages.HillIndexGraphStorage;
import org.heigit.ors.routing.util.HillIndexCalculator;

public class HillIndexGraphStorageBuilder extends AbstractGraphStorageBuilder {
	private HillIndexGraphStorage storage;
	private HillIndexCalculator hillIndexCalc;

	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		if (graphhopper.hasElevation()) {
			storage = new HillIndexGraphStorage(parameters);
			hillIndexCalc = new HillIndexCalculator();
			
			return storage;
		}
		throw new Exception("HillIndexGraphStorageBuilder cannot be initialized since elevation is deactivated for this profile.");
	}

	public void processWay(ReaderWay way) {
		// do nothing
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		boolean revert = edge.getBaseNode() > edge.getAdjNode();

		PointList points = edge.fetchWayGeometry(FetchMode.ALL);
	
		byte hillIndex = hillIndexCalc.getHillIndex(points, false);
		byte reverseHillIndex = hillIndexCalc.getHillIndex(points, true);

		if (revert)
			storage.setEdgeValue(edge.getEdge(), reverseHillIndex, hillIndex);
		else
			storage.setEdgeValue(edge.getEdge(), hillIndex, reverseHillIndex);
	}

	@Override
	public String getName() {
		return "HillIndex";
	}
}
