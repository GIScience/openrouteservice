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
package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import org.heigit.ors.routing.graphhopper.extensions.storages.ExpiringSpeedStorage;

import java.util.List;

public class ExpiringSpeedGraphStorageBuilder extends AbstractGraphStorageBuilder {
	private ExpiringSpeedStorage storage;

	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (storage != null)
			throw new IllegalStateException("GraphStorageBuilder has been already initialized.");

		// extract profiles from GraphHopper instance
		EncodingManager encMgr = graphhopper.getEncodingManager();
		List<FlagEncoder> encoders = encMgr.fetchEdgeEncoders();
		FlagEncoder flagEncoder = encoders.get(0);

		storage = new ExpiringSpeedStorage(flagEncoder);
		if (parameters != null) {
			String value = parameters.get("expiration_min");
			if (!Helper.isEmpty(value))
				storage.setDefaultExpirationTime(Integer.parseInt(value));
		}
		return storage;
	}

	@Override
	public void processWay(ReaderWay way) {
		//No processing on input data
	}

	@Override
	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		//No processing on input data
	}

	@Override
	public String getName() {
		return "ExpiringSpeed";
	}
}
