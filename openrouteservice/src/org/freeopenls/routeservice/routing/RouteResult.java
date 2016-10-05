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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.opengis.xls.DistanceUnitType;
import net.opengis.xls.WaySurfaceType;
import net.opengis.xls.WayTypeType;

import org.apache.xmlbeans.GDuration;
import org.freeopenls.constants.RouteService;
import org.freeopenls.routeservice.documents.Envelope;
import org.freeopenls.tools.CoordTools;
import org.freeopenls.tools.FormatUtility;

import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.WaySurfaceDescription;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionAnnotation;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
// import org.geotools.feature.FeatureCollection;

/**
 * <p>
 * <b>Title: RouteResult</b>
 * </p>
 * <p>
 * <b>Description:</b> Class for route result.
 * </p>
 * 
 * <p>
 * <b>Copyright:</b> Copyright (c) 2008
 * </p>
 * <p>
 * <b>Institution:</b> University of Bonn, Department of Geography
 * </p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-04-15
 */
public class RouteResult {
    private List<GHResponse> mRouteSegments;
	/** Response SRS */
	private String mResponseSRS = null;
	/** Response Language */
	private String mResponseLanguage = "en";
	/** Route Request ID */
	private String mRouteRequestID = null;
	/** DistanceUnit */
	private DistanceUnitType.Enum mDistanceUnit = DistanceUnitType.M; // Default
	private String mFeatCollSRS;
	/** TotalNumberOfPoints of the Route **/
	private int mTotalNumberOfPointsOfRoute = 1;
	/** Total Distance of the Route in Meters [m] **/
	private double mTotalDistance = 0;
	/** Actual Distance of the Route in Meters [m] **/
	private double mActualDistance = 0.0;
	/** Total Time of the Route in seconds [s] **/
	private double mTotalTime = 0;
	private double mActualTotalTime = 0;
	// Total ascent of the route
	private double mTotalAscent;
	// Total descent of the route
    private double mTotalDescent;
		
	
	/** Duration */
	private GDuration mDuration;
	/** Envelope of the route */
	private Envelope mEnvelopeRoute;

	/**
	 * Constructor
	 * 
	 * @param responseSRS
	 * @param responseLanguage
	 * @param routeRequestID
	 */
	public RouteResult(String responseSRS, String responseLanguage, String routeRequestID) {
		mResponseSRS = responseSRS;
		mResponseLanguage = responseLanguage;
		mRouteRequestID = routeRequestID;
		mRouteSegments = new ArrayList<>();
	}

	/**
	 * @return the FeatCollRoute
	 */
	public List<GHResponse> getRouteSegments() {
		return mRouteSegments;
	}
	
	public void addRouteSegment(GHResponse resp)
	{
		mRouteSegments.add(resp);
	}

	public com.vividsolutions.jts.geom.Envelope getRouteEnvelope() {
		BBox bbox = BBox.createInverse(true);
		
		Iterator i;
		com.vividsolutions.jts.geom.Envelope envelope = new com.vividsolutions.jts.geom.Envelope();
		for (i = mRouteSegments.iterator(); i.hasNext();) {
			GHResponse resp = (GHResponse) i.next();
			if (resp != null && resp.getDistance() > 0) // exclude stopovers
			{
				bbox = resp.calcRouteBBox(bbox);
			
				envelope.expandToInclude(new com.vividsolutions.jts.geom.Envelope(bbox.minLon, bbox.maxLon, bbox.minLat, bbox.maxLat));
			}
		}

		return envelope;
	}
	
	/**
	 * @return the TotalNumberOfPointsOfRoute
	 */
	public int getTotalNumberOfPointsOfRoute() {
		return mTotalNumberOfPointsOfRoute;
	}

	/**
	 * @param totalNumberOfPointsOfRoute
	 *            the TotalNumberOfPointsOfRoute to set
	 */
	public void setTotalNumberOfPointsOfRoute(int totalNumberOfPointsOfRoute) {
		mTotalNumberOfPointsOfRoute = totalNumberOfPointsOfRoute;
	}

	/**
	 * @return the TotalDistance
	 */
	public double getTotalDistance() {
		return mTotalDistance;
	}

	/**
	 * @param totalDistance
	 *            the TotalDistance to set
	 */
	public void setTotalDistance(double totalDistance) {
		mTotalDistance = totalDistance;
	}

	/**
	 * @return the ActualDistance
	 */
	public double getActualDistance() {
		return mActualDistance;
	}

	/**
	 * @param totalDistance
	 *            the ActualDistance to set
	 */
	public void setActualDistance(double actualDistance) {
		mActualDistance = actualDistance;
	}
	
	/**
	 * @return the TotalTime
	 */
	public double getTotalTime() {
		return mTotalTime;
	}

	/**
	 * @param totalTime
	 *            the TotalTime to set
	 */
	public void setTotalTime(double totalTime) {
		mTotalTime = totalTime;
	}
	
	
	/**
	 * @return the actual TotalTime
	 */
	public double getActualTotalTime() {
		return mActualTotalTime;
	}

	/**
	 * @param totalTime
	 *            the actual TotalTime to set
	 */
	public void setActualTotalTime(double actualTotalTime) {
		mActualTotalTime = actualTotalTime;
	}
	

	/**
	 * @return the ResponseSRS
	 */
	public String getResponseSRS() {
		return mResponseSRS;
	}

	/**
	 * @param responseSRS
	 *            the ResponseSRS to set
	 */
	public void setResponseSRS(String responseSRS) {
		mResponseSRS = responseSRS;
	}

	/**
	 * @return the DistanceUnit
	 */
	public DistanceUnitType.Enum getDistanceUnit() {
		return mDistanceUnit;
	}

	/**
	 * @param distanceUnit
	 *            the DistanceUnit to set
	 */
	public void setDistanceUnit(DistanceUnitType.Enum distanceUnit) {
		mDistanceUnit = distanceUnit;
	}

	/**
	 * @return the Duration
	 */
	public GDuration getDuration() {
		return mDuration;
	}

	/**
	 * @param duration
	 *            the Duration to set
	 */
	public void setDuration(GDuration duration) {
		mDuration = duration;
	}



	/**
	 * @return the EnvelopeRoute
	 */
	public Envelope getEnvelopeRoute() {
		return mEnvelopeRoute;
	}

	/**
	 * @param envelopeRoute
	 *            the EnvelopeRoute to set
	 */
	public void setEnvelopeRoute(Envelope envelopeRoute) {
		mEnvelopeRoute = envelopeRoute;
	}

	/**
	 * @return the RouteRequestID
	 */
	public String getRouteRequestID() {
		return mRouteRequestID;
	}

	/**
	 * @param routeRequestID
	 *            the RouteRequestID to set
	 */
	public void setRouteRequestID(String routeRequestID) {
		mRouteRequestID = routeRequestID;
	}

	/**
	 * @return the ResponseLanguage
	 */
	public String getResponseLanguage() {
		return mResponseLanguage;
	}

	/**
	 * @param responseLanguage
	 *            the ResponseLanguage to set
	 */
	public void setResponseLanguage(String responseLanguage) {
		mResponseLanguage = responseLanguage;
	}

	/**
	 * @return the mFeatCollSRS
	 */
	public String getFeatCollSRS() {
		return mFeatCollSRS;
	}

	/**
	 * @param featCollSRS
	 *            the mFeatCollSRS to set
	 */
	public void setFeatCollSRS(String featCollSRS) {
		mFeatCollSRS = featCollSRS;
	}
	
	public void setTotalAscent(double ascent)
	{
		mTotalAscent = ascent;
	}
	
	public double getTotalAscent()
	{
		return mTotalAscent;
	}
	
	public void addAscent(double ascent)
	{
		mTotalAscent += ascent;
	}
	
	public void setTotalDescent(double ascent)
	{
		mTotalDescent = ascent;
	}
	
	public double getTotalDescent()
	{
		return mTotalDescent;
	}
	
	public void addDescent(double ascent)
	{
		mTotalDescent += ascent;
	}
	
	public void computeSummary()
	{
		for (int i = 0; i < mRouteSegments.size(); i++) {
			GHResponse seg = mRouteSegments.get(i);
            
			setTotalTime(getTotalTime() + (int)(seg.getTime() / 1000));
			setTotalDistance(getTotalDistance() + seg.getDistance());
				
			setTotalAscent(getTotalAscent() + seg.getAscent());
			setTotalDescent(getTotalDescent() + seg.getDescent());
			
		}
	}
}
