/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package org.heigit.ors.isochrones;

import com.graphhopper.util.PointList;

public class EdgeInfo {
   private final PointList geometry;
   private final float v1;
   private final float v2;
   private final float dist;
   private final long edgeId;
   
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
