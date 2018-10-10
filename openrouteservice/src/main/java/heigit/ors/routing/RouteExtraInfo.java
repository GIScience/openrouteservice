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
package heigit.ors.routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.FormatUtility;

public class RouteExtraInfo 
{
    private String _name;
    private List<RouteSegmentItem> _segments;
    private double _factor = 1.0;
    
    public RouteExtraInfo(String name)
    {
    	_name = name;
    	_segments = new ArrayList<RouteSegmentItem>();
    }
    
    public String getName()
    {
    	return _name;
    }
    
    public boolean isEmpty()
    {
    	return _segments.isEmpty();
    }
    
    public void add(RouteSegmentItem item)
    {
    	_segments.add(item);
    }    

	public List<RouteSegmentItem> getSegments()
	{
		return _segments;
	}
	
	public List<ExtraSummaryItem> getSummary(DistanceUnit units, double routeDistance, boolean sort) throws StatusCodeException {
		List<ExtraSummaryItem> summary = new ArrayList<ExtraSummaryItem>();
		
		if (_segments.size() > 0)
		{
			Comparator<ExtraSummaryItem> comp = (ExtraSummaryItem a, ExtraSummaryItem b) -> {
			    return Double.compare(b.getAmount(), a.getAmount());
			};

			double totalDist = 0.0;

			Map<Double, Double> stats = new HashMap<Double, Double>();
			
			for (RouteSegmentItem seg : _segments) 
			{
				Double scaledValue = seg.getValue()/_factor;
				Double value = stats.get(scaledValue);
				
				if (value == null)
					stats.put(scaledValue, seg.getDistance());
				else
				{
					value += seg.getDistance();
					stats.put(scaledValue, value);
				}
				 
				totalDist += seg.getDistance();
			}
			
			if (totalDist != 0.0)
			{
				int unitDecimals = FormatUtility.getUnitDecimals(units);
				// Some extras such as steepness might provide inconsistent distance values caused by multiple rounding. 
				// Therefore, we try to scale distance so that their sum equals to the whole distance of a route  
				double distScale = totalDist/routeDistance;
				
				for (Map.Entry<Double, Double> entry : stats.entrySet())
				{
					double scaledValue = entry.getValue()/distScale;
					ExtraSummaryItem esi = new ExtraSummaryItem(entry.getKey(),
							FormatUtility.roundToDecimals(DistanceUnitUtil.convert(scaledValue, DistanceUnit.Meters, units), unitDecimals),
							FormatUtility.roundToDecimals(scaledValue * 100.0 / routeDistance, 2)
							);
					
					summary.add(esi);
				}
				
				if (sort)
					summary.sort(comp);
			}
		}

		return summary;
	}

	public double getFactor() {
		return _factor;
	}

	public void setFactor(double _factor) {
		this._factor = _factor;
	}
}
