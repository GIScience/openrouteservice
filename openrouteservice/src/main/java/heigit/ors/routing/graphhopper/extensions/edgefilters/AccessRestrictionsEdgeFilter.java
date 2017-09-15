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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import java.util.List;

import com.graphhopper.coll.GHIntArrayList;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;
import heigit.ors.routing.graphhopper.extensions.storages.AccessRestrictionsGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;

public class AccessRestrictionsEdgeFilter implements EdgeFilter {

	private final boolean _in;
	private final boolean _out;
	private FlagEncoder _encoder;
	private int _vehicleType= 0;
	private AccessRestrictionsGraphStorage _gsRestrictions;
	private GHIntArrayList _allowedEdges;
	private byte[] _buffer = new byte[2];

	public AccessRestrictionsEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage, List<Integer> allowedEdges)
	{
		this(encoder, true, true, graphStorage, allowedEdges);
	}

	public AccessRestrictionsEdgeFilter(FlagEncoder encoder, boolean in, boolean out, GraphStorage graphStorage, List<Integer> allowedEdges)
	{
		this._encoder = encoder;
		this._in = in;
		this._out = out;
		
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
	public final boolean accept(EdgeIteratorState iter )
	{
		if (_out && iter.isForward(_encoder) || _in && iter.isBackward(_encoder))
		{
			int res = _gsRestrictions.getEdgeValue(iter.getOriginalEdge(), _vehicleType, _buffer);
			if (res == AccessRestrictionType.None)
				return true;

			if (_allowedEdges == null || _allowedEdges.contains(iter.getOriginalEdge()))
				return true;
		}

		return false;
	}

	@Override
	public String toString()
	{
		return _encoder.toString() + ", in:" + _in + ", out:" + _out;
	}
}
