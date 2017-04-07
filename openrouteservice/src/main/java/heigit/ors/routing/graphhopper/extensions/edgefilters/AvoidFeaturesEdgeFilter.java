/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors: M. Rylov 

package heigit.ors.routing.graphhopper.extensions.edgefilters;

import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.RoutingProfileCategory;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.storages.*;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class AvoidFeaturesEdgeFilter implements EdgeFilter {

	private final boolean in;
	private final boolean out;
	protected final FlagEncoder encoder;
	private int avoidFeatureType;
	private byte[] buffer;
	private WayCategoryGraphStorage gsWayCategory;
	private int profileCategory; 

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

	public AvoidFeaturesEdgeFilter(FlagEncoder encoder, int avoidFeatureType, GraphStorage graphStorage) {
		this(encoder, true, true, avoidFeatureType, graphStorage);
	}

	public AvoidFeaturesEdgeFilter(FlagEncoder encoder, boolean in, boolean out, int avoidFeatureType,
			GraphStorage graphStorage) {
		this.in = in;
		this.out = out;

		this.encoder = encoder;
		this.avoidFeatureType = avoidFeatureType;
		this.buffer = new byte[10];

		profileCategory = RoutingProfileCategory.getFromRouteProfile(RoutingProfileType.getFromEncoderName(encoder.toString()));

		gsWayCategory = GraphStorageUtils.getGraphExtension(graphStorage, WayCategoryGraphStorage.class);
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {

		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder)) {
			if (avoidFeatureType != 0) {
				int edgeFeatType = 0;
				if (gsWayCategory != null) {
					edgeFeatType = gsWayCategory.getEdgeValue(iter.getEdge(), buffer);

					if (edgeFeatType > 0) {

						if (profileCategory == RoutingProfileCategory.DRIVING)
						{
							if ((avoidFeatureType & HIGHWAYS) == HIGHWAYS) {
								if ((edgeFeatType & HIGHWAYS) == HIGHWAYS) {
									return false;
								}
							}

							if ((avoidFeatureType & TOLLWAYS) == TOLLWAYS) {
								if ((edgeFeatType & TOLLWAYS) == TOLLWAYS) {
									return false;
								}
							} 

							if ((avoidFeatureType & FERRIES) == FERRIES) {
								if ((edgeFeatType & FERRIES) == FERRIES) {
									return false;
								}
							} 

							if ((avoidFeatureType & UNPAVEDROADS) == UNPAVEDROADS) {
								if ((edgeFeatType & UNPAVEDROADS) == UNPAVEDROADS) {
									return false;
								}
							}

							if ((avoidFeatureType & TRACKS) == TRACKS) {
								if ((edgeFeatType & TRACKS) == TRACKS) {
									return false;
								}
							} 

							if ((avoidFeatureType & BORDERS) == BORDERS) {
								if ((edgeFeatType & BORDERS) == BORDERS) {
									return false;
								}
							} 

							if ((avoidFeatureType & TUNNELS) == TUNNELS) {
								if ((edgeFeatType & TUNNELS) == TUNNELS) {
									return false;
								}
							} 

							if ((avoidFeatureType & BRIDGES) == BRIDGES) {
								if ((edgeFeatType & BRIDGES) == BRIDGES) {
									return false;
								}
							} 

							if ((avoidFeatureType & FORDS) == FORDS) {
								if ((edgeFeatType & FORDS) == FORDS) {
									return false;
								}
							}
						}
						else if (profileCategory == RoutingProfileCategory.CYCLING)
						{
							if ((avoidFeatureType & FERRIES) == FERRIES) {
								if ((edgeFeatType & FERRIES) == FERRIES) {
									return false;
								}
							}

							if ((avoidFeatureType & UNPAVEDROADS) == UNPAVEDROADS) {
								if ((edgeFeatType & UNPAVEDROADS) == UNPAVEDROADS) {
									return false;
								}
							}

							if ((avoidFeatureType & PAVEDROADS) == PAVEDROADS) {
								if ((edgeFeatType & PAVEDROADS) == PAVEDROADS) {
									return false;
								}
							}

							if ((avoidFeatureType & STEPS) == STEPS) {
								if ((edgeFeatType & STEPS) == STEPS) {
									return false;
								}
							}

							if ((avoidFeatureType & FORDS) == FORDS) {
								if ((edgeFeatType & FORDS) == FORDS) {
									return false;
								}
							}
						}
						else if (profileCategory == RoutingProfileCategory.WALKING)
						{
							if ((avoidFeatureType & FERRIES) == FERRIES) {
								if ((edgeFeatType & FERRIES) == FERRIES) {
									return false;
								}
							}

							if ((avoidFeatureType & STEPS) == STEPS) {
								if ((edgeFeatType & STEPS) == STEPS) {
									return false;
								}
							}

							if ((avoidFeatureType & FORDS) == FORDS) {
								if ((edgeFeatType & FORDS) == FORDS) {
									return false;
								}
							}
						}
						else if (profileCategory == RoutingProfileCategory.WHEELCHAIR)
						{
							if ((avoidFeatureType & FERRIES) == FERRIES) {
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
		return "AVOIDFEATURES|" + encoder;
	}
}
