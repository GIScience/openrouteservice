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
package heigit.ors.routing.graphhopper.extensions.graphbuilders;

import java.util.List;
import java.util.Map;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.OSMReader;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.util.EdgeIteratorState;

import gnu.trove.list.TLongList;
import heigit.ors.plugins.Plugin;

public interface GraphBuilder extends Plugin {
	void init(GraphHopper graphhopper) throws Exception;
	
	boolean createEdges(OSMReader reader, OSMWay way, TLongList osmNodeIds, long wayFlags, List<EdgeIteratorState> createdEdges) throws Exception;
	
	void finish();
	
	String getName();
	
	void setParameters(Map<String, String> parameters);
}
