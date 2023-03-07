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
package org.heigit.ors.isochrones.builders.grid;

import com.graphhopper.GraphHopper;
import com.graphhopper.storage.MMapDirectory;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.StopWatch;
import org.locationtech.jts.geom.Coordinate;
import org.apache.log4j.Logger;
import org.heigit.ors.isochrones.IsochroneMap;
import org.heigit.ors.isochrones.IsochroneSearchParameters;
import org.heigit.ors.isochrones.builders.IsochroneMapBuilder;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixRequest;
import org.heigit.ors.matrix.algorithms.MatrixAlgorithm;
import org.heigit.ors.matrix.algorithms.MatrixAlgorithmFactory;
import org.heigit.ors.routing.RouteSearchContext;

import java.util.ArrayList;
import java.util.List;

//TODO Refactoring : can be probably removed altogether
public class GridBasedIsochroneMapBuilder implements IsochroneMapBuilder {
	private static final Logger LOGGER = Logger.getLogger(GridBasedIsochroneMapBuilder.class.getName());

//	private GeometryFactory geometryFactory;
	private RouteSearchContext searchContext;
	
	private LocationIndex gridIndex;
	
	@Override
	public void initialize(RouteSearchContext searchContext)
	{
//		geometryFactory = new GeometryFactory();
		this.searchContext = searchContext;
	}

	@Override
	public IsochroneMap compute(IsochroneSearchParameters parameters) throws Exception {
		StopWatch swTotal;
		StopWatch sw = null;
		
		if (LOGGER.isDebugEnabled()) {
			swTotal = new StopWatch();
			swTotal.start();
			sw = new StopWatch();
			sw.start();
		}

//		FlagEncoder encoder = searchContext.getEncoder();
		// 1. Find all graph edges for a given cost.
		Coordinate loc = parameters.getLocation();
		GraphHopper gh = searchContext.getGraphHopper();
		//LocationIndexMatch index = new LocationIndexMatch(gh.getGraphHopperStorage(),(LocationIndexTree)gh.getLocationIndex());
		//gh.getGraphHopperStorage().getBounds()
		//index.setGpxAccuracy(500);
     	//index.setMinResolutionInMeter(200);

		if (gridIndex == null) {
			gridIndex = new LocationIndexTree(gh.getGraphHopperStorage().getBaseGraph(), new MMapDirectory(gh.getGraphHopperLocation() + "grid_loc2idIndex").create()).
	                setMinResolutionInMeter(500).prepareIndex();
		}
		
		int gridSizeMeters = 500;
//		int[] gridValues = new int[gridSizeMeters*gridSizeMeters];
		double cx = loc.x;
		double cy = loc.y;
		double gridSizeY = Math.toDegrees(gridSizeMeters / 6378100.0);
		double gridSizeX = gridSizeY / Math.cos(Math.toRadians(cx));
		double halfN = gridSizeMeters / 2.0;
		List<Coordinate> gridLocations = new ArrayList<>(gridSizeMeters*gridSizeMeters);
		
		for (int xi = 0; xi < gridSizeMeters; xi++) {
			double dx = (-halfN + xi)*gridSizeX;

			for (int yi = 0; yi< gridSizeMeters; yi++) {
				double dy = (-halfN + yi)*gridSizeX;

//				int p = xi + yi*gridSizeMeters;

//				QueryResult res = gridIndex.findClosest(cy + dy, cx + dx, EdgeFilter.ALL_EDGES);
//				if (res.isValid())
//					gridValues[p] = res.getClosestNode();
//				else
//					gridValues[p] = -1;

				gridLocations.add(new Coordinate(cx + dx, cy + dy));
			}
		}


		MatrixRequest mtxReq = new MatrixRequest();
		mtxReq.setMetrics(MatrixMetricsType.DISTANCE);
		mtxReq.setFlexibleMode(true);
		mtxReq.setSources(new Coordinate[] { parameters.getLocation() });
        Coordinate[] destinations = new Coordinate[gridLocations.size()];
        gridLocations.toArray(destinations);
		mtxReq.setDestinations(destinations);

		MatrixAlgorithm alg = MatrixAlgorithmFactory.createAlgorithm(mtxReq, gh);

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

		if (LOGGER.isDebugEnabled() && sw != null) {
			sw.stop();
			LOGGER.debug("Find edges: " + sw.getSeconds());
		}


		return isochroneMap;
	}
}
