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
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.routing.graphhopper.extensions.TollwayType;
import heigit.ors.routing.graphhopper.extensions.storages.TollwaysGraphStorage;

public class TollwaysGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private TollwaysGraphStorage _storage;
	private int _tollways;
	private List<String> _tollTags = new ArrayList<String>(7);
	
	public TollwaysGraphStorageBuilder()
	{
		_tollTags.addAll(Arrays.asList("toll", "toll:hgv", "toll:N1", "toll:N2", "toll:N3",  "toll:class", "toll:hgv:class"));
	}

	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");

		_storage = new TollwaysGraphStorage();

		return _storage;
	}

	public void processWay(ReaderWay way) {
		_tollways = TollwayType.None;

		String tollKey = null;
		String tollValue = null;
		
		for (String key : _tollTags) {
			if (way.hasTag(key))
			{
				tollKey = key;
				tollValue = way.getTag(key);
				break;
			}
		} 
		
		/*
        toll=yes
		toll:class= L1, L2, ....M 
		toll:hgv=yes
		toll:hgv:class=N1, N2, N3 
        */
		if (tollValue != null && !tollValue.equals("no"))
		{
			switch(tollKey)
			{
			case "toll":
				_tollways = TollwayType.General;
				break;
			case "toll:hgv":
				_tollways = TollwayType.N;
				break;
			case "toll:N1":
				_tollways = TollwayType.N1;
				break;
			case "toll:N2":
				_tollways = TollwayType.N2;
				break;
			case "toll:N3":
				_tollways = TollwayType.N3;
				break;
			case "toll:hgv:class":
				if (tollValue.equals("yes"))
					_tollways = TollwayType.N;
				else
					_tollways = getTollValues(tollValue);
				break;
			case "toll:class":
				_tollways = getTollValues(tollValue);
				break;
			default:
				_tollways = TollwayType.getFromString(tollValue);
				break;
			}
		}
	}
	
	private int getTollValues(String value)
	{
		if (value.contains(","))
		{
			int res = TollwayType.None;
			String[] values = value.split(",");
			for (String v : values)
			{
				res |= TollwayType.getFromString(v.trim());
			}
			
			return res;
		}
		
		return TollwayType.getFromString(value);
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge)
	{ 
		_storage.setEdgeValue(edge.getEdge(), _tollways);
	}

	@Override
	public String getName() {
		return "Tollways";
	}
}
