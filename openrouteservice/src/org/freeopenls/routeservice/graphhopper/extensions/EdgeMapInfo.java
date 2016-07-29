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

// Authors: M. Rylov

package org.freeopenls.routeservice.graphhopper.extensions;

import gnu.trove.map.TIntObjectMap;

import com.graphhopper.storage.EdgeEntry;

public class EdgeMapInfo {
	private TIntObjectMap<EdgeEntry> map;
	private EdgeEntry edgeEntry;
	
	public EdgeMapInfo(TIntObjectMap<EdgeEntry> map, EdgeEntry edgeEntry)
	{
		this.map = map;
		this.edgeEntry = edgeEntry;
	}
	
	public boolean isEmpty()
	{
		return map.size() == 0;
	}
	
	public TIntObjectMap<EdgeEntry> getMap()
	{
		return map;
	}
	
	public EdgeEntry getEdgeEntry()
	{
		return edgeEntry;
	}
}
