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
package heigit.ors.optimization;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.services.ServiceRequest;
import heigit.ors.util.DistanceUnit;

public class RouteOptimizationRequest extends ServiceRequest {
	private int _profileType;
	private Coordinate[] _locations;
	private int _sourceIndex;
	private int _destinationIndex;
	private int _metric =  MatrixMetricsType.Duration;
	private boolean _roundTrip = false;
	private DistanceUnit _units = DistanceUnit.Meters;
	
    public RouteOptimizationRequest()
    {
    	
    }
    
	public int getProfileType() {
		return _profileType;
	}

	public void setProfileType(int profileType) {
		_profileType = profileType;
	}
	
	public Coordinate[] getLocations()
	{
		return _locations;
	}
	
	public void setLocations(Coordinate[] locations)
	{
		_locations = locations;
	}
	
    public int getLocationsCount()
    {
    	return _locations == null ? 0: _locations.length;
    }

	public boolean isRoundTrip() {
		return _roundTrip;
	}

	public void setRoundTrip(boolean roundTrip) {
		_roundTrip = roundTrip;
	}

	public int getMetric() {
		return _metric;
	}

	public void setMetric(int metric) {
		_metric = metric;
	}

	public int getSourceIndex() {
		return _sourceIndex;
	}

	public void setSourceIndex(int sourceIndex) {
		_sourceIndex = sourceIndex;
	}

	public int getDestinationIndex() {
		return _destinationIndex;
	}

	public void setDestinationIndex(int destinationIndex) {
		_destinationIndex = destinationIndex;
	}

	public DistanceUnit getUnits() {
		return _units;
	}

	public void setUnits(DistanceUnit units) {
		_units = units;
	}
}
