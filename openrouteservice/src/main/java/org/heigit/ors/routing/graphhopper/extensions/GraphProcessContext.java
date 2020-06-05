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
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import org.heigit.ors.plugins.PluginManager;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.routing.graphhopper.extensions.graphbuilders.GraphBuilder;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class GraphProcessContext {
	private static final Logger LOGGER = Logger.getLogger(GraphProcessContext.class.getName());

	private Envelope bbox;
	private List<GraphBuilder> graphBuilders;
	private GraphBuilder[] arrGraphBuilders;
	private List<GraphStorageBuilder> storageBuilders;
	private GraphStorageBuilder[] arrStorageBuilders;
	private double maximumSpeedLowerBound;

	public GraphProcessContext(RouteProfileConfiguration config) throws Exception {
		bbox = config.getExtent();
		PluginManager<GraphStorageBuilder> mgrGraphStorageBuilders = PluginManager.getPluginManager(GraphStorageBuilder.class);

		if (config.getExtStorages() != null) {
			storageBuilders = mgrGraphStorageBuilders.createInstances(config.getExtStorages());

			if (storageBuilders != null && !storageBuilders.isEmpty()) {
				arrStorageBuilders = new GraphStorageBuilder[storageBuilders.size()];
				arrStorageBuilders = storageBuilders.toArray(arrStorageBuilders);
			}
		}

		PluginManager<GraphBuilder> mgrGraphBuilders = PluginManager.getPluginManager(GraphBuilder.class);
		if (config.getGraphBuilders() != null) {
			graphBuilders = mgrGraphBuilders.createInstances(config.getGraphBuilders());
			if (graphBuilders != null && !graphBuilders.isEmpty()) {
				arrGraphBuilders = new GraphBuilder[graphBuilders.size()];
				arrGraphBuilders = graphBuilders.toArray(arrGraphBuilders);
			}
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

	public List<GraphStorageBuilder> getStorageBuilders()
	{
		return storageBuilders;
	}

	public void processWay(ReaderWay way)  {
		try {
			if (arrStorageBuilders != null) {
				int nStorages = arrStorageBuilders.length;
				if (nStorages > 0) {
					if (nStorages == 1) {
						arrStorageBuilders[0].processWay(way);
					} else if (nStorages == 2) {
						arrStorageBuilders[0].processWay(way);
						arrStorageBuilders[1].processWay(way);
					} else if (nStorages == 3) {
						arrStorageBuilders[0].processWay(way);
						arrStorageBuilders[1].processWay(way);
						arrStorageBuilders[2].processWay(way);
					} else if (nStorages == 4) {
						arrStorageBuilders[0].processWay(way);
						arrStorageBuilders[1].processWay(way);
						arrStorageBuilders[2].processWay(way);
						arrStorageBuilders[3].processWay(way);
					} else {
						for (int i = 0; i < nStorages; ++i) {
							arrStorageBuilders[i].processWay(way);
						}
					}
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
	public void processWay(ReaderWay way, Coordinate[] coords, HashMap<Integer, HashMap<String, String>> nodeTags) {
		try {
			if (arrStorageBuilders != null) {
				int nStorages = arrStorageBuilders.length;
				if (nStorages > 0) {
					for (int i = 0; i < nStorages; ++i) {
						arrStorageBuilders[i].processWay(way, coords, nodeTags);
					}
				}
			}
		} catch(Exception ex) {
			LOGGER.warning(ex.getMessage() + ". Way id = " + way.getId());
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		if (arrStorageBuilders != null) {
			int nStorages = arrStorageBuilders.length;
			if (nStorages > 0) {
				if (nStorages == 1) {
					arrStorageBuilders[0].processEdge(way, edge);
				} else if (nStorages == 2) {
					arrStorageBuilders[0].processEdge(way, edge);
					arrStorageBuilders[1].processEdge(way, edge);
				} else if (nStorages == 3) {
					arrStorageBuilders[0].processEdge(way, edge);
					arrStorageBuilders[1].processEdge(way, edge);
					arrStorageBuilders[2].processEdge(way, edge);
				} else if (nStorages == 4) {
					arrStorageBuilders[0].processEdge(way, edge);
					arrStorageBuilders[1].processEdge(way, edge);
					arrStorageBuilders[2].processEdge(way, edge);
					arrStorageBuilders[3].processEdge(way, edge);
				} else {
					for (int i = 0; i < nStorages; ++i) {
						arrStorageBuilders[i].processEdge(way, edge);
					}
				}
			}
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge, Coordinate[] coords) {
		if(arrStorageBuilders != null) {
			int nStorages = arrStorageBuilders.length;
			for(int i=0; i<nStorages; i++) {
				arrStorageBuilders[i].processEdge(way, edge, coords);
			}
		}
	}

	public boolean createEdges(DataReaderContext readerCntx, ReaderWay way, LongArrayList osmNodeIds, IntsRef wayFlags, List<EdgeIteratorState> createdEdges) throws Exception {
		if (arrGraphBuilders != null) {
			int nBuilders = arrGraphBuilders.length;
			if (nBuilders > 0) {
				boolean res = false;
				if (nBuilders == 1) {
					res = arrGraphBuilders[0].createEdges(readerCntx, way, osmNodeIds, wayFlags, createdEdges);
				} else if (nBuilders == 2) {
					res = arrGraphBuilders[0].createEdges(readerCntx, way, osmNodeIds, wayFlags, createdEdges);
					boolean res2 = arrGraphBuilders[1].createEdges(readerCntx, way, osmNodeIds, wayFlags, createdEdges);
					if (res2)
						res = res2;
				} else {
					for (int i = 0; i < nBuilders; ++i) {
						boolean res2 = arrGraphBuilders[i].createEdges(readerCntx, way, osmNodeIds, wayFlags, createdEdges);
						if (res2)
							res = res2;
					}
				}
				return res;
			}
		}
		return false;
	}

	public boolean isValidPoint(double x, double y) {
		if (bbox == null)
			return true;
		else
			return bbox.contains(x, y);
	}

	public void finish() {
		if (arrStorageBuilders != null) {
			int nStorages = arrStorageBuilders.length;
			if (nStorages > 0) {
				for (int i = 0; i < nStorages; ++i)
					arrStorageBuilders[i].finish();
			}
		}
	}

	public double getMaximumSpeedLowerBound(){
		return maximumSpeedLowerBound;
	}
}
