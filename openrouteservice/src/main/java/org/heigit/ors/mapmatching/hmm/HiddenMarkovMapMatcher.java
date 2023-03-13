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
package org.heigit.ors.mapmatching.hmm;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import org.heigit.ors.mapmatching.AbstractMapMatcher;
import org.heigit.ors.mapmatching.RouteSegmentInfo;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/*
 * This class presents an implementation of a map matching algorithm based on a paper "Hidden Markov Map Matching Through Noise and Sparseness" written by Paul Newson and John Krumm
 *
 * http://research.microsoft.com/en-us/um/people/jckrumm/Publications%202009/map%20matching%20ACM%20GIS%20camera%20ready.pdf
 *
 * */
public class HiddenMarkovMapMatcher extends AbstractMapMatcher {

    private static final double SIGMA_Z = 4.07;// sigma_z(z, x); this value is taken from a paper by Newson and Krumm
    private static final double BETA =  0.00959442; // beta(z, x)
    private static final double DENOM = Math.sqrt(2 * Math.PI) * SIGMA_Z; // see Equation 1
    private DistanceCalc distCalcEarth = new DistanceCalcEarth(); // DistancePlaneProjection
    private LocationIndexTree locationIndex;
    private FlagEncoder encoder;
    private List<MatchPoint> matchPoints = new ArrayList<>(2);
    private List<Integer> roadSegments = new ArrayList<>();
    private double[] distances = new double[2];
    private double[] longitudes = new double[2];
    private double[] latitudes = new double[2];


	@SuppressWarnings("serial")
	private static class MatchPoint extends Coordinate {
		int segmentId;
		double distanceVal;
		int measuredPointIndex;

		MatchPoint(double lat, double lon) {
			super(lat, lon);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final MatchPoint other = (MatchPoint) obj;
			return toString().equals(other.toString());
		}

		@Override
		public int hashCode() {
			return ("MatchPoint" + this).hashCode();
		}
	}

	@Override
	public void setGraphHopper(GraphHopper gh) {
		graphHopper = gh;

		encoder = gh.getEncodingManager().fetchEdgeEncoders().get(0);
		locationIndex = (LocationIndexTree) gh.getLocationIndex();
	}

    @Override
    public RouteSegmentInfo[] match(Coordinate[] locations, boolean bothDirections) {
		EdgeFilter edgeFilter = this.edgeFilter == null ? AccessFilter.allEdges(encoder.getAccessEnc()) : this.edgeFilter;

        boolean bPreciseMode = false;
        int nPoints = locations.length;
        Coordinate[] z = locations;
        int nZ = z.length;
        int nR = 0;
        matchPoints.clear();
        roadSegments.clear();

        // Phase I: We are looking for the nearest road segments
        MatchPoint[][] x = new MatchPoint[nZ][];
        double searchRadius = this.searchRadius;

        for (int i = 0; i < nPoints; i++) {
            Coordinate zt = z[i];
            this.searchRadius = (bPreciseMode && i == 1) ? 50 : searchRadius;

            MatchPoint[] xi = findNearestPoints(zt.y, zt.x, i, edgeFilter, matchPoints, roadSegments);

            if (xi == null)
                return new RouteSegmentInfo[]{};

            x[i] = xi;
        }

        this.searchRadius = searchRadius;

        nR += roadSegments.size();

        if (nR == 0)
            return new RouteSegmentInfo[]{};

        double[][] transProbs = new double[nR][nR];
        double[][] emissionProbs = new double[nR][nZ];
        double[] startProbs = new double[nR];

        RouteSegmentInfo seg1 = findRouteSegments(z, x, nR, nZ, startProbs, emissionProbs, transProbs);
        RouteSegmentInfo seg2 = null;

        if (bothDirections) {
            startProbs = new double[nR];

            for (int i = 0; i < nZ / 2; i++) {
                Coordinate tempZ = z[i];
                z[i] = z[nZ - i - 1];
                z[nZ - i - 1] = tempZ;

                MatchPoint[] tempX = x[i];
                x[i] = x[nZ - i - 1];
                x[nZ - i - 1] = tempX;
            }

            for (int t = 0; t < nZ; t++) {
                int nRI = x[t].length;

                for (int i = 0; i < nRI; i++) {
                    x[t][i].measuredPointIndex = t;
                }
            }

            seg2 = findRouteSegments(z, x, nR, nZ, startProbs, emissionProbs, transProbs);
        }

        if (seg1 != null && seg2 != null) {
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

        RouteSegmentInfo[] result = new RouteSegmentInfo[2];
        if (seg1 != null) {
            result[0] = seg1;
            result[1] = seg2;
        } else {
            result[0] = seg2;
        }
        return result;
    }

    private RouteSegmentInfo findRouteSegments(Coordinate[] z, MatchPoint[][] x, int nR, int nZ, double[] startProbs, double[][] emissionProbs, double[][] transProbs) {
        // Phase II: Compute distances, probabilities, etc.

        double v;
        double dist;
        Coordinate z0 = z[0];

        double defaultProbability = 0.0;
        double distThreshold = 250;

        for (int t = 0; t < nZ; t++) {
            int nRI = x[t].length;

            for (int i = 0; i < nRI; i++) {
                MatchPoint xi = x[t][i];
                int ri = xi.segmentId;
                dist = xi.distanceVal;// distCalcEarth.calcDist(zt.lat, zt.lon, xi.lat, xi.lon)
                if (dist > distThreshold)
                    emissionProbs[ri][t] = defaultProbability;
                else {
                    v = dist / SIGMA_Z;
                    emissionProbs[ri][t] = Math.exp(-0.5 * v * v) / DENOM;
                }

				if (startProbs[ri] == 0.0) {
					dist = distCalcEarth.calcDist(z0.y, z0.x, xi.y, xi.x) / SIGMA_Z;
					if (dist > distThreshold || xi.measuredPointIndex != 0)
						startProbs[ri] = defaultProbability;
					else {
						v = dist / SIGMA_Z;
						startProbs[ri] = Math.exp(-0.5 * v * v) / DENOM;
					}
				}
			}
		}

		if (z.length > distances.length)
			distances = new double[z.length];

		for (int i = 0; i < z.length - 1 ; i++) {
			Coordinate zt = z[i];
			Coordinate zt1 = z[i+1];
			distances[i] = distCalcEarth.calcDist(zt.y, zt.x, zt1.y, zt1.x);
		}

		distances[z.length - 1] = distances[0];

		double perfTime = (distances[0]/encoder.getMaxSpeed())*3600;

		for (int i = 0; i < nR; i++) {
		    MatchPoint xi = matchPoints.get(i);

			for (int j = 0; j < nR; j++) {

				double value = defaultProbability;

				if (i != j) {
					MatchPoint xj = matchPoints.get(j);

					// check the order of points from 0 -> 1
					if (xi.measuredPointIndex < xj.measuredPointIndex) {
						//Point zt = z[xi.measuredPointIndex]
						//Point zt1 = z[xj.measuredPointIndex]
						double dz = distances[xi.measuredPointIndex]; // distCalcEarth.calcDist(zt.lat, zt.lon, zt1.lat, zt1.lon)

						GHRequest req = new GHRequest(xi.y, xi.x, xj.y, xj.x);
						req.getHints().putObject("ch.disable", true);
						req.getHints().putObject("lm.disable", true);
						req.setAlgorithm("dijkstrabi");

						try {
							GHResponse resp = graphHopper.route(req);

							if (!resp.hasErrors()) {
								ResponsePath path = resp.getBest();
								/*
								double dx = resp.getDistance()
								double dt = Math.abs(dz - dx)
                                								
								value = exponentialDistribution(100*beta, dt);  // Equation 2
								*/

                                double dx = path.getDistance();
                                double dt = Math.abs(dz - dx) / distances[0]; // normalize

								double time = path.getTime();
								//(distances[0]/1000/encoder.getMaxSpeed())*60*60*1000
                                double dt2 = Math.abs(time - perfTime)/perfTime;

								value = exponentialDistribution(BETA, 0.2*dt + 0.8*dt2);
							}
						} catch(Exception ex) {
							// do nothing
						}
					}
				}

				transProbs[i][j] = value;
			}
		}

        // Phase III: Apply Viterbi algorithm to find the path through the
        // lattice that maximizes the product of the measurement probabilities
        // and transition probabilities

        int[] bestPath = ViterbiSolver.findPath(startProbs, transProbs, emissionProbs, true);

        ORSGraphHopper gh = (ORSGraphHopper) graphHopper;

        RouteSegmentInfo res;

        if (nZ > latitudes.length) {
            latitudes = new double[nZ];
            longitudes = new double[nZ];
        }

        if (bestPath[0] != bestPath[1]) {
            for (int i = 0; i < nZ; i++) {
                MatchPoint mp = matchPoints.get(bestPath[i]);
                latitudes[i] = mp.y;
                longitudes[i] = mp.x;
            }
        } else {
            for (int i = 0; i < nZ; i++) {
                latitudes[i] = z[i].y;
                longitudes[i] = z[i].x;
            }
        }

		res = gh.getRouteSegment(latitudes, longitudes, encoder.toString());

		return res;
	}

	static double exponentialDistribution(double beta, double x) {
        return 1.0 / beta * Math.exp(-x / beta);
    }

	private MatchPoint[] findNearestPoints(double lat, double lon, int measuredPointIndex, EdgeFilter edgeFilter, List<MatchPoint> matchPoints,
			List<Integer> roadSegments) {
		// TODO Postponed: find out how to do this now: List<Snap> qResults = locationIndex.findNClosest(lat, lon, edgeFilter);
        // TODO: this is just a temporary work-around for the previous line
		List<Snap> qResults = List.of(locationIndex.findClosest(lat, lon, edgeFilter));
		if (qResults.isEmpty())
			return new MatchPoint[] {};

        int nMatchPoints = matchPoints.size();

		for (int matchIndex = 0; matchIndex < qResults.size(); matchIndex++) {
			Snap qr = qResults.get(matchIndex);

            double spLat = qr.getSnappedPoint().getLat();
            double spLon = qr.getSnappedPoint().getLon();
            double distance = distCalcEarth.calcDist(qr.getQueryPoint().getLat(), qr.getQueryPoint().getLon(), spLat,
                    spLon);

            // TODO Add start end end radius to search in rings.
            if (distance <= searchRadius) {

                int edgeId = EdgeIteratorStateHelper.getOriginalEdge(qr.getClosestEdge());

                if (!roadSegments.contains(edgeId))
                    roadSegments.add(edgeId);

                MatchPoint mp = new MatchPoint(spLat, spLon);
                mp.distanceVal = distance;
                mp.segmentId = roadSegments.indexOf(edgeId);
                mp.measuredPointIndex = measuredPointIndex;

                matchPoints.add(mp);
            }
        }

        int n = matchPoints.size() - nMatchPoints;

        if (n > 0) {
            MatchPoint[] res = new MatchPoint[n];
            for (int k = 0; k < n; k++)
                res[k] = matchPoints.get(nMatchPoints + k);
            return res;
        }
        return null;
    }

    public void clear() {
        this.distances = new double[2];
        this.longitudes = new double[2];
        this.latitudes = new double[2];
        this.matchPoints = new ArrayList<>(2);
        this.roadSegments = new ArrayList<>();
    }
}
