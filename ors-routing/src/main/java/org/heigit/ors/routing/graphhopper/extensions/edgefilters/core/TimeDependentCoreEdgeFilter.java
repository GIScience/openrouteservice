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
package org.heigit.ors.routing.graphhopper.extensions.edgefilters.core;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.ConditionalEdges;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;

import java.util.ArrayList;
import java.util.List;


public class TimeDependentCoreEdgeFilter implements EdgeFilter {
	private BooleanEncodedValue[] conditionalEncoders;
	private static String[] names = {ConditionalEdges.ACCESS, ConditionalEdges.SPEED};

	public TimeDependentCoreEdgeFilter(GraphHopperStorage graphStorage) {
		EncodingManager encodingManager = graphStorage.getEncodingManager();

		List<BooleanEncodedValue> conditionalEncodersList = new ArrayList<>();

		for (FlagEncoder encoder : encodingManager.fetchEdgeEncoders()) {
			for (String name : names) {
				String encoderName = EncodingManager.getKey(encoder, name);
				if (encodingManager.hasEncodedValue(encoderName)) {
					conditionalEncodersList.add(encodingManager.getBooleanEncodedValue(encoderName));
				}
			}
		}

		conditionalEncoders = conditionalEncodersList.toArray(new BooleanEncodedValue[conditionalEncodersList.size()]);
	}

	public static boolean hasConditionals(EncodingManager encodingManager) {
		for (FlagEncoder encoder : encodingManager.fetchEdgeEncoders())
			for (String name : names) {
				String encoderName = EncodingManager.getKey(encoder, name);
				if (encodingManager.hasEncodedValue(encoderName)) {
					return true;
				}
			}
		return false;
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {
		for (BooleanEncodedValue conditionalEncoder: conditionalEncoders) {
			if (iter.get(conditionalEncoder)) {
				return false;
			}
		}
		return true;
	}

}
