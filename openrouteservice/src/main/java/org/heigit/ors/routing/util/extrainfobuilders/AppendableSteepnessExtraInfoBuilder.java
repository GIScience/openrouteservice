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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppendableSteepnessExtraInfoBuilder extends SteepnessExtraInfoBuilder {
	private final ArrayList<PointList> segmentPointLists;

    public AppendableSteepnessExtraInfoBuilder(RouteExtraInfo extraInfo) {
    	super(extraInfo);
		segmentPointLists = new ArrayList<>();
	}

	public List<PointList> getSegmentPointLists() {
		return segmentPointLists;
	}

	public void append(AppendableSteepnessExtraInfoBuilder more) {
		this.segmentPointLists.addAll(more.getSegmentPointLists());
	}

	@Override
	public void addPoints(PointList geom) {
    	this.segmentPointLists.add(geom);
	}
	
	@Override
	public void finish() {
		for (Iterator<PointList> it = segmentPointLists.iterator(); it.hasNext(); ){
			PointList s = it.next();
			super.addSegment(0, 0, null, 0, !it.hasNext());
			super.addPoints(s);
		}
	}
}
