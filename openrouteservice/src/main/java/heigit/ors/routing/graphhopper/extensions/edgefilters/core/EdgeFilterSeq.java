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
package heigit.ors.routing.graphhopper.extensions.edgefilters.core;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.EdgeIteratorState;

public class EdgeFilterSeq implements EdgeFilter {
	protected final boolean _in;
	protected final boolean _out;
	protected final FlagEncoder _encoder;
	private EdgeFilter _prevFilter;

	protected EdgeFilterSeq(FlagEncoder encoder, boolean in, boolean out, EdgeFilter prevFilter) {
		_encoder = encoder;
		_in = in;
		_out = out;
		_prevFilter = prevFilter;
	}

	public final void setPrevFilter(EdgeFilter prevFilter) {
		_prevFilter = prevFilter;
	}

	public final EdgeFilter getPrevFilter() {
		return _prevFilter;
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {
		if (_prevFilter.accept(iter)) {
			return check(iter);
		} else {
			return false;
		}
	}

	protected boolean check(EdgeIteratorState iter) {
		return true;
	}

}
