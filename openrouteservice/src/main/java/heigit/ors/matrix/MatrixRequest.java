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
package heigit.ors.matrix;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.services.ServiceRequest;
import heigit.ors.util.DistanceUnit;

public class MatrixRequest extends ServiceRequest
{
    private int _profileType = -1;
    private Coordinate[] _sources;
    private Coordinate[] _destinations;
    private int _metrics =  MatrixMetricsType.Duration;
	private DistanceUnit _units = DistanceUnit.Meters;
    private boolean _resolveLocations = false;
    
    public MatrixRequest()
    {
    	
    }
    
    public Coordinate[] getSources()
    {
    	return _sources;
    }
    
    public void setSources(Coordinate[] sources)
    {
    	_sources = sources;
    }
    
    public Coordinate[] getDestinations()
    {
    	return _destinations;
    }    
    
    public void setDestinations(Coordinate[] destinations)
    {
    	_destinations = destinations;
    }

	public int getMetrics() {
		return _metrics;
	}

	public void setMetrics(int valueTypes) {
		_metrics = valueTypes;
	}

	public boolean getResolveLocations() {
		return _resolveLocations;
	}

	public void setResolveLocations(boolean resolveLocations) {
		_resolveLocations = resolveLocations;
	}

	public int getProfileType() {
		return _profileType;
	}

	public void setProfileType(int profile) {
		_profileType = profile;
	}
	
	public DistanceUnit getUnits() {
		return _units;
	}

	public void setUnits(DistanceUnit units) {
		_units = units;
	}
	
	public int getTotalNumberOfLocations()
	{
		return _destinations.length * _sources.length;
	}
}
