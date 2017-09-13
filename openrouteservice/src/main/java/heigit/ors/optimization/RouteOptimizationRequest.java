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

import heigit.ors.common.DistanceUnit;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.routing.RouteInstructionsFormat;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.services.ServiceRequest;

public class RouteOptimizationRequest extends ServiceRequest {
	private int _profileType;
	private Coordinate[] _locations;
	private int _sourceIndex;
	private int _destinationIndex;
	private int _metric =  MatrixMetricsType.Duration;
	private boolean _roundTrip = false;
	private DistanceUnit _units = DistanceUnit.Meters;

	private String _language = "en";
	private String _geometryFormat = "encodedpolyline";
	private RouteInstructionsFormat _instructionsFormat = RouteInstructionsFormat.TEXT;
	private Boolean _includeInstructions = true;
	private Boolean _includeElevation = false;
	private Boolean _includeGeometry = true;
	private Boolean _simplifyGeometry = false;
	//private double[] _searchRadii;

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

	public String getGeometryFormat() {
		return _geometryFormat;
	}

	public void setGeometryFormat(String geometryFormat) {
		_geometryFormat = geometryFormat;
	}

	public String getLanguage() {
		return _language;
	}

	public void setLanguage(String language) {
		_language = language;
	}

	public RouteInstructionsFormat getInstructionsFormat() {
		return _instructionsFormat;
	}

	public void setInstructionsFormat(RouteInstructionsFormat instructionsFormat) {
		_instructionsFormat = instructionsFormat;
	}

	public Boolean getIncludeInstructions() {
		return _includeInstructions;
	}

	public void setIncludeInstructions(Boolean includeInstructions) {
		_includeInstructions = includeInstructions;
	}

	public Boolean getIncludeElevation() {
		return _includeElevation;
	}

	public void setIncludeElevation(Boolean includeElevation) {
		_includeElevation = includeElevation;
	}

	public Boolean getIncludeGeometry() {
		return _includeGeometry;
	}

	public void setIncludeGeometry(Boolean includeGeometry) {
		this._includeGeometry = includeGeometry;
	}

	public Boolean getSimplifyGeometry() {
		return _simplifyGeometry;
	}

	public void setSimplifyGeometry(Boolean simplifyGeometry) {
		this._simplifyGeometry = simplifyGeometry;
	}

	public RoutingRequest createRoutingRequest(int[] wayPoints)
	{
		RoutingRequest req = new RoutingRequest();
		//req.setCoordinates(_coordinates);
		req.setGeometryFormat(_geometryFormat);

		return req;
	}

	public MatrixRequest createMatrixRequest()
	{
		MatrixRequest mtxReq = new MatrixRequest();
		mtxReq.setProfileType(_profileType);
		mtxReq.setMetrics(_metric);
		mtxReq.setUnits(_units);

		if (_sourceIndex == -1)
		{
			mtxReq.setSources(_locations);
			mtxReq.setDestinations(_locations);
		}
		else
		{
			mtxReq.setSources(new Coordinate[] { _locations[_sourceIndex] });

			int nLocations = _locations.length;
			Coordinate[] destinations = new Coordinate[nLocations - 1];

			int j = 0;
			for (int i = 0; i < nLocations; ++i)
			{
				if (i != _sourceIndex)
				{
					destinations[j] = _locations[i];
					++j;
				}
			}
			mtxReq.setDestinations(destinations);
		}
		
		return mtxReq;
	}

}
