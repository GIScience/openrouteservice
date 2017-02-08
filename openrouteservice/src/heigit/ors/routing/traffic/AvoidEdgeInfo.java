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
package org.freeopenls.routeservice.traffic;

public class AvoidEdgeInfo extends EdgeInfo {
   private float mSpeedFactor;
   
   public AvoidEdgeInfo(Integer edgeId, short[] codes, float speedFactor) {
	  super(edgeId, codes);
	   
	   mSpeedFactor = speedFactor;
   }
   
   public float getSpeedFactor() {
	   return mSpeedFactor;
   }
}
