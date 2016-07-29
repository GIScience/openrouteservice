/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.routeservice.routing;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.freeopenls.constants.RouteService;
import org.freeopenls.location.WayPoint;

import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.WaySurfaceDescription;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

/**
 * <p>
 * <b>Title: Routing</b>
 * </p>
 * <p>
 * <b>Description:</b> Class for Routing.
 * </p>
 * 
 * <p>
 * <b>Copyright:</b> Copyright (c) 2008
 * </p>
 * <p>
 * <b>Institution:</b> University of Bonn, Department of Geography
 * </p>
 * 
 * TODO: Klasse muss noch komplett überarbeitet werden!!
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-05-01
 * @version 1.1 2008-04-22
 */
public class Routing {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger.getLogger(Routing.class.getName());

	public Routing() throws IOException {
	}

	public void addStopOver(RoutePlan routePlan, RouteResult routeResult) {
		GHResponse resp = new GHResponse();
		resp.setInstructions(new InstructionList(null));
		resp.getInstructions().add(new Instruction(100, "", null, null));
		routeResult.addRouteSegment(resp);
	}

	/**
	 * Routing - Calculate the Route with the given Parameters
	 * 
	 * @param routePlan
	 * @param routeResult
	 * @param startWayPoint
	 * @param endWayPoint
	 * @param hmEdgeIDtoStrTyp
	 * @param hmEdgeIDtoLength
	 * @throws Exception
	 */
	public void doRouting(RoutePlan routePlan, RouteResult routeResult, WayPoint wpStart, WayPoint wpEnd)
			throws Exception {
		Coordinate wps = wpStart.getCoordinate();
		Coordinate wpe = wpEnd.getCoordinate();

		GHResponse resp = RouteProfileManager.getInstance().getPath(routePlan, wps.y, wps.x, wpe.y, wpe.x, wpStart.getCode());

		if (resp.hasErrors())
			throw new Exception(resp.getErrors().get(0).getMessage());

		if (resp.getDistance() == 0.0) {
			DistanceCalc dc = new DistanceCalcEarth();
			if (dc.calcDist(wps.y, wps.x, wpe.y, wpe.x) > 20) {
				throw new Exception("Unable to find a route between two coordinates: " + wps.toString() + " - "
						+ wpe.toString() + ". One of the points might belong to a disconnected subgraph.");
			}
		}

		routeResult.addRouteSegment(resp);
	}
/*
	private Coordinate[] getCoordinates(Instruction instr, Instruction instrNext) {
		PointList points = instr.getPoints();
		int nSize = points.size();
		Coordinate[] array = null;
		if (instrNext == null)
			array = new Coordinate[nSize];
		else
			array = new Coordinate[nSize + 1];

		boolean is3D = instr.getPoints().is3D();
		for (int i = 0; i < nSize; i++) {
			array[i] = new Coordinate(points.getLongitude(i), points.getLatitude(i), is3D ? points.getEle(i): Double.NaN);
		}

		if (instrNext != null) {
			points = instrNext.getPoints();
			array[nSize] = new Coordinate(points.getLongitude(0), points.getLatitude(0), is3D ? points.getEle(0): Double.NaN);
		}

		return array;
	}

	private Boolean equalInstructions(Instruction instr, Instruction instrNext, Feature prevFeature)
	{
		if (!Helper.isEmpty(instr.getName()) && instr.getName().equals(prevFeature.getString("Name")))
		{
			WaySurfaceDescription waySurfaceDesc = instr.getWaySurfaceDescription();
			if (waySurfaceDesc != null)
			{
				byte prevWayType = (byte)prevFeature.getAttribute("WayType");
				byte prevSurfaceType = (byte)prevFeature.getAttribute("SurfaceType");
				if (prevWayType != waySurfaceDesc.WayType || prevSurfaceType != waySurfaceDesc.SurfaceType)
					return false;				
			}	
			else
				return true;
		}
		
		return false;
	}
	

	private void addFeature(Instruction instr, Instruction instrNext, int routePref) {

		Coordinate[] coords = getCoordinates(instr, instrNext);
		if (coords.length <= 1)
			return;

		LineString ls = mGeometryFactory.createLineString(coords);

		Boolean hasAnnotation = instr.getAnnotation() != null && !Helper.isEmpty(instr.getAnnotation().getMessage());

		if (!mFeatCollRoute.isEmpty()) {
			Feature featPrev = mFeatCollRoute.getFeature(mFeatCollRoute.size() - 1);

			//if (!Helper.isEmpty(instr.getName()) && instr.getName().equals(featPrev.getString("Name")))
			if (equalInstructions(instr, instrNext, featPrev))	{
				if (hasAnnotation == false
						|| (hasAnnotation && !instr.getAnnotation().getMessage()
								.equals(featPrev.getString("Description")))) {
					LineMerger merger = new LineMerger();
					merger.add(featPrev.getGeometry());
					merger.add(ls);

					LineString lsMerged = null;

					for (Object o : merger.getMergedLineStrings()) {
						lsMerged = (LineString) o;
						break;
					}

					if (lsMerged != null) {
						// Update feature info.
						int time = (int) featPrev.getAttribute("Time") + addTime(instr.getTime() / 1000);
						featPrev.setAttribute("Time", time);
						double distance = (double) featPrev.getAttribute("Distance") + addDistance(instr.getDistance());
						featPrev.setAttribute("Distance", distance);
						featPrev.setGeometry(lsMerged);
						if (hasAnnotation)
							featPrev.setAttribute("Description", instr.getAnnotation().getMessage());

						return;
					}
				}
			}
		}

		// Create & Add Feature to the FeatColl
		Feature feat = new BasicFeature(mFeatureSchema);
		// feat.setAttribute("EdgeID", edge.getID());
		// Set Time of the Route
		feat.setAttribute("Name", instr.getName());
		if (hasAnnotation)
			feat.setAttribute("Description", instr.getAnnotation().getMessage());
		else
			feat.setAttribute("Description", "");

		feat.setAttribute("Sign", instr.getSign());
		feat.setAttribute("Time", addTime(instr.getTime() / 1000));
		
		if (instr.getAnnotation().getWayType() == 1) // Ferry, Steps as pushing sections
		{
			feat.setAttribute("ActualDistance", 0.0);
		}
		else
		{
			feat.setAttribute("ActualDistance", addActualDistance(instr.getDistance()));
		}

		WaySurfaceDescription desc = instr.getWaySurfaceDescription();
		if (desc != null)
		{
			feat.setAttribute("WayType", desc.WayType);
			feat.setAttribute("SurfaceType", desc.SurfaceType);
		}
		
		// Set Distance of the Route
		feat.setAttribute("Distance", addDistance(instr.getDistance()));
		feat.setAttribute("Geometry", ls);
		mFeatCollRoute.add(feat);
	}


	private double addDistance(double length) {
		mRouteResult.setTotalDistance(mRouteResult.getTotalDistance() + length);
		return length;
	}

	private double addActualDistance(double length) {
		mRouteResult.setActualDistance(mRouteResult.getActualDistance() + length);
		return length;
	}
	
	private int addTime(double time) {
		mRouteResult.setTotalTime(mRouteResult.getTotalTime() + time);
		return (int) time;
	}*/
}
