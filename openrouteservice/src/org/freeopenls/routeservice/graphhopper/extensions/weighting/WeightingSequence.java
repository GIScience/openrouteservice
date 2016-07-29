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

import java.util.ArrayList;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.util.EdgeIteratorState;

public class WeightingSequence implements Weighting {
	private ArrayList<Weighting> weightings;
	private int weightingsCount;
	private Weighting weighting1, weighting2;

	public WeightingSequence(ArrayList<Weighting> weightings) {
		this.weightings = weightings;
		this.weightingsCount = weightings.size();
		
		if (weightingsCount == 2)
		{
			weighting1 = weightings.get(0);
			weighting2 = weightings.get(1);
		}
	}

	public void addWeighting(Weighting w) {
		weightings.add(w);
		weightingsCount++;
	}

	@Override
	public double getMinWeight(double distance) {
		
		double minValue = Double.MAX_VALUE;
		
		for (int i = 0; i < weightingsCount; i++) {
			double w = weightings.get(i).getMinWeight(distance);
			if (w < minValue)
				minValue = w;
		}
 
		return minValue;
	}

	@Override
	public String toString() {
		return "WEIGHTINGSEQUENCE";
	}

	@Override
	public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId ) {
		if (weightingsCount == 2)
		{
			return weighting1.calcWeight(edge, reverse, prevOrNextEdgeId) + weighting2.calcWeight(edge, reverse, prevOrNextEdgeId);
		}
		
        double result = 0;
		
		for (int i = 0; i < weightingsCount; i++) {
			result += weightings.get(i).calcWeight(edge, reverse, prevOrNextEdgeId);
		}
 
		return result;
	}

	@Override
	public FlagEncoder getFlagEncoder() {
		return weightings.get(0).getFlagEncoder();
	}
}