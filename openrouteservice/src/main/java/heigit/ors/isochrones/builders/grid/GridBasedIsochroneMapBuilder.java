package heigit.ors.isochrones.builders.grid;

import java.awt.geom.Point2D;
import java.util.List;

import org.apache.log4j.Logger;

import com.graphhopper.GraphHopper;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.StopWatch;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import heigit.ors.isochrones.GraphEdgeMapFinder;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.isochrones.IsochroneSearchParameters;
import heigit.ors.isochrones.builders.AbstractIsochroneMapBuilder;
import heigit.ors.routing.RouteSearchContext;
import heigit.ors.routing.graphhopper.extensions.AccessibilityMap;

public class GridBasedIsochroneMapBuilder extends AbstractIsochroneMapBuilder 
{
	private final Logger LOGGER = Logger.getLogger(GridBasedIsochroneMapBuilder.class.getName());

	private GeometryFactory _geomFactory;
	private RouteSearchContext _searchContext;
	
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
		StopWatch	swTotal = null;
		StopWatch sw = null;
		if (LOGGER.isDebugEnabled())
		{
			swTotal = new StopWatch();
			swTotal.start();
			sw = new StopWatch();
			sw.start();
		}

		// 1. Find all graph edges for a given cost.
		double maxSpeed = _searchContext.getEncoder().getMaxSpeed();

		Coordinate loc = parameters.getLocation();
		GraphHopper gh = _searchContext.getGraphHopper();
		LocationIndexTree index = (LocationIndexTree)gh.getLocationIndex();
     	//index.setMinResolutionInMeter(200);
		
		int gridSizeMeters = 400;
		int[] gridValues = new int[gridSizeMeters*gridSizeMeters];
		double cx = loc.x;
		double cy = loc.y;
		double gridSizeY = Math.toDegrees(gridSizeMeters / 6378100.0);
		double gridSizeX = gridSizeY / Math.cos(Math.toRadians(cx));
		double halfN = gridSizeMeters/2;
		
		for (int xi = 0; xi < gridSizeMeters; xi++)
		{
			double dx = (-halfN + xi)*gridSizeX;
			
			for (int yi = 0; yi< gridSizeMeters; yi++)
			{
				double dy = (-halfN + yi)*gridSizeX;
				
				int p = xi + yi*gridSizeMeters;

				QueryResult res = index.findClosest(cy + dy, cx + dx, _searchContext.getEdgeFilter());
				if (res.isValid())
					gridValues[p] = res.getClosestNode();
				else
					gridValues[p] = -1;
			}
		}
		
		
		
		IsochroneMap isochroneMap = new IsochroneMap(loc);

		//AccessibilityMap edgeMap = GraphEdgeMapFinder.findEdgeMap(_searchContext, parameters);

		if (LOGGER.isDebugEnabled())
		{
			sw.stop();

			LOGGER.debug("Find edges: " + sw.getSeconds());
		}
		

		return isochroneMap;
	}
}
