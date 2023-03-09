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
package org.heigit.ors.routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphhopper.storage.GraphExtension;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.routing.graphhopper.extensions.storages.WarningGraphExtension;
import org.heigit.ors.util.DistanceUnitUtil;
import org.heigit.ors.util.FormatUtility;

public class RouteExtraInfo {
    private final String name;
    private final List<RouteSegmentItem> segments;
    private double factor = 1.0;
    private boolean usedForWarnings = false;
    private WarningGraphExtension warningGraphExtension;
    
    public RouteExtraInfo(String name)
    {
    	this(name, null);
    }

	/**
	 * Constructor that can mark the RouteExtraInfo as being able to generate warnings or not
	 *
	 * @param name			name of the extra info
	 * @param extension		The GraphExtension that is used to generate the extra info. A check is made against this to
	 *                      see if it is of type {@Link org.heigit.ors.routing.graphhopper.extensions.storages.WarningGraphExtension}.
	 *
	 */
	public RouteExtraInfo(String name, GraphExtension extension) {
		this.name = name;
		segments = new ArrayList<>();
		if(extension instanceof WarningGraphExtension) {
			warningGraphExtension = (WarningGraphExtension) extension;
			usedForWarnings = true;
		}
	}
    
    public String getName()
    {
    	return name;
    }
    
    public boolean isEmpty()
    {
    	return segments.isEmpty();
    }
    
    public void add(RouteSegmentItem item)
    {
    	segments.add(item);
    }    

	public List<RouteSegmentItem> getSegments()
	{
		return segments;
	}
	
	public List<ExtraSummaryItem> getSummary(DistanceUnit units, double routeDistance, boolean sort) throws StatusCodeException {
		List<ExtraSummaryItem> summary = new ArrayList<>();
		
		if (!segments.isEmpty()) {
			Comparator<ExtraSummaryItem> comp = (ExtraSummaryItem a, ExtraSummaryItem b) -> Double.compare(b.getAmount(), a.getAmount());

			double totalDist = 0.0;

			Map<Double, Double> stats = new HashMap<>();
			
			for (RouteSegmentItem seg : segments) {
				Double scaledValue = seg.getValue()/ factor;
				Double value = stats.get(scaledValue);
				
				if (value == null)
					stats.put(scaledValue, seg.getDistance());
				else {
					value += seg.getDistance();
					stats.put(scaledValue, value);
				}
				 
				totalDist += seg.getDistance();
			}
			
			if (totalDist != 0.0) {
				int unitDecimals = FormatUtility.getUnitDecimals(units);
				// Some extras such as steepness might provide inconsistent distance values caused by multiple rounding. 
				// Therefore, we try to scale distance so that their sum equals to the whole distance of a route  
				double distScale = totalDist/routeDistance;
				
				for (Map.Entry<Double, Double> entry : stats.entrySet())
				{
					double scaledValue = entry.getValue()/distScale;
					ExtraSummaryItem esi = new ExtraSummaryItem(entry.getKey(),
							FormatUtility.roundToDecimals(DistanceUnitUtil.convert(scaledValue, DistanceUnit.METERS, units), unitDecimals),
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
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public boolean isUsedForWarnings() {
    	return usedForWarnings;
    }

    public WarningGraphExtension getWarningGraphExtension() {
    	return warningGraphExtension;
	}
}
