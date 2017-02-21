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

package heigit.ors.routing.graphhopper.extensions.weighting;

import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.RoutingProfileCategory;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.storages.*;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class AvoidFeaturesWeighting implements Weighting {

	/**
	 * Converting to seconds is not necessary but makes adding other penalities
	 * easier (e.g. turn costs or traffic light costs etc)
	 */
	protected final static double SPEED_CONV = 3.6;
	protected final FlagEncoder encoder;
	private int avoidFeatureType;
	private WayCategoryGraphStorage gsWayCategory;
	private WheelchairAttributesGraphStorage gsWheelchair;
	private HillIndexGraphStorage gsHillIndex;
	private Weighting superWeighting;
	private byte[] buffer;
	private int profileCategory;

	private static final double SPEED_FACTOR = 0.001;
	private static final int HIGHWAYS = AvoidFeatureFlags.Highways;
	private static final int TOLLWAYS = AvoidFeatureFlags.Tollways;
	private static final int FERRIES = AvoidFeatureFlags.Ferries;
	private static final int UNPAVEDROADS = AvoidFeatureFlags.UnpavedRoads;
	private static final int STEPS = AvoidFeatureFlags.Steps;
	private static final int BORDERS = AvoidFeatureFlags.Borders;
	private static final int TUNNELS = AvoidFeatureFlags.Tunnels;
	private static final int BRIDGES = AvoidFeatureFlags.Bridges;
	private static final int HILLS = AvoidFeatureFlags.Hills;
	private static final int FORDS = AvoidFeatureFlags.Fords;

	public AvoidFeaturesWeighting(Weighting superWeighting, FlagEncoder encoder, int avoidFeatureType, GraphStorage graphStorage)  {
		this.encoder = encoder;
		this.superWeighting = superWeighting;
		this.avoidFeatureType = avoidFeatureType;
		this.buffer = new byte[10];

		profileCategory = RoutingProfileCategory.getFromRouteProfile(RoutingProfileType.getFromEncoderName(encoder.toString()));

		gsWayCategory = GraphStorageUtils.getGraphExtension(graphStorage, WayCategoryGraphStorage.class);
		gsWheelchair = GraphStorageUtils.getGraphExtension(graphStorage, WheelchairAttributesGraphStorage.class);
		if (((avoidFeatureType & HILLS) == HILLS))
			gsHillIndex = GraphStorageUtils.getGraphExtension(graphStorage, HillIndexGraphStorage.class); 

		//if (((avoidFeatureType & HILLS) == HILLS) && gsHillIndex == null)
		//		throw new Exception("Unable to detect HillIndex storage");
	}

	@Override
	public double getMinWeight(double distance) {
		return superWeighting.getMinWeight(distance);
	}

	@Override
	public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
		double reduceFactor = 1.0;

		if (avoidFeatureType != 0) {
			int edgeFeatType = 0;
			if (gsWayCategory != null)
			{
				edgeFeatType = gsWayCategory.getEdgeValue(edge.getEdge(), buffer);
				if (edgeFeatType > 0) {
					if (profileCategory != RoutingProfileCategory.DRIVING)
					{
						if ((avoidFeatureType & HIGHWAYS) == HIGHWAYS) {
							if ((edgeFeatType & HIGHWAYS) == HIGHWAYS) {
								reduceFactor = SPEED_FACTOR;
							}
						}
						
						if ((avoidFeatureType & TOLLWAYS) == TOLLWAYS) {
							if ((edgeFeatType & TOLLWAYS) == TOLLWAYS) {
								reduceFactor = SPEED_FACTOR;
							}
						}
						
						if ((avoidFeatureType & FERRIES) == FERRIES) {
							if ((edgeFeatType & FERRIES) == FERRIES) {
								reduceFactor = SPEED_FACTOR;
							}
						}
						
						if ((avoidFeatureType & UNPAVEDROADS) == UNPAVEDROADS) {
							if ((edgeFeatType & UNPAVEDROADS) == UNPAVEDROADS) {
								reduceFactor = SPEED_FACTOR;
							}
						}
						
						if ((avoidFeatureType & BORDERS) == BORDERS) {
							if ((edgeFeatType & BORDERS) == BORDERS) {
								reduceFactor = SPEED_FACTOR;
							}
						}
						
						if ((avoidFeatureType & FORDS) == FORDS) {
							if ((edgeFeatType & FORDS) == FORDS) {
								reduceFactor = SPEED_FACTOR;
							}
						}
						
						if ((avoidFeatureType & TUNNELS) == TUNNELS) {
							if ((edgeFeatType & TUNNELS) == TUNNELS) {
								reduceFactor = SPEED_FACTOR;
							}
						}
						
						if ((avoidFeatureType & BRIDGES) == BRIDGES) {
							if ((edgeFeatType & BRIDGES) == BRIDGES) {
								reduceFactor = SPEED_FACTOR;
							}
						}
					}
					else if (profileCategory == RoutingProfileCategory.CYCLING)
					{
						if ((avoidFeatureType & FERRIES) == FERRIES) {
							if ((edgeFeatType & FERRIES) == FERRIES) {
								reduceFactor = SPEED_FACTOR;
							}
						}

						if ((avoidFeatureType & FORDS) == FORDS) {
							if ((edgeFeatType & FORDS) == FORDS) {
								reduceFactor = SPEED_FACTOR;
							}
						}
						if ((avoidFeatureType & UNPAVEDROADS) == UNPAVEDROADS) {
							if ((edgeFeatType & UNPAVEDROADS) == UNPAVEDROADS) {
								reduceFactor = SPEED_FACTOR;
							}
						}

						if ((avoidFeatureType & STEPS) == STEPS) {
							if ((edgeFeatType & STEPS) == STEPS) {
								reduceFactor = SPEED_FACTOR;
							}
						}	
					}
					else if (profileCategory == RoutingProfileCategory.WALKING)
					{
						if ((avoidFeatureType & FERRIES) == FERRIES) {
							if ((edgeFeatType & FERRIES) == FERRIES) {
								reduceFactor = SPEED_FACTOR;
							}
						}
						
						if ((avoidFeatureType & FORDS) == FORDS) {
							if ((edgeFeatType & FORDS) == FORDS) {
								reduceFactor = SPEED_FACTOR;
							}
						}

						if ((avoidFeatureType & STEPS) == STEPS) {
							if ((edgeFeatType & STEPS) == STEPS) {
								reduceFactor = SPEED_FACTOR;
							}
						}	
					}
				}
			}
			else if (gsWheelchair != null)
			{
				edgeFeatType = gsWheelchair.getEdgeFeatureTypeFlag(edge.getEdge(), buffer);

				if (edgeFeatType > 0) {
					if ((avoidFeatureType & FERRIES) == FERRIES) {
						if ((edgeFeatType & FERRIES) == FERRIES) {
							reduceFactor = SPEED_FACTOR;
						}
					}
				}
			}

			if ((avoidFeatureType & HILLS) == HILLS) {
				int hi = gsHillIndex.getEdgeValue(edge.getOriginalEdge(), reverse, buffer);
				if (hi > 0)
				{
					if (reduceFactor == 1.0)
						reduceFactor = SPEED_FACTOR/hi;
					else 
						reduceFactor /= hi;
				}
			}
		}

		if (reduceFactor == 1.0)
			return superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId);
		else
		{
			double weight = superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId);
			return weight / reduceFactor;
			//return Double.POSITIVE_INFINITY;

		}
	}

	@Override
	public String toString() {
		return "AVOIDFEATURES|" + encoder;
	}

	@Override
	public FlagEncoder getFlagEncoder() {
		return this.encoder;
	}
}
