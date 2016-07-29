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

package org.freeopenls.routeservice.graphhopper.extensions.weighting;

import org.freeopenls.routeservice.graphhopper.extensions.storages.BikeAttributesGraphStorage;
import org.freeopenls.routeservice.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;
import org.freeopenls.routeservice.graphhopper.extensions.storages.MotorcarAttributesGraphStorage;
import org.freeopenls.routeservice.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import org.freeopenls.routeservice.routing.AvoidFeatureFlags;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphExtension.ExtendedStorageSequence;
import com.graphhopper.storage.GraphHopperStorage;
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
	private MotorcarAttributesGraphStorage gsMotorcar;
	private HeavyVehicleAttributesGraphStorage gsHeavyVehicles;
	private BikeAttributesGraphStorage gsBike;
	private WheelchairAttributesGraphStorage gsWheelchair;
    private Weighting superWeighting;
    private byte[] buffer;
	
	private static final double SPEED_FACTOR = 0.001;
	private static final int HIGHWAYS = AvoidFeatureFlags.Highway;
	private static final int TOLLWAYS = AvoidFeatureFlags.Tollway;
	private static final int FERRIES = AvoidFeatureFlags.Ferries;
	private static final int UNPAVEDROADS = AvoidFeatureFlags.UnpavedRoads;
	private static final int STEPS = AvoidFeatureFlags.Steps;
	private static final int BORDERS = AvoidFeatureFlags.Borders;
	private static final int TUNNELS = AvoidFeatureFlags.Tunnels;
	private static final int BRIDGES = AvoidFeatureFlags.Bridges;

	public AvoidFeaturesWeighting(Weighting superWeighting, FlagEncoder encoder, int avoidFeatureType, GraphStorage graphStorage) {
		this.encoder = encoder;
		this.superWeighting = superWeighting;
		this.avoidFeatureType = avoidFeatureType;
        this.buffer = new byte[10];
		
		setGraphStorage(graphStorage);
	}

	private void setGraphStorage(GraphStorage graphStorage) {
		if (graphStorage != null) {
			if (graphStorage instanceof GraphHopperStorage) {
				GraphHopperStorage ghs = (GraphHopperStorage) graphStorage;
				GraphExtension ge = ghs.getExtension();
				
				if(ge instanceof ExtendedStorageSequence)
				{
					ExtendedStorageSequence ess = (ExtendedStorageSequence)ge;
					GraphExtension[] exts = ess.getExtensions();
					for (int i = 0; i < exts.length; i++)
					{
						if (assignExtension(exts[i]))
							break;
					}
				}
				else 
				{
					assignExtension(ge);
				}
			}
		}
	}
	
	private boolean assignExtension(GraphExtension ext)
	{
		if (ext instanceof MotorcarAttributesGraphStorage) {
			this.gsMotorcar = (MotorcarAttributesGraphStorage)ext;
			return true;
		} else if (ext instanceof HeavyVehicleAttributesGraphStorage) {
			this.gsHeavyVehicles = (HeavyVehicleAttributesGraphStorage) ext;
			return true;
		} else if (ext instanceof BikeAttributesGraphStorage) {
			this.gsBike = (BikeAttributesGraphStorage) ext;
			return true;
		} else if (ext instanceof WheelchairAttributesGraphStorage)
		{
			this.gsWheelchair = (WheelchairAttributesGraphStorage) ext;
			return true;
		}
		
		return false;
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
			if (gsMotorcar != null)
			{
				edgeFeatType = gsMotorcar.getEdgeWayFlag(edge.getEdge(), buffer);
				if (edgeFeatType > 0) {
					if ((avoidFeatureType & HIGHWAYS) == HIGHWAYS) {
						if ((edgeFeatType & HIGHWAYS) == HIGHWAYS) {
							reduceFactor = SPEED_FACTOR;
						}
					}
					else if ((avoidFeatureType & TOLLWAYS) == TOLLWAYS) {
						if ((edgeFeatType & TOLLWAYS) == TOLLWAYS) {
							reduceFactor = SPEED_FACTOR;
						}
					}
					else if ((avoidFeatureType & FERRIES) == FERRIES) {
						if ((edgeFeatType & FERRIES) == FERRIES) {
							reduceFactor = SPEED_FACTOR;
						}
					}
					else if ((avoidFeatureType & UNPAVEDROADS) == UNPAVEDROADS) {
						if ((edgeFeatType & UNPAVEDROADS) == UNPAVEDROADS) {
							reduceFactor = SPEED_FACTOR;
						}
					}
					else if ((avoidFeatureType & BORDERS) == BORDERS) {
						if ((edgeFeatType & BORDERS) == BORDERS) {
							reduceFactor = SPEED_FACTOR;
						}
					}
					else if ((avoidFeatureType & TUNNELS) == TUNNELS) {
						if ((edgeFeatType & TUNNELS) == TUNNELS) {
							reduceFactor = SPEED_FACTOR;
						}
					}
					else if ((avoidFeatureType & BRIDGES) == BRIDGES) {
						if ((edgeFeatType & BRIDGES) == BRIDGES) {
							reduceFactor = SPEED_FACTOR;
						}
					}
				}
			}
			else if (gsHeavyVehicles != null)
			{
				edgeFeatType = gsHeavyVehicles.getEdgeWayFlag(edge.getEdge(), buffer);
				
				if (edgeFeatType > 0) {
					if ((avoidFeatureType & HIGHWAYS) == HIGHWAYS) {
						if ((edgeFeatType & HIGHWAYS) == HIGHWAYS) {
							reduceFactor = SPEED_FACTOR;
						}
					}
					else if ((avoidFeatureType & TOLLWAYS) == TOLLWAYS) {
						if ((edgeFeatType & TOLLWAYS) == TOLLWAYS) {
							reduceFactor = SPEED_FACTOR;
						}
					}
					else if ((avoidFeatureType & FERRIES) == FERRIES) {
						if ((edgeFeatType & FERRIES) == FERRIES) {
							reduceFactor = SPEED_FACTOR;
						}
					}
					else if ((avoidFeatureType & UNPAVEDROADS) == UNPAVEDROADS) {
						if ((edgeFeatType & UNPAVEDROADS) == UNPAVEDROADS) {
							reduceFactor = SPEED_FACTOR;
						}
					} else if ((avoidFeatureType & BORDERS) == BORDERS) {
						if ((edgeFeatType & BORDERS) == BORDERS) {
							reduceFactor = SPEED_FACTOR;
						}
					}
					else if ((avoidFeatureType & TUNNELS) == TUNNELS) {
						if ((edgeFeatType & TUNNELS) == TUNNELS) {
							reduceFactor = SPEED_FACTOR;
						}
					}
					else if ((avoidFeatureType & BRIDGES) == BRIDGES) {
						if ((edgeFeatType & BRIDGES) == BRIDGES) {
							reduceFactor = SPEED_FACTOR;
						}
					}
				}
			}
			else if (gsBike != null)
			{
				edgeFeatType = gsBike.getEdgeWayFlag(edge.getEdge(), buffer);
				
				if (edgeFeatType > 0) {
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
					
					if ((avoidFeatureType & STEPS) == STEPS) {
						if ((edgeFeatType & STEPS) == STEPS) {
							reduceFactor = SPEED_FACTOR;
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
