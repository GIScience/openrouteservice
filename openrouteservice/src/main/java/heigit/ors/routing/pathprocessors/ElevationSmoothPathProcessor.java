/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.pathprocessors;

import com.graphhopper.routing.PathProcessingContext;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import heigit.ors.routing.util.ElevationSmoother;

public class ElevationSmoothPathProcessor extends PathProcessor {
	public ElevationSmoothPathProcessor()
	{

	}

	@Override
	public void init(FlagEncoder enc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSegmentIndex(int index, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processEdge(EdgeIteratorState edge, boolean isLastEdge, PointList geom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public PointList processPoints(PointList points) {
		return ElevationSmoother.smooth(points);
	}
}
