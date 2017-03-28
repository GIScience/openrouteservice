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

package heigit.ors.isochrones;

import com.graphhopper.util.PointList;

public class EdgeInfo {
   private PointList geometry;
   private float v1;
   private float v2;
   private float dist;
   private long edgeId;
   
   public EdgeInfo(long id, PointList geom, float v1, float v2, float dist)
   {
	   this.edgeId = id;
	   this.geometry = geom;
	   this.v1 = v1;
	   this.v2 = v2;
	   this.dist = dist;
   }
   
   public long getEdge()
   {
	   return edgeId;
   }
    
   public PointList getGeometry()
   {
	   return this.geometry;
   }
   
   public float getV1()
   {
	   return this.v1;
   }
   
   public float getV2()
   {
	   return this.v2;
   }
   
   public float getDistance()
   {
	   return this.dist;
   }
}
