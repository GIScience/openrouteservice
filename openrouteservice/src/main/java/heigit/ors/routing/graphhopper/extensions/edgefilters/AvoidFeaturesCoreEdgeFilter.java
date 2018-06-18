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
import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingProfileCategory;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.TollwaysGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.WayCategoryGraphStorage;
import heigit.ors.routing.pathprocessors.TollwayExtractor;
import org.apache.log4j.Logger;

public class AvoidFeaturesCoreEdgeFilter implements EdgeFilter {
	private final boolean _in;
	private final boolean _out;
	protected final FlagEncoder _encoder;
	private byte[] _buffer;
	private WayCategoryGraphStorage _extWayCategory;
	private int _profileCategory;

	private static final int HIGHWAYS = AvoidFeatureFlags.Highways;
	private static final int TOLLWAYS = AvoidFeatureFlags.Tollways;
	private static final int FERRIES = AvoidFeatureFlags.Ferries;
	private static final int UNPAVEDROADS = AvoidFeatureFlags.UnpavedRoads;
	private static final int PAVEDROADS = AvoidFeatureFlags.PavedRoads;
	private static final int TRACKS = AvoidFeatureFlags.Tracks;
	private static final int STEPS = AvoidFeatureFlags.Steps;
	private static final int BORDERS = AvoidFeatureFlags.Borders;
	private static final int TUNNELS = AvoidFeatureFlags.Tunnels;
	private static final int BRIDGES = AvoidFeatureFlags.Bridges;
	private static final int FORDS = AvoidFeatureFlags.Fords;

	private static final int DRIVING_FEATURES = HIGHWAYS | TOLLWAYS | FERRIES | UNPAVEDROADS | TRACKS | BORDERS | TUNNELS | BRIDGES | FORDS;
	private static final int CYCLING_FEATURES = FERRIES | UNPAVEDROADS | PAVEDROADS| STEPS | FORDS;
	private static final int WALKING_FEATURES = FERRIES | STEPS | FORDS;
	private static final int WHEELCHAIR_FEATURES = FERRIES;

	public AvoidFeaturesCoreEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage) {
		this(encoder, true, true, graphStorage);
	}

	public AvoidFeaturesCoreEdgeFilter(FlagEncoder encoder, boolean in, boolean out, GraphStorage graphStorage) {
		this._in = in;
		this._out = out;

		this._encoder = encoder;
		this._buffer = new byte[10];

		_profileCategory = RoutingProfileCategory.getFromRouteProfile(RoutingProfileType.getFromEncoderName(encoder.toString()));

		_extWayCategory = GraphStorageUtils.getGraphExtension(graphStorage, WayCategoryGraphStorage.class);
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {
		if (_out && iter.isForward(_encoder) || _in && iter.isBackward(_encoder)) {
			if (_extWayCategory != null) {
				int edgeFeatType = _extWayCategory.getEdgeValue(iter.getEdge(), _buffer);

				if (edgeFeatType > 0) {
					switch (_profileCategory) {
						case RoutingProfileCategory.DRIVING: return (edgeFeatType & DRIVING_FEATURES) == 0;
						case RoutingProfileCategory.CYCLING: return (edgeFeatType & CYCLING_FEATURES) == 0;
						case RoutingProfileCategory.WALKING: return (edgeFeatType & WALKING_FEATURES) == 0;
						case RoutingProfileCategory.WHEELCHAIR: return (edgeFeatType & WHEELCHAIR_FEATURES) == 0;
					}
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return "AVOIDFEATURES|" + _encoder;
	}
}
