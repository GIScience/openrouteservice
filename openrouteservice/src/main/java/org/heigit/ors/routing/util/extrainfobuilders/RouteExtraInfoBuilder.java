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
package org.heigit.ors.routing.util.extrainfobuilders;

import com.graphhopper.util.PointList;
import org.heigit.ors.routing.RouteExtraInfo;

public abstract class RouteExtraInfoBuilder {
    protected RouteExtraInfo extraInfo;
    
    RouteExtraInfoBuilder(RouteExtraInfo extraInfo) {
    	if (extraInfo == null)
    		throw new NullPointerException("extraInfo");
    	this.extraInfo = extraInfo;
    }
    
    public abstract void addSegment(double value, long valueIndex, PointList geom, double dist, boolean lastEdge);

    public abstract void addSegment(double value, long valueIndex, PointList geom, double dist);

    public abstract void finish();
}
