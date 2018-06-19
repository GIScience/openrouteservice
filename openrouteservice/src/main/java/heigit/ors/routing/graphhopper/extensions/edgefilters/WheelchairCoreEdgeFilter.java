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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;

public class WheelchairCoreEdgeFilter implements EdgeFilter {
	private final boolean in;
	private final boolean out;
	private FlagEncoder encoder;
	private byte[] _buffer;
	private WheelchairAttributesGraphStorage _storage;
	private WheelchairAttributes _attributes;

	public WheelchairCoreEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage) throws Exception {
		this(encoder, true, true, graphStorage);
	}

	public WheelchairCoreEdgeFilter(FlagEncoder encoder, boolean in, boolean out, GraphStorage graphStorage) throws Exception {
		this.encoder = encoder;
		this.in = in;
		this.out = out;

		_storage = GraphStorageUtils.getGraphExtension(graphStorage, WheelchairAttributesGraphStorage.class);

		if (_storage == null)
			throw new Exception("ExtendedGraphStorage for wheelchair attributes was not found.");

		_attributes = new WheelchairAttributes();
		_buffer = new byte[3];
	}

	@Override
	public boolean accept(EdgeIteratorState iter) {
		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder)) {
			_storage.getEdgeValues(iter.getEdge(), _attributes, _buffer);
			
			return !_attributes.hasValues();
		}

		return false;
	}
}
