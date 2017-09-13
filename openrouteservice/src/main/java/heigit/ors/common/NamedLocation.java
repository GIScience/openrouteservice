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
package heigit.ors.common;

import com.vividsolutions.jts.geom.Coordinate;

public class NamedLocation {
   private Coordinate _coordinate;
   private String _name;
   
   public NamedLocation(Coordinate coord, String name)
   {
	   _coordinate = coord;
	   _name = name;
   }
   
   public Coordinate getCoordinate()
   {
	   return _coordinate;
   }
   
   public String getName()
   {
	   return _name;
   }
}
