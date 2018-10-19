/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package heigit.ors.isochrones;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.common.TravelRangeType;
import heigit.ors.common.TravellerInfo;
import heigit.ors.services.ServiceRequest;

import java.util.ArrayList;
import java.util.List;

public class IsochroneRequest extends ServiceRequest
{
	private List<TravellerInfo> _travellers;
	private String _calcMethod;
	private String _units = null;
    private String _area_units = null;
	private Boolean _includeIntersections = false;
	private String[] _attributes;
	private float _smoothingFactor = -1.0f;

	public IsochroneRequest()
	{
		_travellers = new ArrayList<TravellerInfo>();
	}

	public String getCalcMethod() 
	{
		return _calcMethod;
	}

	public void setCalcMethod(String calcMethod) 
	{
		_calcMethod = calcMethod;
	}

	public String getUnits() {
		return _units;
	}

    public String getAreaUnits() {
        return _area_units;
    }

	public void setUnits(String units) {
		_units = units;
	}

    public void setAreaUnits(String area_units) {
        _area_units = area_units;
    }

	public boolean isValid()
	{
		return _travellers.size() >= 1;
	}

	public String[] getAttributes() {
		return _attributes;
	}

	public void setAttributes(String[] attributes) {
		_attributes = attributes;
	}

	public String[] getNonDefaultAttributes()
	{
		if (_attributes == null || _attributes.length ==0)
			return null;

		List<String> list = null;
		
		for(String attr : _attributes)
		{
			if (! (attr ==null || "area".equalsIgnoreCase(attr) || "reachfactor".equalsIgnoreCase(attr)))
			{
				if (list == null)
					list = new ArrayList<String>();
				
				list.add(attr);
			}
		}

		if (list == null || list.size() == 0)
			return null;
		
		return list.toArray(new String[list.size()]);
	}

	public boolean hasAttribute(String attr) {
		if (_attributes == null || attr == null)
			return false;

		for (int i = 0; i < _attributes.length; i++)
			if (attr.equalsIgnoreCase(_attributes[i]))
				return true;

		return false;
	}

	public Boolean getIncludeIntersections()
	{
		return _includeIntersections;
	}

	public void setIncludeIntersections(Boolean value)
	{
		_includeIntersections = value;
	}

	public Coordinate[] getLocations()
	{
		Coordinate[] locations = new Coordinate[_travellers.size()];

		for(int i = 0; i < _travellers.size(); i++)
		{
			locations[i] = _travellers.get(i).getLocation();
		}

		return locations;
	}

	public void setSmoothingFactor(float smoothingFactor) {
		this._smoothingFactor = smoothingFactor;
	}

	public IsochroneSearchParameters getSearchParameters(int travellerIndex)
	{
		TravellerInfo traveller = _travellers.get(travellerIndex);
		double[] ranges = traveller.getRanges();

		// convert ranges in units to meters or seconds
		if (!(_units == null || "m".equalsIgnoreCase(_units)))
		{
			double scale = 1.0;
			if (traveller.getRangeType() == TravelRangeType.Distance)
			{
				switch(_units)
				{
				case "m":
					break;
				case "km":
					scale = 1000;
					break;
				case "mi":
					scale = 1609.34;
					break;
				}
			}

			if (scale != 1.0)
			{
				for (int i = 0; i < ranges.length; i++)
					ranges[i] = ranges[i]*scale;
			}
		}

		IsochroneSearchParameters parameters = new IsochroneSearchParameters(travellerIndex, traveller.getLocation(), ranges);
		parameters.setLocation(traveller.getLocation());
		parameters.setRangeType(traveller.getRangeType() );
		parameters.setCalcMethod(_calcMethod);
		parameters.setRouteParameters(traveller.getRouteSearchParameters());
		if ("destination".equalsIgnoreCase(traveller.getLocationType()))
			parameters.setReverseDirection(true);
		parameters.setSmoothingFactor(_smoothingFactor);
		return parameters;
	}

	public List<TravellerInfo> getTravellers() {
		return _travellers;
	}

	public void addTraveller(TravellerInfo traveller) throws Exception
	{
		if (traveller == null)
			throw new Exception("'traveller' argument is null.");

		_travellers.add(traveller);
	}
}
