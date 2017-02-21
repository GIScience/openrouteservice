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

package heigit.ors.isochrones;

import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.builders.IsochroneMapBuilder;
import heigit.ors.isochrones.builders.concaveballs.ConcaveBallsIsochroneMapBuilder;
import heigit.ors.routing.RouteSearchContext;

import com.graphhopper.util.Helper;

public class IsochroneMapBuilderFactory {
	private RouteSearchContext _searchContext;

	public IsochroneMapBuilderFactory(RouteSearchContext searchContext) {
		_searchContext = searchContext;
	}

	public IsochroneMap buildMap(IsochroneSearchParameters parameters) throws Exception {
		IsochroneMapBuilder isochroneBuilder = null;

		String method = parameters.getCalcMethod();
		if (Helper.isEmpty(method) || "Default".equalsIgnoreCase(method) || "ConcaveBalls".equalsIgnoreCase(method)) {
			isochroneBuilder = new ConcaveBallsIsochroneMapBuilder();
					} 
        else {
			throw new Exception("Unknown method.");
		}
		
		isochroneBuilder.initialize(_searchContext);
		return isochroneBuilder.compute(parameters);
	}
}
