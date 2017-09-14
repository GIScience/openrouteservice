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
package heigit.ors.routing.graphhopper.extensions.util;

public class OSMTagsEncoder {

	public static int getTrackValue(String trackType) {
		if ("grade1".equals(trackType))
			return 1;
		else if ("grade2".equals(trackType))
			return 2;
		else if ("grade3".equals(trackType))
			return 3;
		else if ("grade4".equals(trackType))
			return 4;
		else if ("grade5".equals(trackType))
			return 5;
		else if ("grade6".equals(trackType))
			return 6;
		else if ("grade7".equals(trackType))
			return 7;
		else if ("grade8".equals(trackType))
			return 8;
		else
			return 0;
	}

	public static int getSmoothnessValue(String smoothness) {
		if ("excellent".equals(smoothness))
			return 1;
		else if ("good".equals(smoothness))
			return 2;
		else if ("intermediate".equals(smoothness))
			return 3;
		else if ("bad".equals(smoothness))
			return 4;
		else if ("very_bad".equals(smoothness))
			return 5;
		else if ("horrible".equals(smoothness))
			return 6;
		else if ("very_horrible".equals(smoothness))
			return 7;
		else if ("impassable".equals(smoothness))
			return 8;
		else
			return 0;
	}
	
	public static int getSurfaceValue(String surface) {
		if ("paved".equals(surface))
			return 1;
		else if ("asphalt".equals(surface))
			return 2;
		else if ("cobblestone".equals(surface))
			return 3;
		else if ("cobblestone:flattened".equals(surface))
			return 4;
		else if ("concrete".equals(surface))
			return 5;
		else if ("concrete:lanes".equals(surface))
			return 6;
		else if ("concrete:plates".equals(surface))
			return 7;
		else if ("paving_stones".equals(surface))
			return 8;
		else
			return 0;
	}
}
