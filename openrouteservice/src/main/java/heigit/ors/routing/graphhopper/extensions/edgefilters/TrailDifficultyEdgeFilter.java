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

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.HillIndexGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.TrailDifficultyScaleGraphStorage;

public class TrailDifficultyEdgeFilter implements EdgeFilter {

	private final boolean _in;
	private final boolean _out;
	private FlagEncoder _encoder;
	private boolean _isHiking = true;
	private TrailDifficultyScaleGraphStorage _extTrailDifficulty;
	private HillIndexGraphStorage _extHillIndex;
	private byte[] _buffer = new byte[2];
	private int _maximumScale = 10;

	public TrailDifficultyEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage, int maximumScale)
	{
		this(encoder, true, true, graphStorage, maximumScale);
	}

	public TrailDifficultyEdgeFilter(FlagEncoder encoder, boolean in, boolean out, GraphStorage graphStorage, int maximumScale)
	{
		this._encoder = encoder;
		this._in = in;
		this._out = out;

		_maximumScale = maximumScale;

		int routePref = RoutingProfileType.getFromEncoderName(encoder.toString());
		_isHiking = RoutingProfileType.isWalking(routePref);

		_extTrailDifficulty = GraphStorageUtils.getGraphExtension(graphStorage, TrailDifficultyScaleGraphStorage.class);
		_extHillIndex = GraphStorageUtils.getGraphExtension(graphStorage, HillIndexGraphStorage.class);
	}

	@Override
	public final boolean accept(EdgeIteratorState iter )
	{
		if (_out && iter.isForward(_encoder) || _in && iter.isBackward(_encoder))
		{
			if (_isHiking)
			{
				int value = _extTrailDifficulty.getHikingScale(iter.getOriginalEdge(), _buffer);
				if (value > _maximumScale)
					return false;
			} 
			else
			{
				boolean uphill = false;
				if (_extHillIndex != null)
				{
					boolean revert = iter.getBaseNode() < iter.getAdjNode();
					int hillIndex = _extHillIndex.getEdgeValue(iter.getOriginalEdge(), revert, _buffer);
					if (hillIndex > 0)
						uphill = true;
				}
				
				int value = _extTrailDifficulty.getMtbScale(iter.getOriginalEdge(), _buffer, uphill);
				if (value > _maximumScale)
					return false;				
			}

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
