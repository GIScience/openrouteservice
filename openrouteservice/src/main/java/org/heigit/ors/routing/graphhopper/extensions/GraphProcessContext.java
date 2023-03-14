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
package org.heigit.ors.routing.graphhopper.extensions;

import com.carrotsearch.hppc.LongArrayList;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.EdgeIteratorState;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.plugins.PluginManager;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.routing.graphhopper.extensions.graphbuilders.GraphBuilder;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.HereTrafficGraphStorageBuilder;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GraphProcessContext {
	private static final Logger LOGGER = Logger.getLogger(GraphProcessContext.class.getName());

	private final Envelope bbox;
	private List<GraphBuilder> graphBuilders;
	private GraphBuilder[] arrGraphBuilders;
	private List<GraphStorageBuilder> storageBuilders;
	private GraphStorageBuilder[] arrStorageBuilders;
	private int trafficArrStorageBuilderLocation = -1;
	private final double maximumSpeedLowerBound;

	private boolean getElevationFromPreprocessedData;

	public GraphProcessContext(RouteProfileConfiguration config) throws Exception {
		bbox = config.getExtent();
		PluginManager<GraphStorageBuilder> mgrGraphStorageBuilders = PluginManager.getPluginManager(GraphStorageBuilder.class);

		if (config.getExtStorages() != null) {
			storageBuilders = mgrGraphStorageBuilders.createInstances(config.getExtStorages());
		}

		PluginManager<GraphBuilder> mgrGraphBuilders = PluginManager.getPluginManager(GraphBuilder.class);
		if (config.getGraphBuilders() != null) {
			graphBuilders = mgrGraphBuilders.createInstances(config.getGraphBuilders());
		}

		maximumSpeedLowerBound = config.getMaximumSpeedLowerBound();
	}

	public void init(GraphHopper gh) {
		if (graphBuilders != null && !graphBuilders.isEmpty()) {
			for(GraphBuilder builder : graphBuilders) {
				try {
					builder.init(gh);
				} catch(Exception ex) {
					LOGGER.warning(ex.getMessage());
				}
			}
		}
	}

	public void initArrays() {
		if (storageBuilders != null && !storageBuilders.isEmpty()) {
			arrStorageBuilders = new GraphStorageBuilder[storageBuilders.size()];
			arrStorageBuilders = storageBuilders.toArray(arrStorageBuilders);
		}
		if (graphBuilders != null && !graphBuilders.isEmpty()) {
			arrGraphBuilders = new GraphBuilder[graphBuilders.size()];
			arrGraphBuilders = graphBuilders.toArray(arrGraphBuilders);
		}
	}

	public List<GraphStorageBuilder> getStorageBuilders()
	{
		return storageBuilders;
	}

	public void processWay(ReaderWay way)  {
		try {
			if (arrStorageBuilders != null) {
				for (GraphStorageBuilder builder: arrStorageBuilders) {
					builder.processWay(way);
				}
			}
		} catch(Exception ex) {
			LOGGER.warning(ex.getMessage() + ". Way id = " + way.getId());
		}
	}

	/**
	 * Pass the way read along with its geometry (a LineString) to the graph storage builders.
	 *
	 * @param way		The OSM data for the way (including tags)
	 * @param coords	Coordinates of the linestring
	 * @param nodeTags  Tags for nodes found on the way
	 */
	public void processWay(ReaderWay way, Coordinate[] coords, Map<Integer, Map<String, String>> nodeTags, Coordinate[] allCoordinates) {
		try {
			if (arrStorageBuilders != null) {
				int nStorages = arrStorageBuilders.length;
				if (nStorages > 0) {
					for (int i = 0; i < nStorages; ++i) {
						if (trafficArrStorageBuilderLocation == -1  && arrStorageBuilders[i].getName().equals(HereTrafficGraphStorageBuilder.BUILDER_NAME)){
							trafficArrStorageBuilderLocation = i;
						}
						arrStorageBuilders[i].processWay(way, coords, nodeTags);
					}
					if (trafficArrStorageBuilderLocation >= 0){
						arrStorageBuilders[trafficArrStorageBuilderLocation].processWay(way, allCoordinates, nodeTags);
					}
				}
			}
		} catch(Exception ex) {
			LOGGER.warning(ex.getMessage() + ". Way id = " + way.getId());
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		if (arrStorageBuilders != null) {
			for (GraphStorageBuilder builder: arrStorageBuilders) {
				builder.processEdge(way, edge);
			}
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge, Coordinate[] coords) {
		if(arrStorageBuilders != null) {
			for (GraphStorageBuilder builder: arrStorageBuilders) {
				builder.processEdge(way, edge, coords);
			}
		}
	}

	public boolean createEdges(DataReaderContext readerCntx, ReaderWay way, LongArrayList osmNodeIds, IntsRef wayFlags, List<EdgeIteratorState> createdEdges) throws Exception {
		boolean res = false;
		if (arrGraphBuilders != null) {
			for (GraphBuilder builder: arrGraphBuilders) {
				res |= builder.createEdges(readerCntx, way, osmNodeIds, wayFlags, createdEdges);
			}
		}
		return res;
	}

	public boolean isValidPoint(double x, double y) {
		if (bbox == null)
			return true;
		else
			return bbox.contains(x, y);
	}

	public void finish() {
		if (arrStorageBuilders != null) {
			for (GraphStorageBuilder builder: arrStorageBuilders) {
				builder.finish();
			}
		}
	}

	public double getMaximumSpeedLowerBound(){
		return maximumSpeedLowerBound;
	}

	public void setGetElevationFromPreprocessedData(boolean getElevationFromPreprocessedData) {
		this.getElevationFromPreprocessedData = getElevationFromPreprocessedData;
	}

	public boolean getElevationFromPreprocessedData() {
		return getElevationFromPreprocessedData;
	}
}
