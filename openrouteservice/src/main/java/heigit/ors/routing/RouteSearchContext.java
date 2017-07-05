/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.PMap;

public class RouteSearchContext {
	private GraphHopper _graphhopper;
	private EdgeFilter _edgeFilter;
	private FlagEncoder _encoder;
	
	private PMap _properties;

	public RouteSearchContext(GraphHopper gh, EdgeFilter edgeFilter, FlagEncoder encoder)
	{
		_graphhopper = gh;   
		_edgeFilter = edgeFilter;
		_encoder = encoder;
	}

	public FlagEncoder getEncoder() {
		return _encoder;
	}

	public EdgeFilter getEdgeFilter() {
		return _edgeFilter;
	}

	public GraphHopper getGraphHopper() {
		return _graphhopper;
	}
	
	public PMap getProperties()
	{
		return _properties;
	}
	
	public void setProperties(PMap value)
	{
		_properties = value;
	}
}
