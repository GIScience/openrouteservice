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
package heigit.ors.isochrones.builders.grid;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.MMapDirectory;
import com.graphhopper.storage.index.Location2IDQuadtree;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.StopWatch;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.builders.AbstractIsochroneMapBuilder;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.algorithms.MatrixAlgorithm;
import heigit.ors.matrix.algorithms.MatrixAlgorithmFactory;
import heigit.ors.routing.RouteSearchContext;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class GridBasedIsochroneMapBuilder extends AbstractIsochroneMapBuilder 
{
	private final Logger LOGGER = Logger.getLogger(GridBasedIsochroneMapBuilder.class.getName());

	private GeometryFactory _geomFactory;
	private RouteSearchContext _searchContext;
	
	private static LocationIndex _gridIndex;
	
	private double _grdiStep = 500; // measured in meters

	public GridBasedIsochroneMapBuilder() 
	{
		
	}

	@Override
	public void initialize(RouteSearchContext searchContext) 
	{
		_geomFactory = new GeometryFactory();
		_searchContext = searchContext;		
	}

	@Override
	public IsochroneMap compute(IsochroneSearchParameters parameters) throws Exception {
		StopWatch swTotal = null;
		StopWatch sw = null;
		
		if (LOGGER.isDebugEnabled())
		{
			swTotal = new StopWatch();
			swTotal.start();
			sw = new StopWatch();
			sw.start();
		}

		FlagEncoder encoder = _searchContext.getEncoder();
		// 1. Find all graph edges for a given cost.
		double maxSpeed = encoder.getMaxSpeed();

		Coordinate loc = parameters.getLocation();
		GraphHopper gh = _searchContext.getGraphHopper();
		//LocationIndexMatch index = new LocationIndexMatch(gh.getGraphHopperStorage(),(LocationIndexTree)gh.getLocationIndex()); 
		//gh.getGraphHopperStorage().getBounds()
		//index.setGpxAccuracy(500);
     	//index.setMinResolutionInMeter(200);
		
		if (_gridIndex == null)
		{
			_gridIndex = new Location2IDQuadtree(gh.getGraphHopperStorage().getGraph(Graph.class), new MMapDirectory(gh.getGraphHopperLocation() + "grid_loc2idIndex").create()).
	                setResolution(500).prepareIndex();
		}
		
		int gridSizeMeters = 500;
		int[] gridValues = new int[gridSizeMeters*gridSizeMeters];
		double cx = loc.x;
		double cy = loc.y;
		double gridSizeY = Math.toDegrees(gridSizeMeters / 6378100.0);
		double gridSizeX = gridSizeY / Math.cos(Math.toRadians(cx));
		double halfN = gridSizeMeters/2;
		List<Coordinate> gridLocations = new ArrayList<Coordinate>(gridValues.length);
		
		for (int xi = 0; xi < gridSizeMeters; xi++)
		{
			double dx = (-halfN + xi)*gridSizeX;
			
			for (int yi = 0; yi< gridSizeMeters; yi++)
			{
				double dy = (-halfN + yi)*gridSizeX;
				
				int p = xi + yi*gridSizeMeters;

				QueryResult res = _gridIndex.findClosest(cy + dy, cx + dx, EdgeFilter.ALL_EDGES);
				if (res.isValid())
					gridValues[p] = res.getClosestNode();
				else
					gridValues[p] = -1;
				
				gridLocations.add(new Coordinate(cx + dx, cy + dy));
			}
		}
		
		
		MatrixRequest mtxReq = new MatrixRequest();
		mtxReq.setMetrics(MatrixMetricsType.Distance);
		mtxReq.setFlexibleMode(true);
		mtxReq.setSources(new Coordinate[] { parameters.getLocation() });
        Coordinate[] destinations = new Coordinate[gridLocations.size()];
        gridLocations.toArray(destinations);
		mtxReq.setDestinations(destinations); 

		MatrixAlgorithm alg = MatrixAlgorithmFactory.createAlgorithm(mtxReq, gh, encoder);
		
		if (alg == null)
			throw new Exception("Unable to create an algorithm to distance/duration matrix.");

		//alg.init(mtxReq, gh, _searchContext.getEncoder());

		//MatrixLocationDataResolver locResolver = new MatrixLocationDataResolver(gh.getLocationIndex(), new DefaultEdgeFilter(encoder), new ByteArrayBuffer(), mtxReq.getResolveLocations(), 2000);

	/*	MatrixSearchData srcData = locResolver.resolve(mtxReq.getSources());
		MatrixSearchData dstData = locResolver.resolve(mtxReq.getDestinations()); 

		MatrixResult mtxResult = alg.compute(srcData, dstData, mtxReq.getMetrics()); 
		*/
		IsochroneMap isochroneMap = new IsochroneMap(0, loc);

		//AccessibilityMap edgeMap = GraphEdgeMapFinder.findEdgeMap(_searchContext, parameters);

		if (LOGGER.isDebugEnabled())
		{
			sw.stop();

			LOGGER.debug("Find edges: " + sw.getSeconds());
		}
		

		return isochroneMap;
	}
}
