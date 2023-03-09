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
package org.heigit.ors.matrix;

import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.common.NamedLocation;

public class ResolvedLocation extends NamedLocation {
   private final double snappedDistance;
   
   public ResolvedLocation(Coordinate coord, String name, double snappedDistance) {
	   super(coord, name);
	   this.snappedDistance = snappedDistance;
   }
   
   public double getSnappedDistance()
   {
	   return snappedDistance;
   }
}
