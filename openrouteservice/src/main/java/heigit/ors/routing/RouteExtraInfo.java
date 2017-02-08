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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import heigit.ors.util.DistanceUnit;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.FormatUtility;

public class RouteExtraInfo 
{
    private String _name;
    private List<RouteSegmentItem> _segments;
    
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
	
	public Map<String, Map<String, Object>> getSummary(DistanceUnit units) throws Exception
	{
		Map<String, Map<String, Object>> summary = new HashMap<String, Map<String, Object>>();
		
		if (_segments.size() > 0)
		{
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
					Map<String, Object> valueSummary = new LinkedHashMap<String, Object>();
					valueSummary.put("value",  entry.getKey());
					valueSummary.put("distance", FormatUtility.roundToDecimals(DistanceUnitUtil.convert(entry.getValue(), DistanceUnit.Meters, units), unitDecimals));
					valueSummary.put("amount",  FormatUtility.roundToDecimals(entry.getValue() * 100.0 / totalDist, 2));
					
					summary.put(entry.getKey().toString(), valueSummary);
				}
			}
		}

		return summary;
	}
}
