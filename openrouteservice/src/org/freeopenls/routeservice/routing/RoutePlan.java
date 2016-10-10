/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

package org.freeopenls.routeservice.routing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.freeopenls.location.WayPoint;
import org.freeopenls.routeservice.graphhopper.extensions.HeavyVehicleAttributes;
import org.freeopenls.routeservice.routing.RoutePreferenceType;

import com.vividsolutions.jts.geom.Polygon;

/**
 * <p>
 * <b>Title: RoutePlan</b>
 * </p>
 * <p>
 * <b>Description:</b> Class for plan a route.
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
public class RoutePlan {

	/** Calendar Start/EndDateTime */
	private Calendar mCalendarDateTime = null;
	private String mExpectedDateTime = null;

	/** RoutePreference */
	private int mRoutePreference = RoutePreferenceType.CAR;
	private int mWeightingMethod = WeightingMethod.FASTEST;
	
	/** Source WayPoint */
	private WayPoint mSourceWayPoint = null;

	/** Source WayPoint */
	private ArrayList<WayPoint> mViaWayPoints = new ArrayList<WayPoint>();

	/** Destination WayPoint */
	private WayPoint mDestinationWayPoint = null;

	/** AvoidAreas and AvoidLines Geometry for display in RouteMap */
	private ArrayList<Polygon> mAvoidAreas = new ArrayList<Polygon>();

	private int mAvoidFeatureTypes = -1;
	private float[] mVehicleAttributes;
	private double[]  mWheelchairAttributes;
	private boolean mUseRealTimeTraffic = false;
	private int mVehicleType = HeavyVehicleAttributes.Goods;
	private List<String> mLoadCharacteristics = null;
	private double mMaxSpeed = -1;
	private boolean mSupportTurnRestrictions = false;
	private boolean mGetSurfaceInformation = false;
	private boolean mGetElevationInformation = false;
	private int mSteepnessDifficultyLevel = -1;
	private double mSteepnessMaxValue = -1;

	public RoutePlan() {
	}

	/**
	 * toString()
	 */
	public String toString() {
		return " Calendar: " + mCalendarDateTime + " ExpectedDateTime: " + mExpectedDateTime + "\n"
				+ " RoutePreference: " + mRoutePreference + " \n Source: " + mSourceWayPoint.toString() + " \n Via: "
				+ mViaWayPoints + " \n Destination : " + mDestinationWayPoint.toString() + " \n Avoid Polygons: "
				+ mAvoidAreas.size();
	}

	public int getVehicleType()
	{
		return mVehicleType;
	}
	
	public void setVehicleType(int value)
	{
		mVehicleType = value;
	}
	
	public float[] getVehicleAttributes() {
		return mVehicleAttributes;
	}

	public void setVehicleAttributes(float[] values) {
		mVehicleAttributes = values;
	}

	public boolean hasVehicleAttributes() {
		return mVehicleAttributes != null && mVehicleAttributes.length > 0;
	}
	
	public boolean hasLoadCharacteristic(String value)
	{
		if (value == null)
			return false;
		
		if (hasLoadCharacteristics())
		{
			for (int i = 0; i < mLoadCharacteristics.size(); i++)
			{
				if (value.equalsIgnoreCase(mLoadCharacteristics.get(i)))
					return true;
			}
		}
		
		return false;
	}

	public List<String> getLoadCharacteristics() {
		return mLoadCharacteristics;
	}

	public void setLoadCharacteristics(List<String> values) {
		mLoadCharacteristics = values;
	}
	
	public boolean hasLoadCharacteristics()
	{
	   return mLoadCharacteristics != null &&  mLoadCharacteristics.size() > 0;
	}

	public void setWheelchairAttributes(double[] values)
	{
		mWheelchairAttributes = values;
	}
	
	public double[] getWheelchairAttributes() {
		return mWheelchairAttributes;
	}
	
	public boolean hasWheelchairAttributes()
	{
		return mWheelchairAttributes != null && mWheelchairAttributes.length > 0;
	}
	
	public boolean getSurfaceInformation() {
		return mGetSurfaceInformation;
	}

	public void setSurfaceInformation(boolean value) {
		mGetSurfaceInformation = value;
	}
	
	public boolean getElevationInformation() {
		return mGetElevationInformation;
	}

	public void setElevationInformation(boolean value) {
		mGetElevationInformation = value;
	}
	
	public boolean getUseRealTimeTraffic() {
		return mUseRealTimeTraffic;
	}

	public void setUseRealTimeTraffic(boolean value) {
		mUseRealTimeTraffic = value;
	}
	
	public boolean getSupportTurnRestrictions() {
		return mSupportTurnRestrictions;
	}

	public void setSupportTurnRestrictions(boolean value) {
		mSupportTurnRestrictions = value;
	}

	/**
	 * @return the RoutePreference
	 */
	public int getRoutePreference() {
		return mRoutePreference;
	}

	/**
	 * @param fastest
	 *            the RoutePreference to set
	 */
	public void setRoutePreference(int pref) {
		mRoutePreference = pref;
	}

	public int getWeightingMethod()	{
		return mWeightingMethod;
	}
	
	public void setWeightingMethod(int weightingMethod)	{
		mWeightingMethod = weightingMethod;
	}
	
	public double getMaxSpeed()	{
		return mMaxSpeed;
	}
	
	public void setMaxSpeed(double speed)	{
		mMaxSpeed = speed;
	}
	
	/**
	 * @return the SourceWayPoint
	 */
	public WayPoint getSourceWayPoint() {
		return mSourceWayPoint;
	}

	/**
	 * @param sourceWayPoint
	 *            the SourceWayPoint to set
	 */
	public void setSourceWayPoint(WayPoint sourceWayPoint) {
		mSourceWayPoint = sourceWayPoint;
	}

	/**
	 * @return the DestinationWayPoint
	 */
	public WayPoint getDestinationWayPoint() {
		return mDestinationWayPoint;
	}

	/**
	 * @param destinationWayPoint
	 *            the DestinationWayPoint to set
	 */
	public void setDestinationWayPoint(WayPoint destinationWayPoint) {
		mDestinationWayPoint = destinationWayPoint;
	}

	/**
	 * @return the CalendarDateTime
	 */
	public Calendar getCalendarDateTime() {
		return mCalendarDateTime;
	}

	/**
	 * @param calendarDateTime
	 *            the CalendarDateTime to set
	 */
	public void setCalendarDateTime(Calendar calendarDateTime) {
		mCalendarDateTime = calendarDateTime;
	}

	/**
	 * @return the ExpectedDateTime
	 */
	public String getExpectedDateTime() {
		return mExpectedDateTime;
	}

	/**
	 * @param expectedDateTime
	 *            the ExpectedDateTime to set
	 */
	public void setExpectedDateTime(String expectedDateTime) {
		mExpectedDateTime = expectedDateTime;
	}

	public boolean hasAvoidAreas() {
		return mAvoidAreas == null || mAvoidAreas.size() > 0;
	}

	public boolean hasAvoidFeatures() {
		return mAvoidFeatureTypes > 0;
	}

	/**
	 * @return the AvoidAreas
	 */
	public ArrayList<Polygon> getAvoidAreas() {
		return mAvoidAreas;
	}

	/**
	 * @param avoidAreas
	 *            the AvoidAreas to set
	 */
	public void setAvoidAreas(ArrayList<Polygon> avoidAreas) {
		mAvoidAreas = avoidAreas;
	}

	public void setAvoidFeatureTypes(int value) {
		mAvoidFeatureTypes = value;
	}

	public int getAvoidFeatureTypes() {
		return mAvoidFeatureTypes;
	}

	/**
	 * @return the mViaWayPoints
	 */
	public ArrayList<WayPoint> getViaWayPoints() {
		return mViaWayPoints;
	}

	/**
	 * @param viaWayPoints
	 *            the mViaWayPoints to set
	 */
	public void addViaWayPoint(WayPoint viaWayPoint) {
		mViaWayPoints.add(viaWayPoint);
	}
	
	public int getSteepnessDifficultyLevel() {
	  return mSteepnessDifficultyLevel;	
	}
	
	public void setSteepnessDifficultyLevel(int value) {
		  mSteepnessDifficultyLevel =  value;	
		}
	
	public double getSteepnessMaxValue() {
		return mSteepnessMaxValue;	
	}

	public void setSteepnessMaxValue(double value) {
		mSteepnessMaxValue =  value;	
	}
}
