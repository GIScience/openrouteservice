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
	private List<String> _tollTags = new ArrayList<String>(6);
	
	public TollwaysGraphStorageBuilder() {
		// Currently consider only toll tags relevant to cars or hgvs:
		_tollTags.addAll(Arrays.asList("toll", "toll:hgv", "toll:N1", "toll:N2", "toll:N3",  "toll:motorcar"));
	}

	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");

		_storage = new TollwaysGraphStorage();

		return _storage;
	}

	public void processWay(ReaderWay way) {
		_tollways = TollwayType.None;

		for (String key : _tollTags) {
			if (way.hasTag(key)) {
				String value = way.getTag(key);

				if (value != null) {
					switch(key) {
						case "toll":
							setFlag(TollwayType.General, value);
							break;
						case "toll:hgv":
							setFlag(TollwayType.Hgv, value);
							break;
						case "toll:N1": //currently not used in OSM
							setFlag(TollwayType.N1, value);
							break;
						case "toll:N2":
							setFlag(TollwayType.N2, value);
							break;
						case "toll:N3":
							setFlag(TollwayType.N3, value);
							break;
						case "toll:motorcar":
							setFlag(TollwayType.Motorcar, value);
						default:
							break;
					}
				}
			}
		}

	}

	private void setFlag(int flag, String value) {
		switch(value) {
			case "yes":
				_tollways |= flag;
				break;
			case "no":
				_tollways &= ~flag;
				break;
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		_storage.setEdgeValue(edge.getEdge(), _tollways);
	}

	@Override
	public String getName() {
		return "Tollways";
	}
}
