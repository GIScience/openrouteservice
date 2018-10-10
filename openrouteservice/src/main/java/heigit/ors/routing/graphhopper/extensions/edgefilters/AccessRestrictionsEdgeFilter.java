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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.coll.GHIntArrayList;
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;
import heigit.ors.routing.graphhopper.extensions.storages.AccessRestrictionsGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;

import java.util.List;

public class AccessRestrictionsEdgeFilter implements EdgeFilter {
	private int _vehicleType= 0;
	private AccessRestrictionsGraphStorage _gsRestrictions;
	private GHIntArrayList _allowedEdges;
	private byte[] _buffer = new byte[2];

	public AccessRestrictionsEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage, List<Integer> allowedEdges) {

		if (allowedEdges != null)
		{
			_allowedEdges = new GHIntArrayList(allowedEdges.size());
			for (Integer v : allowedEdges)
				_allowedEdges.add(v);
		}

		// motorcar = 0
		// motorcycle = 1
		// bicycle = 2
		// foot = 3
		int routePref = RoutingProfileType.getFromEncoderName(encoder.toString());

		if (RoutingProfileType.isDriving(routePref) && routePref == RoutingProfileType.DRIVING_MOTORCYCLE)
			_vehicleType = 1;
		else if (RoutingProfileType.isDriving(routePref))
			_vehicleType = 0;
		else if (RoutingProfileType.isCycling(routePref))
			_vehicleType = 2;
		else if (RoutingProfileType.isWalking(routePref))
			_vehicleType = 3;

		_gsRestrictions = GraphStorageUtils.getGraphExtension(graphStorage, AccessRestrictionsGraphStorage.class);
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {

		return _gsRestrictions.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(iter), _vehicleType, _buffer) == AccessRestrictionType.None || _allowedEdges == null || _allowedEdges.contains(EdgeIteratorStateHelper.getOriginalEdge(iter));

	}

}
