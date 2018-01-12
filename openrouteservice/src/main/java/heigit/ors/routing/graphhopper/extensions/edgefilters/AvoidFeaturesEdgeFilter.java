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

import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingProfileCategory;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.storages.*;
import heigit.ors.routing.pathprocessors.BordersExtractor;
import heigit.ors.routing.pathprocessors.TollwayExtractor;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.apache.log4j.Logger;

public class AvoidFeaturesEdgeFilter implements EdgeFilter {
	private static Logger LOGGER = Logger.getLogger(AvoidFeaturesEdgeFilter.class);
	private final boolean _in;
	private final boolean _out;
	protected final FlagEncoder _encoder;
	private byte[] _buffer;
	private WayCategoryGraphStorage _extWayCategory;
	private TollwayExtractor _tollwayExtractor;
	private int _avoidFeatureType;
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

	public AvoidFeaturesEdgeFilter(FlagEncoder encoder, RouteSearchParameters searchParams, GraphStorage graphStorage) {
		this(encoder, true, true, searchParams, graphStorage);
	}

	public AvoidFeaturesEdgeFilter(FlagEncoder encoder, boolean in, boolean out, RouteSearchParameters searchParams,
			GraphStorage graphStorage) {
		this._in = in;
		this._out = out;

		this._encoder = encoder;
		this._avoidFeatureType = searchParams.getAvoidFeatureTypes();
		this._buffer = new byte[10];

		_profileCategory = RoutingProfileCategory.getFromRouteProfile(RoutingProfileType.getFromEncoderName(encoder.toString()));

		_extWayCategory = GraphStorageUtils.getGraphExtension(graphStorage, WayCategoryGraphStorage.class);
		TollwaysGraphStorage extTollways = GraphStorageUtils.getGraphExtension(graphStorage, TollwaysGraphStorage.class);
		if (extTollways != null)
			_tollwayExtractor = new TollwayExtractor(extTollways, searchParams.getVehicleType(), searchParams.getProfileParameters());
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {

		if (_out && iter.isForward(_encoder) || _in && iter.isBackward(_encoder)) {
			if (_avoidFeatureType != 0) {
				int edgeFeatType = 0;
				if (_extWayCategory != null) {
					edgeFeatType = _extWayCategory.getEdgeValue(iter.getEdge(), _buffer);

					if (edgeFeatType > 0) {

						if (_profileCategory == RoutingProfileCategory.DRIVING)
						{

							if ((_avoidFeatureType & HIGHWAYS) == HIGHWAYS) {
								if ((edgeFeatType & HIGHWAYS) == HIGHWAYS) {
									return false;
								}
							}

							if ((_avoidFeatureType & TOLLWAYS) == TOLLWAYS) {
								if ((edgeFeatType & TOLLWAYS) == TOLLWAYS) {
									if (_tollwayExtractor != null)
									{
										int value = _tollwayExtractor.getValue(iter.getEdge());
										if (value != 0)
											return false;
									}
								}
							}

							if ((_avoidFeatureType & FERRIES) == FERRIES) {
								if ((edgeFeatType & FERRIES) == FERRIES) {
									return false;
								}
							} 

							if ((_avoidFeatureType & UNPAVEDROADS) == UNPAVEDROADS) {
								if ((edgeFeatType & UNPAVEDROADS) == UNPAVEDROADS) {
									return false;
								}
							}

							if ((_avoidFeatureType & TRACKS) == TRACKS) {
								if ((edgeFeatType & TRACKS) == TRACKS) {
									return false;
								}
							}

							if ((_avoidFeatureType & TUNNELS) == TUNNELS) {
								if ((edgeFeatType & TUNNELS) == TUNNELS) {
									return false;
								}
							} 

							if ((_avoidFeatureType & BRIDGES) == BRIDGES) {
								if ((edgeFeatType & BRIDGES) == BRIDGES) {
									return false;
								}
							}

                            if ((_avoidFeatureType & BORDERS) == BORDERS) {
                                if ((edgeFeatType & BORDERS) == BORDERS) {
                                    return false;
                                }
                            }

                            if ((_avoidFeatureType & FORDS) == FORDS) {
								if ((edgeFeatType & FORDS) == FORDS) {
									return false;
								}
							}
						}
						else if (_profileCategory == RoutingProfileCategory.CYCLING)
						{
							if ((_avoidFeatureType & FERRIES) == FERRIES) {
								if ((edgeFeatType & FERRIES) == FERRIES) {
									return false;
								}
							}

							if ((_avoidFeatureType & UNPAVEDROADS) == UNPAVEDROADS) {
								if ((edgeFeatType & UNPAVEDROADS) == UNPAVEDROADS) {
									return false;
								}
							}

							if ((_avoidFeatureType & PAVEDROADS) == PAVEDROADS) {
								if ((edgeFeatType & PAVEDROADS) == PAVEDROADS) {
									return false;
								}
							}

							if ((_avoidFeatureType & STEPS) == STEPS) {
								if ((edgeFeatType & STEPS) == STEPS) {
									return false;
								}
							}

							if ((_avoidFeatureType & FORDS) == FORDS) {
								if ((edgeFeatType & FORDS) == FORDS) {
									return false;
								}
							}
						}
						else if (_profileCategory == RoutingProfileCategory.WALKING)
						{
							if ((_avoidFeatureType & FERRIES) == FERRIES) {
								if ((edgeFeatType & FERRIES) == FERRIES) {
									return false;
								}
							}

							if ((_avoidFeatureType & STEPS) == STEPS) {
								if ((edgeFeatType & STEPS) == STEPS) {
									return false;
								}
							}

							if ((_avoidFeatureType & FORDS) == FORDS) {
								if ((edgeFeatType & FORDS) == FORDS) {
									return false;
								}
							}
						}
						else if (_profileCategory == RoutingProfileCategory.WHEELCHAIR)
						{
							if ((_avoidFeatureType & FERRIES) == FERRIES) {
								if ((edgeFeatType & FERRIES) == FERRIES) {
									return false;
								}
							}
						}
						
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
