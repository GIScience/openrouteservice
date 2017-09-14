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
package heigit.ors.isochrones;

import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.builders.IsochroneMapBuilder;
import heigit.ors.isochrones.builders.concaveballs.ConcaveBallsIsochroneMapBuilder;
import heigit.ors.isochrones.builders.grid.GridBasedIsochroneMapBuilder;
import heigit.ors.routing.RouteSearchContext;

import com.graphhopper.util.Helper;

public class IsochroneMapBuilderFactory {
	private RouteSearchContext _searchContext;

	public IsochroneMapBuilderFactory(RouteSearchContext searchContext) {
		_searchContext = searchContext;
	}

	public IsochroneMap buildMap(IsochroneSearchParameters parameters) throws Exception {
		IsochroneMapBuilder isochroneBuilder = null;

		String method = parameters.getCalcMethod();

		if (Helper.isEmpty(method) || "Default".equalsIgnoreCase(method) || "ConcaveBalls".equalsIgnoreCase(method)) {
			isochroneBuilder = new ConcaveBallsIsochroneMapBuilder();
		} 
        else if ("grid".equalsIgnoreCase(method))
        {
        	isochroneBuilder= new GridBasedIsochroneMapBuilder();
        }
        else
        {
			throw new Exception("Unknown method.");
		}
		
		isochroneBuilder.initialize(_searchContext);
		return isochroneBuilder.compute(parameters);
	}
}
