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

package org.freeopenls.routeservice.isochrones;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class IsochroneMap {
   private Envelope extent;
   private List<Isochrone> isochrones;
   
   public IsochroneMap()
   {
	   isochrones = new ArrayList<Isochrone>();
	   extent = new Envelope();
   }
   
   public boolean isEmpty()
   {
	   return isochrones.size() == 0;
   }
   
   public Iterable<Isochrone> getIsochrones()
   {
	   return isochrones;
   }
   
   public Isochrone getIsochrone(int index)
   {
	   return isochrones.get(index);
   }
   
   public void addIsochrone(Isochrone isochrone)
   {
	   isochrones.add(isochrone);
	   extent.expandToInclude(isochrone.getGeometry().getEnvelopeInternal());
   }
   
   public Envelope getExtent()
   {
	   return extent;
   }
}
