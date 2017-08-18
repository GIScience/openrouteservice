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
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;

import heigit.ors.routing.graphhopper.extensions.storages.TrailDifficultyScaleGraphStorage;

public class TrailDifficultyScaleGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private TrailDifficultyScaleGraphStorage _storage;
	private int _hikingScale;
	private int _mtbScale;
	private int _mtbUphillScale;

	public TrailDifficultyScaleGraphStorageBuilder()
	{
	}

	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");

		_storage = new TrailDifficultyScaleGraphStorage();

		return _storage;
	}

	public void processWay(ReaderWay way) {
		_hikingScale = getSacScale(way.getTag("sac_scale"));
		_mtbScale = getMtbScale(way.getTag("mtb:scale"));
		if (_mtbScale == 0)
			_mtbScale = getMtbScale(way.getTag("mtb:scale:imba"));
		_mtbUphillScale = getMtbScale(way.getTag("mtb:scale:uphill"));
		if (_mtbUphillScale == 0)
			_mtbUphillScale = _mtbScale;
	}

	private int getSacScale(String value)
	{
		if (!Helper.isEmpty(value))
		{
			switch(value)
			{
			case "hiking":
				return 1;
			case "mountain_hiking":
				return 2;
			case "demanding_mountain_hiking":
				return 3;
			case "alpine_hiking":
				return 4;
			case "demanding_alpine_hiking":
				return 5;
			case "difficult_alpine_hiking":
				return 6;
			}
		}

		return 0;
	}

	private int getMtbScale(String value)
	{
		if (!Helper.isEmpty(value))
		{
			try
			{
				return Integer.parseInt(value) + 1;
			}
			catch(Exception ex)
			{}
		}

		return 0;
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge)
	{ 
		_storage.setEdgeValue(edge.getEdge(), _hikingScale, _mtbScale, _mtbUphillScale);
	}

	@Override
	public String getName() {
		return "TrailDifficulty";
	}
}
