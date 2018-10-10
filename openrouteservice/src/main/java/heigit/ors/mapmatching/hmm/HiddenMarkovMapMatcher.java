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
package heigit.ors.mapmatching.hmm;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.mapmatching.AbstractMapMatcher;
import heigit.ors.mapmatching.LocationIndexMatch;
import heigit.ors.mapmatching.RouteSegmentInfo;
import heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;

import java.util.ArrayList;
import java.util.List;

/*
 * This class presents an implementation of a map matching algorithm based on a paper "Hidden Markov Map Matching Through Noise and Sparseness" written by Paul Newson and John Krumm  
 * 
 * http://research.microsoft.com/en-us/um/people/jckrumm/Publications%202009/map%20matching%20ACM%20GIS%20camera%20ready.pdf
 * 
 * */
public class HiddenMarkovMapMatcher extends AbstractMapMatcher {

	private DistanceCalc distCalcEarth = new DistanceCalcEarth(); // DistancePlaneProjection
	private LocationIndexMatch locationIndex;
	private FlagEncoder encoder;
	private List<MatchPoint> matchPoints = new ArrayList<>(2);
	private List<Integer> roadSegments = new ArrayList<Integer>();
	
	private static double sigma_z = 4.07;// sigma_z(z, x); this value is taken from a paper by Newson and Krumm
	private static double beta =  0.00959442; // beta(z, x); 
	private static double denom = Math.sqrt(2 * Math.PI) * sigma_z; // see Equation 1
	
	private double[] distances = new double[2];
	private double[] longitudes = new double[2];
	private double[] latitudes = new double[2];

	
	@SuppressWarnings("serial")
	private class MatchPoint extends Coordinate {
		public int segmentId;
		public double distance;
		public int measuredPointIndex;

		public MatchPoint(double lat, double lon) {
			super(lat, lon);
		}
	}

	public void setSearchRadius(double radius)
	{
		_searchRadius = radius;
		if (locationIndex != null)
			locationIndex.setGpxAccuracy(radius);
	}

	public void setGraphHopper(GraphHopper gh) {
		_graphHopper = gh;

		encoder = gh.getEncodingManager().fetchEdgeEncoders().get(0);
		GraphHopperStorage graph = gh.getGraphHopperStorage();
		locationIndex = new LocationIndexMatch(graph,
				(com.graphhopper.storage.index.LocationIndexTree) gh.getLocationIndex(), (int)_searchRadius);
	}

	@Override
	public RouteSegmentInfo[] match(Coordinate[] locations, boolean bothDirections) {
		EdgeFilter edgeFilter = _edgeFilter == null ? new DefaultEdgeFilter(encoder) : _edgeFilter;

		boolean bPreciseMode = false;
		int nPoints = locations.length;
		//Point[] inputPoints = new Point[nPoints];
		//inputPoints[0] = new Point(lat0, lon0);
		//inputPoints[1] = new Point((lat0 + lat1)/2.0, (lon0+lon1)/2.0); // extension
		//inputPoints[2] = new Point(lat1, lon1);
		//inputPoints[1] = new Point(lat1, lon1);
		Coordinate[] z = locations;
		int Nz = z.length;
		int Nr = 0;
		matchPoints.clear();
		roadSegments.clear();
		
		// Phase I: We are looking for the nearest road segments
		MatchPoint[][] x = new MatchPoint[Nz][];
		double searchRadius = _searchRadius;
		
		for (int i = 0; i < nPoints; i++) {
			Coordinate zt = z[i];
			_searchRadius = (bPreciseMode && i == 1) ? 50 : searchRadius;
				
			MatchPoint[] xi = findNearestPoints(zt.y, zt.x, i, edgeFilter, matchPoints, roadSegments);

			if (xi == null)
				return null;

			x[i] = xi;
		}
		
		_searchRadius = searchRadius;

		Nr += roadSegments.size();

		if (Nr == 0)
			return null;
		
		double[][] transProbs = new double[Nr][Nr];
		double[][] emissionProbs = new double[Nr][Nz];
		double[] startProbs = new double[Nr];

		RouteSegmentInfo seg1 = findRouteSegments(z, x, Nr, Nz, startProbs, emissionProbs, transProbs, edgeFilter);
		RouteSegmentInfo seg2 = null;
		
		if (bothDirections)
		{
			startProbs = new double[Nr];

			for (int i = 0; i < Nz / 2; i++) {
				Coordinate tempZ = z[i];
				z[i] = z[Nz - i - 1];
				z[Nz - i - 1] = tempZ;

				MatchPoint[] tempX = x[i];
				x[i] = x[Nz - i - 1];
				x[Nz - i - 1] = tempX;
			}
			
			for (int t = 0; t < Nz; t++)
			{
				int Nri = x[t].length;
				
				for (int i = 0; i < Nri; i++) {
					x[t][i].measuredPointIndex = t;
				}
			}

			seg2 = findRouteSegments(z, x, Nr, Nz, startProbs, emissionProbs, transProbs, edgeFilter);
		}
		
		if (seg1 !=  null && seg2 != null)
		{
			double koef = 1.1;
			if (seg1.getDistance() < 100)
				koef = 1.2;
			else if (seg1.getDistance() > 1000)
				koef = 1.25;
			// Remove unneeded loops. see example
			// 53856-53857
			
			if (seg1.getDistance() > seg2.getDistance()) {
				if (seg1.getDistance() > koef * seg2.getDistance())
					seg1 = null;
			} else {
				if (seg2.getDistance() > koef * seg1.getDistance())
					seg2 = null;
			}
		}
		
		RouteSegmentInfo result[] = new RouteSegmentInfo[2];
		if (seg1 != null) {
			result[0] = seg1;
			result[1] = seg2;
		} else
			result[0] = seg2;
		
		return result;
	}

	private RouteSegmentInfo findRouteSegments(Coordinate[] z, MatchPoint[][] x, int Nr, int Nz, double[] startProbs, double[][] emissionProbs, double[][] transProbs, EdgeFilter edgeFilter)
	{
		// Phase II: Compute distances, probabilities, etc.

		double v, dist;
		Coordinate z0 = z[0];
		
		double defaultProbability = 0.0;
		double distThreshold = 250;
        
		for (int t = 0; t < Nz; t++) {
			int Nri = x[t].length;
			
			for (int i = 0; i < Nri; i++) {
				MatchPoint xi = x[t][i];
				int ri = xi.segmentId;
				dist = xi.distance;// distCalcEarth.calcDist(zt.lat, zt.lon, xi.lat, xi.lon);
				if (dist > distThreshold)
					emissionProbs[ri][t] = defaultProbability;
				else {
					v = dist / sigma_z;
					emissionProbs[ri][t] = Math.exp(-0.5 * v * v) / denom;
				}

				if (startProbs[ri] == 0.0)
				{
					dist = distCalcEarth.calcDist(z0.y, z0.x, xi.y, xi.x) / sigma_z;
					if (dist > distThreshold || xi.measuredPointIndex != 0)
						startProbs[ri] = defaultProbability;
					else {
						v = dist / sigma_z;
						startProbs[ri] = Math.exp(-0.5 * v * v) / denom;
					}
				}
			}
		}
		
		if (z.length > distances.length)
			distances = new double[z.length];
		
		for (int i = 0; i < z.length - 1 ; i++)
		{
			Coordinate zt = z[i];
			Coordinate zt1 = z[i+1];
			distances[i] = distCalcEarth.calcDist(zt.y, zt.x, zt1.y, zt1.x);
		}
		
		distances[z.length - 1] = distances[0];
		
		double perfTime = (distances[0]/encoder.getMaxSpeed())*3600;
		
		for (int i = 0; i < Nr; i++) {
		    MatchPoint xi = matchPoints.get(i);
			
			for (int j = 0; j < Nr; j++) {
				
				double value = defaultProbability;
				
				if (i != j)
				{
					MatchPoint xj = matchPoints.get(j);
				 
					// check the order of points from 0 -> 1
					if (xi.measuredPointIndex < xj.measuredPointIndex)
					{
						//Point zt = z[xi.measuredPointIndex ];
						//Point zt1 = z[xj.measuredPointIndex];
						double dz = distances[xi.measuredPointIndex]; // distCalcEarth.calcDist(zt.lat, zt.lon, zt1.lat, zt1.lon);
				
						GHRequest req = new GHRequest(xi.y, xi.x, xj.y, xj.x);
						req.getHints().put("ch.disable", true);
						req.getHints().put("lm.disable", true);
						req.setAlgorithm("dijkstrabi"); 
						
						try
						{
							GHResponse resp = _graphHopper.route(req);
						
							if (!resp.hasErrors())
							{
								PathWrapper path = resp.getBest();
								/*
								double dx = resp.getDistance();
								double dt = Math.abs(dz - dx);
                                								
								value = exponentialDistribution(100*beta, dt);  // Equation 2
								*/
								
								double dx = path.getDistance();
								double dt = Math.abs(dz - dx)/distances[0]; // normalize 

								double time = path.getTime();
								//(distances[0]/1000/encoder.getMaxSpeed())*60*60*1000
                                double dt2 = Math.abs(time - perfTime)/perfTime;
                                								
								value = exponentialDistribution(beta, 0.2*dt + 0.8*dt2); 
							}
						}
						catch(Exception ex)
						{}
					}
				}
				
				transProbs[i][j] = value;
			}
		}

		// Phase III: Apply Viterbi algorithm to find the path through the
		// lattice that maximizes the product of the measurement probabilities
		// and transition probabilities

		ViterbiSolver viterbiSolver = new ViterbiSolver();
		int[] bestPath = viterbiSolver.findPath(startProbs, transProbs, emissionProbs, true);

		ORSGraphHopper gh = (ORSGraphHopper)_graphHopper;
		
		RouteSegmentInfo res = null;

		if (Nz > latitudes.length)
		{
		   latitudes = new double[Nz];
		   longitudes = new double[Nz];
		}

		if (bestPath[0] != bestPath[1])
		{
			for (int i = 0; i < Nz; i++)
			{
				MatchPoint mp = matchPoints.get(bestPath[i]);
				latitudes[i] = mp.y;
				longitudes[i] = mp.x;
			}
		}
		else
		{
			for (int i = 0; i < Nz; i++)
			{
				latitudes[i] = z[i].y;
				longitudes[i] = z[i].x;
			}
		}

		res = gh.getRouteSegment(latitudes, longitudes, encoder.toString(), edgeFilter);
		
		return res;
	}
	
	static double exponentialDistribution(double beta, double x) {
        return 1.0 / beta * Math.exp(-x / beta); 
    }
	
	private MatchPoint[] findNearestPoints(double lat, double lon, int measuredPointIndex, EdgeFilter edgeFilter, List<MatchPoint> matchPoints,
			List<Integer> roadSegments) {
		List<QueryResult> qResults = locationIndex.findNClosest(lat, lon, edgeFilter);
		if (qResults.isEmpty())
			return null;

		int nMatchPoints = matchPoints.size();

		for (int matchIndex = 0; matchIndex < qResults.size(); matchIndex++) {
			QueryResult qr = qResults.get(matchIndex);

			double spLat = qr.getSnappedPoint().getLat();
			double spLon = qr.getSnappedPoint().getLon();
			double distance = distCalcEarth.calcDist(qr.getQueryPoint().getLat(), qr.getQueryPoint().getLon(), spLat,
					spLon);

			if (distance <= _searchRadius) {
				
				int edgeId = EdgeIteratorStateHelper.getOriginalEdge(qr.getClosestEdge());

				if (!roadSegments.contains(edgeId))
					roadSegments.add(edgeId);

				MatchPoint mp = new MatchPoint(spLat, spLon);
				mp.distance = distance;
                mp.segmentId = roadSegments.indexOf(edgeId);
                mp.measuredPointIndex = measuredPointIndex;

				matchPoints.add(mp);
			}
		}
		
		int n = matchPoints.size() - nMatchPoints;
		
		if (n > 0)
		{
			MatchPoint[] res = new MatchPoint[n];
			
			for (int k = 0; k < n; k ++)
				res[k] = matchPoints.get(nMatchPoints + k);

			return res;
		}
		
		return null;
	}
}
