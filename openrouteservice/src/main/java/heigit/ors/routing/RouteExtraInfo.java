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
package heigit.ors.routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heigit.ors.common.DistanceUnit;
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
	
	public List<ExtraSummaryItem> getSummary(DistanceUnit units, boolean sort) throws Exception
	{
		List<ExtraSummaryItem> summary = new ArrayList<ExtraSummaryItem>();
		
		if (_segments.size() > 0)
		{
			Comparator<ExtraSummaryItem> comp = (ExtraSummaryItem a, ExtraSummaryItem b) -> {
			    return Double.compare(b.getAmount(), a.getAmount());
			};

			double totalDist = 0.0;

			Map<Integer, Double> stats = new HashMap<Integer, Double>();
			
			for (RouteSegmentItem seg : _segments) 
			{
				Double value = stats.get(seg.getValue());
				
				if (value == null)
					stats.put(seg.getValue(), seg.getDistance());
				else
				{
					value += seg.getDistance();
					stats.put(seg.getValue(), value);
				}
				 
				totalDist += seg.getDistance();
			}
			
			if (totalDist != 0.0)
			{
				int unitDecimals = FormatUtility.getUnitDecimals(units);
				
				for (Map.Entry<Integer, Double> entry : stats.entrySet())
				{
					Double value = entry.getValue()/_factor;
					ExtraSummaryItem esi = new ExtraSummaryItem(entry.getKey(),
							FormatUtility.roundToDecimals(DistanceUnitUtil.convert(value, DistanceUnit.Meters, units), unitDecimals),
							FormatUtility.roundToDecimals(value * 100.0 / totalDist, 2)
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
