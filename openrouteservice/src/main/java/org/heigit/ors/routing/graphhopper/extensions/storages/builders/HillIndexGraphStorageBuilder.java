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

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import heigit.ors.routing.graphhopper.extensions.storages.HillIndexGraphStorage;
import heigit.ors.routing.util.HillIndexCalculator;

public class HillIndexGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private HillIndexGraphStorage _storage;
	private HillIndexCalculator _hillIndexCalc;

	public HillIndexGraphStorageBuilder()
	{
		
	}
	
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		if (graphhopper.hasElevation())
		{
			_storage = new HillIndexGraphStorage(_parameters);
			_hillIndexCalc = new HillIndexCalculator();
			
			return _storage;
		}
		else 
			return null;
	}

	public void processWay(ReaderWay way) {
		
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		boolean revert = edge.getBaseNode() > edge.getAdjNode();

		PointList points = edge.fetchWayGeometry(3);
	
		byte hillIndex = _hillIndexCalc.getHillIndex(points, false);
		byte reverseHillIndex = _hillIndexCalc.getHillIndex(points, true);

		if (revert)
			_storage.setEdgeValue(edge.getEdge(), reverseHillIndex, hillIndex);
		else
			_storage.setEdgeValue(edge.getEdge(), hillIndex, reverseHillIndex);
	}

	@Override
	public String getName() {
		return "HillIndex";
	}
}
