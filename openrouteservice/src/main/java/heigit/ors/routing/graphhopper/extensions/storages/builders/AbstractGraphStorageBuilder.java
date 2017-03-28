/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import java.util.Map;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;

public abstract class AbstractGraphStorageBuilder implements GraphStorageBuilder
{
	protected Map<String, String> _parameters;
	
	public abstract GraphExtension init(GraphHopper graphhopper) throws Exception;

	public abstract void processWay(OSMWay way);
	
	public abstract void processEdge(OSMWay way, EdgeIteratorState edge);
	
	public void setParameters(Map<String, String> parameters)
	{
		_parameters = parameters;
	}
	
	public abstract String getName();
	
	public void finish()
	{
		
	}
}
