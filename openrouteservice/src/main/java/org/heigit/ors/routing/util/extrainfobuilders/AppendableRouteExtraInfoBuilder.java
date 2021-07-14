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

public class AppendableRouteExtraInfoBuilder extends SimpleRouteExtraInfoBuilder {
	private final ArrayList<SegmentParams> segmentParamsList;

    public AppendableRouteExtraInfoBuilder(RouteExtraInfo extraInfo) {
    	super(extraInfo);
		segmentParamsList = new ArrayList<>();
	}

	@Override
	public void addSegment(double value, long valueIndex, PointList geom, double dist) {
    	segmentParamsList.add(new SegmentParams(value, valueIndex, geom, dist));
    }

	public List<SegmentParams> getSegmentParamsList() {
		return segmentParamsList;
	}

	public void append(AppendableRouteExtraInfoBuilder more) {
		this.segmentParamsList.addAll(more.getSegmentParamsList());
	}

	@Override
	public void finish() {
		for (Iterator<SegmentParams> it = segmentParamsList.iterator(); it.hasNext(); ){
			SegmentParams s = it.next();
			super.addSegment(s.value, s.valueIndex, s.geom, s.dist, !it.hasNext());
		}
	}
	
	private class SegmentParams {
		double value;
		long valueIndex;
		PointList geom;
		double dist;
		SegmentParams(double value, long valueIndex, PointList geom, double dist) {
			this.value = value;
			this.valueIndex = valueIndex;
			this.geom = geom;
			this.dist = dist;
		}
	}
}
