/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.services.routing;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.routing.RouteExtraInformationFlag;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.util.DistanceUnit;

public class RoutingRequest {
	private Coordinate[] _coordinates;
	private RouteSearchParameters _searchParameters;
	private DistanceUnit _units = DistanceUnit.Meters;
	private String _language = "en";
	private String _geometryFormat = "encodedpolyline";
	private Boolean _prettifyInstructions = false;
	private Boolean _includeInstructions = true;
	private Boolean _includeElevation = false;
	private Boolean _includeGeometry = true;
	private String[] _attributes = null;
    private int _extraInfo;	
	
	public RoutingRequest()
	{
		_searchParameters = new RouteSearchParameters();
	}

	public Coordinate[] getCoordinates() {
		return _coordinates;
	}

	public void setCoordinates(Coordinate[] _coordinates) {
		this._coordinates = _coordinates;
	}

	public RouteSearchParameters getSearchParameters() {
		return _searchParameters;
	}

	public void setSearchParameters(RouteSearchParameters _searchParameters) {
		this._searchParameters = _searchParameters;
	}

	public boolean getIncludeInstructions() {
		return _includeInstructions;
	}

	public void setIncludeInstructions(boolean includeInstructions) {
		_includeInstructions = includeInstructions;
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

	public Boolean getPrettifyInstructions() {
		return _prettifyInstructions;
	}

	public void setPrettifyInstructions(Boolean prettifyInstructions) {
		this._prettifyInstructions = prettifyInstructions;
	}

	public int getExtraInfo() {
		return _extraInfo;
	}

	public void setExtraInfo(int extraInfo) {
		_extraInfo = extraInfo;
	}

	public Boolean getIncludeElevation() {
		return _includeElevation;
	}

	public void setIncludeElevation(Boolean includeElevation) {
		this._includeElevation = includeElevation;
	}

	public Boolean getIncludeGeometry() {
		return _includeGeometry;
	}

	public void setIncludeGeometry(Boolean _includeGeometry) {
		this._includeGeometry = _includeGeometry;
	}

	public String[] getAttributes() {
		return _attributes;
	}

	public void setAttributes(String[] attributes) {
		_attributes = attributes;
	}
	
	public boolean hasAttribute(String attr) {
		if (_attributes == null || attr == null)
			return false;

		for (int i = 0; i< _attributes.length; i++)
			if (attr.equalsIgnoreCase(_attributes[i]))
				return true;

		return false;
	}
}
