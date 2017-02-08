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

import heigit.ors.routing.graphhopper.extensions.VehicleRestrictionCodes;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;

public class VehicleMaxWeightEdgeFilter extends WayRestrictionEdgeFilter {
	public VehicleMaxWeightEdgeFilter(FlagEncoder encoder, double restrictionValue, GraphStorage graphStorage) {
		super(encoder, true, true, restrictionValue, VehicleRestrictionCodes.MaxWeight, graphStorage);
	}

	public VehicleMaxWeightEdgeFilter(FlagEncoder encoder, boolean in, boolean out, double restrictionValue,
			GraphStorage graphStorage) {
		super(encoder, in, out, restrictionValue, VehicleRestrictionCodes.MaxWeight, graphStorage);
	}
}
