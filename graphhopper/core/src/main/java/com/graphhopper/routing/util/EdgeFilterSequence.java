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

package com.graphhopper.routing.util;

import java.util.ArrayList;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;

public class EdgeFilterSequence implements EdgeFilter {

	private ArrayList<EdgeFilter> edgeFilters;
	private int filtersCount;

	/**
	 * Creates an edges filter which accepts both direction of the specified
	 * vehicle.
	 */
	public EdgeFilterSequence(ArrayList<EdgeFilter> edgeFilters) {
		this.edgeFilters = edgeFilters;
		this.filtersCount = edgeFilters.size();
	}

	public void addFilter(EdgeFilter e) {
		edgeFilters.add(e);
		filtersCount++;
	}
	
	public EdgeFilter getEdgeFilter(Class<?> type)
	{
		for (int i = 0; i < filtersCount; i++) {
			if (type.isAssignableFrom(edgeFilters.get(i).getClass()))
				return edgeFilters.get(i);
		}
		
		return null;
	}
	
	public boolean containsEdgeFilter(Class<?> type)
	{
		for (int i = 0; i < filtersCount; i++) {
			if (type.isAssignableFrom(edgeFilters.get(i).getClass()))
				return true;
		}
		
		return false;
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {
		for (int i = 0; i < filtersCount; i++) {
			if (!edgeFilters.get(i).accept(iter))
				return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "EdgeFilter Sequence :" + filtersCount;
	}
}
