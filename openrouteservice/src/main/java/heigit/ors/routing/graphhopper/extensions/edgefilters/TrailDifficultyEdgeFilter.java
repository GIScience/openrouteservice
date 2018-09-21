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

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.HillIndexGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.TrailDifficultyScaleGraphStorage;

public class TrailDifficultyEdgeFilter implements EdgeFilter {
	private FlagEncoder _encoder;
	private boolean _isHiking = true;
	private TrailDifficultyScaleGraphStorage _extTrailDifficulty;
	private HillIndexGraphStorage _extHillIndex;
	private byte[] _buffer = new byte[2];
	private int _maximumScale = 10;

	public TrailDifficultyEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage, int maximumScale) {
		this._encoder = encoder;

		_maximumScale = maximumScale;

		int routePref = RoutingProfileType.getFromEncoderName(encoder.toString());
		_isHiking = RoutingProfileType.isWalking(routePref);

		_extTrailDifficulty = GraphStorageUtils.getGraphExtension(graphStorage, TrailDifficultyScaleGraphStorage.class);
		_extHillIndex = GraphStorageUtils.getGraphExtension(graphStorage, HillIndexGraphStorage.class);
	}

	@Override
	public final boolean accept(EdgeIteratorState iter ) {
		if (_isHiking)
		{
			int value = _extTrailDifficulty.getHikingScale(EdgeIteratorStateHelper.getOriginalEdge(iter), _buffer);
			if (value > _maximumScale)
				return false;
		}
		else
		{
			boolean uphill = false;
			if (_extHillIndex != null)
			{
				boolean revert = iter.getBaseNode() < iter.getAdjNode();
				int hillIndex = _extHillIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(iter), revert, _buffer);
				if (hillIndex > 0)
					uphill = true;
			}

			int value = _extTrailDifficulty.getMtbScale(EdgeIteratorStateHelper.getOriginalEdge(iter), _buffer, uphill);
			if (value > _maximumScale)
				return false;
		}

		return true;

	}

}
