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

package org.freeopenls.routeservice.isochrones.isolinebuilders;

import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Envelope;

public class EdgeInfo {
   private PointList geometry;
   private float v1;
   private float v2;
   private float dist;
   //private Envelope bounds;
   
   public EdgeInfo(PointList geom, float v1, float v2, float dist)
   {
	   this.geometry = geom;
	   this.v1 = v1;
	   this.v2 = v2;
	   this.dist = dist;
   }
   /*
   public boolean hasBounds()
   {
	   return this.bounds != null;
   }
   
   public void setBounds(Envelope env)
   {
	   this.bounds = env;	   
   }
   
   public boolean intersect(Envelope env)
   {
	   if (bounds == null)
		   return true;
	   else
		   return bounds.intersects(env);
   }*/
   
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
