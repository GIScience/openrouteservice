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
package heigit.ors.routing.graphhopper.extensions;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.carrotsearch.hppc.LongArrayList;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.util.EdgeIteratorState;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import com.vividsolutions.jts.geom.LineString;
import heigit.ors.plugins.PluginManager;
import heigit.ors.routing.configuration.RouteProfileConfiguration;
import heigit.ors.routing.graphhopper.extensions.graphbuilders.GraphBuilder;
import heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;

public class GraphProcessContext {
	private static Logger LOGGER = Logger.getLogger(GraphProcessContext.class.getName());

	private Envelope _bbox;
	private List<GraphBuilder> _graphBuilders;
	private GraphBuilder[] _arrGraphBuilders;
	private List<GraphStorageBuilder> _storageBuilders;
	private GraphStorageBuilder[] _arrStorageBuilders;

	public GraphProcessContext(RouteProfileConfiguration config) throws Exception
	{
		_bbox = config.getExtent();
		PluginManager<GraphStorageBuilder> mgrGraphStorageBuilders = PluginManager.getPluginManager(GraphStorageBuilder.class);

		if (config.getExtStorages() != null)
		{
			_storageBuilders = mgrGraphStorageBuilders.createInstances(config.getExtStorages());

			if (_storageBuilders != null && _storageBuilders.size() > 0)
			{
				_arrStorageBuilders = new GraphStorageBuilder[_storageBuilders.size()];
				_arrStorageBuilders = _storageBuilders.toArray(_arrStorageBuilders);
			}
		}

		PluginManager<GraphBuilder> mgrGraphBuilders = PluginManager.getPluginManager(GraphBuilder.class);
		if (config.getGraphBuilders() != null)
		{
			_graphBuilders  = mgrGraphBuilders.createInstances(config.getGraphBuilders());
			if (_graphBuilders != null && _graphBuilders.size() > 0)
			{
				_arrGraphBuilders = new GraphBuilder[_graphBuilders.size()];
				_arrGraphBuilders = _graphBuilders.toArray(_arrGraphBuilders);
			}
		}
	}

	public void init(GraphHopper gh)
	{
		if (_graphBuilders != null && _graphBuilders.size() > 0)
		{
			for(GraphBuilder builder : _graphBuilders)
			{
				try
				{
					builder.init(gh);
				}
				catch(Exception ex)
				{
					LOGGER.warning(ex.getMessage());
				}
			}
		}
	}

	public List<GraphStorageBuilder> getStorageBuilders()
	{
		return _storageBuilders;
	}

	public void processWay(ReaderWay way) 
	{
		try
		{
			if (_arrStorageBuilders != null)
			{
				int nStorages = _arrStorageBuilders.length;
				if (nStorages > 0)
				{
					if (nStorages == 1)
					{
						_arrStorageBuilders[0].processWay(way);
					}
					else if (nStorages == 2)
					{
						_arrStorageBuilders[0].processWay(way);
						_arrStorageBuilders[1].processWay(way);
					}
					else if (nStorages == 3)
					{
						_arrStorageBuilders[0].processWay(way);
						_arrStorageBuilders[1].processWay(way);
						_arrStorageBuilders[2].processWay(way);
					}
					else  if (nStorages == 4)
					{
						_arrStorageBuilders[0].processWay(way);
						_arrStorageBuilders[1].processWay(way);
						_arrStorageBuilders[2].processWay(way);
						_arrStorageBuilders[3].processWay(way);
					}
					else
					{		
						for (int i = 0; i < nStorages; ++i)
						{
							_arrStorageBuilders[i].processWay(way);
						}
					}
				}
			}
		}
		catch(Exception ex)
		{
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
		try
		{
			if (_arrStorageBuilders != null)
			{
				int nStorages = _arrStorageBuilders.length;
				if (nStorages > 0)
				{
					for (int i = 0; i < nStorages; ++i)
					{
						_arrStorageBuilders[i].processWay(way, coords, nodeTags);
					}

				}
			}
		}
		catch(Exception ex)
		{
			LOGGER.warning(ex.getMessage() + ". Way id = " + way.getId());
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge)
	{
		if (_arrStorageBuilders != null)
		{
			int nStorages = _arrStorageBuilders.length;
			if (nStorages > 0)
			{
				if (nStorages == 1)
				{
					_arrStorageBuilders[0].processEdge(way, edge);
				}
				else if (nStorages == 2)
				{
					_arrStorageBuilders[0].processEdge(way, edge);
					_arrStorageBuilders[1].processEdge(way, edge);
				}
				else if (nStorages == 3)
				{
					_arrStorageBuilders[0].processEdge(way, edge);
					_arrStorageBuilders[1].processEdge(way, edge);
					_arrStorageBuilders[2].processEdge(way, edge);
				}
				else  if (nStorages == 4)
				{
					_arrStorageBuilders[0].processEdge(way, edge);
					_arrStorageBuilders[1].processEdge(way, edge);
					_arrStorageBuilders[2].processEdge(way, edge);
					_arrStorageBuilders[3].processEdge(way, edge);
				}
				else
				{		
					for (int i = 0; i < nStorages; ++i)
					{
						_arrStorageBuilders[i].processEdge(way, edge);
					}
				}
			}
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge, Coordinate[] coords) {
		if(_arrStorageBuilders != null) {
			int nStorages = _arrStorageBuilders.length;
			for(int i=0; i<nStorages; i++) {
				_arrStorageBuilders[i].processEdge(way, edge, coords);
			}
		}
	}

	public boolean createEdges(DataReaderContext readerCntx, ReaderWay way, LongArrayList osmNodeIds, long wayFlags, List<EdgeIteratorState> createdEdges) throws Exception
	{
		if (_arrGraphBuilders != null)
		{
			int nBuilders = _arrGraphBuilders.length;
			if (nBuilders > 0)
			{
				boolean res = false;
				if (nBuilders == 1)
				{
					res = _arrGraphBuilders[0].createEdges(readerCntx, way, osmNodeIds, wayFlags, createdEdges);
				}
				else if (nBuilders == 2)
				{
					res = _arrGraphBuilders[0].createEdges(readerCntx, way, osmNodeIds, wayFlags, createdEdges);
					boolean res2 = _arrGraphBuilders[1].createEdges(readerCntx, way, osmNodeIds, wayFlags, createdEdges);
					if (res2)
						res = res2;
				}
				else
				{		
					for (int i = 0; i < nBuilders; ++i)
					{
						boolean res2 = _arrGraphBuilders[i].createEdges(readerCntx, way, osmNodeIds, wayFlags, createdEdges);
						if (res2)
							res = res2;
					}
				}

				return res;
			}
		}

		return false;
	}

	public boolean isValidPoint(double x, double y)
	{
		if (_bbox == null)
			return true;
		else
			return _bbox.contains(x, y);
	}

	public void finish()
	{
		if (_arrStorageBuilders != null)
		{
			int nStorages = _arrStorageBuilders.length;
			if (nStorages > 0)
			{
				for (int i = 0; i < nStorages; ++i)
					_arrStorageBuilders[i].finish();
			}
		}
	}
}
