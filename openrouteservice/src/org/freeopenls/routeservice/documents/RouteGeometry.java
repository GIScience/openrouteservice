
package org.freeopenls.routeservice.documents;

import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;
import org.freeopenls.constants.RouteService;
import org.freeopenls.error.ServiceError;
import org.freeopenls.routeservice.routing.RoutePlan;
import org.freeopenls.routeservice.routing.RouteResult;
import org.freeopenls.tools.FormatUtility;
import org.freeopenls.tools.CoordTransform;

import com.graphhopper.GHResponse;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.LineStringType;
import net.opengis.xls.DetermineRouteRequestType;
import net.opengis.xls.RouteGeometryRequestType;
import net.opengis.xls.RouteGeometryType;


/**
 * <p><b>Title: RouteGeometry</b></p>
 * <p><b>Description:</b> Class for RouteGeometry - Defines the geometry of the route.<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-07-26
 */
public class RouteGeometry {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger.getLogger(RouteGeometry.class.getName());
	/** RSConfigurator */
//	private RSConfigurator m_RSConfig;
	private StringBuffer buffer;

	/**
	 * Constructor
	 *
	 */
	public RouteGeometry(){
//    	m_RSConfig = RSConfigurator.getInstance();
		buffer = new StringBuffer();
	}

    /**
     * Method that create and returns the requested RouteGeometryResponse
     * 
     * @param drrType
     * @param routePlan
     * @param routeResult
     * @return RouteGeometryType
     * @throws ServiceError
     * @throws Exception
     */	
	public RouteGeometryType createRouteGeometry(DetermineRouteRequestType drrType, RoutePlan routePlan, RouteResult routeResult, RouteGeometryType routegeomType)throws ServiceError, Exception{

		//Set variables for RouteGeometryRequest
		RouteGeometryRequestType routegeomreqType = null; 	// Optional
		EnvelopeType boundingbox = null; 					// Optional		
//		BigInteger bigiScale = new BigInteger("1");			// Optional		Default=1
		Boolean boolProvideStartingPortion = false; 		// Optional		Default=false
		BigInteger bigiMaxPoints = new BigInteger("100"); 	// Optional		Default=100
		String sSRSBoundingBox = RouteService.GRAPH_SRS;				//Default
		//TODO
		double dFlatness = 0.0001;		//Default Flatness for Generalize
		double dFlatnessBoost = 0.001;	//Default FlatnessBoost for Generalize
		
		//Get RouteGeoemtryRequest
		routegeomreqType = drrType.getRouteGeometryRequest();
//		if (routegeomreqType.isSetScale()){
//			bigiScale = routegeomreqType.getScale();
//			if(bigiScale.intValue() > 500 ){
//				dFlatness = bigiScale.doubleValue() / m_RSConfig.getRouteGeomDivScaleValue();
//				dFlatnessBoost  = dFlatness;
//			}
//		}
		if (routegeomreqType.isSetProvideStartingPortion())
			boolProvideStartingPortion = routegeomreqType.getProvideStartingPortion();
		if (routegeomreqType.isSetMaxPoints())
			bigiMaxPoints = routegeomreqType.getMaxPoints();

		
		//Rectangular area of route for which the geometry is requested.
		if (routegeomreqType.isSetBoundingBox()) {
			throw new Exception("method is not implemented");
			/*
			// *** RouteGeometryResponse ***
			boundingbox = routegeomreqType.getBoundingBox();
			DirectPositionType[] dpTypeBoundingBox = boundingbox.getPosArray();
			
			if(boundingbox.isSetSrsName())
				sSRSBoundingBox = boundingbox.getSrsName();

			//Create BBox for Intersection/Overlay
			Coordinate c[] = new Coordinate[5];
			c[0] = Pos.getCoord(dpTypeBoundingBox[0].getStringValue());	//LowerLeftCorner
			c[2] = Pos.getCoord(dpTypeBoundingBox[1].getStringValue());	//UpperRightCorner
			c[1] = new Coordinate();		//LowerRightCorner
			c[3] = new Coordinate();		//UpperLeftCorner
			Coordinate cTMP = new Coordinate();		//TMP
			
			if(!sSRSBoundingBox.equals(RouteService.GRAPH_SRS)){
				c[0] = CoordTransform.transformGetCoord(sSRSBoundingBox, RouteService.GRAPH_SRS, c[0]);
				c[2] = CoordTransform.transformGetCoord(sSRSBoundingBox, RouteService.GRAPH_SRS, c[2]);
			}

			//Sets Upper/Lower corner BBox
			if(c[0].y>c[2].y){
				cTMP.y = c[0].y; c[0].y = c[2].y; c[2].y = cTMP.y;
			}
			if(c[0].x>c[2].x){
				cTMP.x = c[0].x; c[0].x = c[2].x; c[2].x = cTMP.x;
			}
			c[1].x = c[2].x; c[1].y = c[0].y; c[3].x = c[0].x; c[3].y = c[2].y; c[4]=c[0];
			
			//Create Polygon from BBox for Overlay/Intersection
			GeometryFactory gf = new GeometryFactory();
			LinearRing lr = gf.createLinearRing(c);
			Polygon p = new Polygon(lr, null, gf);

			//Create FeatureCollection with Polygon
			FeatureSchema featureS = new FeatureSchema();
			featureS.addAttribute("Geometry", AttributeType.GEOMETRY);
			FeatureCollection featureD = new FeatureDataset(featureS);
			Feature feat = new BasicFeature(featureS);
			feat.setAttribute("Geometry", p);
			featureD.add(feat);
			
			//Overlay/Intersection FeatureCollection with Graph
			Overlay doOverlay = new Overlay(routeResult.getFeatCollRoute(), featureD);
			FeatureCollection featcollOut = doOverlay.getResultFeatColl();
			
			//*boolProvideStartingPortion*
			//If true, return the geometry of the starting portion 
			//of the route contained within the specified bounding area, up to the specified 
			//maximum number of points.  
			//If false, return the geometry of the complete route contained within the specified 
			//area, reducing the accuracy of the geometry as necessary to not exceed the 
			//specified maximum number of points.
			if(boolProvideStartingPortion){
				if(routePlan.getSourceWayPoint().getCoordinate().x > c[0].x 
						&& routePlan.getSourceWayPoint().getCoordinate().y > c[0].y 
						&& routePlan.getSourceWayPoint().getCoordinate().x < c[2].x 
						&& routePlan.getSourceWayPoint().getCoordinate().y < c[2].y){
					LineStringType lsType = routegeomType.addNewLineString();
					addLineStringType(RouteService.GRAPH_SRS, routeResult.getResponseSRS(), featcollOut, bigiMaxPoints.intValue(), lsType, routePlan.getElevationInformation());
					//routegeomType.setLineString(addLineStringType(RouteService.GRAPH_SRS, routeResult.getResponseSRS(), featcollOut, bigiMaxPoints.intValue()));
				}	
			}else{
				if (featcollOut.size() > 0){
					// Generalize
					if(bigiMaxPoints.intValue() < routeResult.getTotalNumberOfPointsOfRoute() && bigiMaxPoints.intValue() > (featcollOut.size() * 2))
						featcollOut = Generalizer.generalizeFeatureCollection(featcollOut, "EdgeID", dFlatness, bigiMaxPoints.intValue(), dFlatnessBoost);
					else if(bigiMaxPoints.intValue() < routeResult.getTotalNumberOfPointsOfRoute())
						featcollOut = Generalizer.generalizeFeatureCollection(featcollOut, dFlatness, bigiMaxPoints.intValue(), dFlatnessBoost);
					
					// *** RouteGeometryResponse ***
					LineStringType lsType = routegeomType.addNewLineString();
					addLineStringType(RouteService.GRAPH_SRS, routeResult.getResponseSRS(), featcollOut, lsType, routePlan.getElevationInformation());
					//routegeomType.setLineString(addLineStringType(RouteService.GRAPH_SRS, routeResult.getResponseSRS(), featcollOut));
				}
			}
			*/
		//If not specified, defaults to full route.
		}else{		
			//Generalize
			/* Runge
			 * 
			 * if (routegeomreqType.isSetMaxPoints()){
				if(bigiMaxPoints.intValue() < routeResult.getTotalNumberOfPointsOfRoute() && bigiMaxPoints.intValue() > (routeResult.getFeatCollRoute().size() * 2))
					routeResult.setFeatCollRoute(Generalizer.generalizeFeatureCollection(routeResult.getFeatCollRoute(), "EdgeID", dFlatness, bigiMaxPoints.intValue(), dFlatnessBoost));
				else if(bigiMaxPoints.intValue() < routeResult.getTotalNumberOfPointsOfRoute())
					routeResult.setFeatCollRoute(Generalizer.generalizeFeatureCollection(routeResult.getFeatCollRoute(), dFlatness, bigiMaxPoints.intValue(), dFlatnessBoost));
			}
			*/
			LineStringType lsType = routegeomType.addNewLineString();
			// *** RouteGeometryResponse ***
			addLineStringType(RouteService.GRAPH_SRS, routeResult.getResponseSRS(), routeResult.getRouteSegments(), lsType, routePlan.getElevationInformation());
			//routegeomType.setLineString(addLineStringType(RouteService.GRAPH_SRS, routeResult.getResponseSRS(), routeResult.getFeatCollRoute()));
		}
		
		mLogger.debug("RouteGeometry finish!");
		
		return routegeomType;
	}

	/**
	 * Method that add geometry of the route to a LineString-Element	 
	 * 
	 * @param sGraphSRS
	 * @param sResponseSRS
	 * @param featcoll FeatureCollection with linestrings to add
	 * @return LineStringtype RouteGeometry (LineString)
	 * @throws ServiceError
	 */
	private LineStringType addLineStringType(String sGraphSRS, String sResponseSRS, List<GHResponse> routeSegments, LineStringType lsType, boolean withElevation)throws ServiceError{
		lsType.setSrsName(sResponseSRS);

		if (routeSegments.size() > 0)
		{
			GHResponse firstSegment = routeSegments.get(0);
			DirectPositionType directFirst = lsType.addNewPos();
			double lon = firstSegment.getPoints().getLongitude(0);
			double lat = firstSegment.getPoints().getLatitude(0);
			double z = Double.NaN;
			if (withElevation)
				z = firstSegment.getPoints().getElevation(0);
			if(!sGraphSRS.equals(sResponseSRS)){
				Coordinate cTMP = CoordTransform.transformGetCoord(sGraphSRS, sResponseSRS, new Coordinate(lon,  lat, z));
				directFirst.setStringValue(FormatUtility.formatCoordinate(cTMP, true, buffer));
			}else
				directFirst.setStringValue(FormatUtility.formatCoordinate(lon, lat, z, withElevation, buffer));

			for (int i = 0; i < routeSegments.size(); i++) {
				GHResponse seg = routeSegments.get(i);
				PointList points = seg.getPoints();

				for (int j = 1; j < points.size(); j++) {
					DirectPositionType direct = lsType.addNewPos();
					if(!sGraphSRS.equals(sResponseSRS)){
						Coordinate cTMP = CoordTransform.transformGetCoord(sGraphSRS, sResponseSRS, new Coordinate(points.getLongitude(j), points.getLatitude(j), points.is3D() ? points.getElevation(j): Double.NaN));
						direct.setStringValue(FormatUtility.formatCoordinate(cTMP, withElevation, buffer));
					}else
						direct.setStringValue(FormatUtility.formatCoordinate(points.getLongitude(j), points.getLatitude(j), points.is3D() ? points.getElevation(j): Double.NaN, withElevation, buffer));
				}
			}
		}

		return lsType;
	}

	/**
	 * Method that add geometry of the starting portion of the route contained<br>
	 * within the specified bounding area, up to the specified maximum number of points to a LineString-Element.
	 * 
	 * @param sGraphSRS
	 * @param sResponseSRS
	 * @param featcoll FeatureCollection with linestrings to add
	 * @param iNumberOfPoints maximum number of points
	 * @return LineStringtype RouteGeometry (LineString)
	 * @throws ServiceError
	 */
	private LineStringType addLineStringType(String sGraphSRS, String sResponseSRS, FeatureCollection featcoll, int iNumberOfPoints, LineStringType lsType, boolean withElevation)throws ServiceError{
		int iNumberOfPointsTMP = 0;
		int iCount = 0;
		boolean boolFinish = false;
		///LineStringType lsType = LineStringType.Factory.newInstance();
		lsType.setSrsName(sResponseSRS);
		List l = featcoll.getFeatures();

		Feature firstfeat = (Feature) l.get(0);
		LineString firstlineTMP = (LineString) firstfeat.getGeometry();
		Coordinate cFirst[] = firstlineTMP.getCoordinates();
		DirectPositionType directFirst = lsType.addNewPos();
		if(!sGraphSRS.equals(sResponseSRS))
			cFirst[0] = CoordTransform.transformGetCoord(sGraphSRS, sResponseSRS, cFirst[0]);
		
		directFirst.setStringValue(FormatUtility.formatCoordinate(cFirst[0], withElevation, buffer));
				
		for (int i = 0 ; i < l.size() ; i++) {
			Feature feat2 = (Feature) l.get(i);
			LineString lineTMP = (LineString) feat2.getGeometry();
			Coordinate c[] = lineTMP.getCoordinates();
	
			iNumberOfPointsTMP = iNumberOfPointsTMP + c.length;
			
			if(iNumberOfPointsTMP > iNumberOfPoints){
				iCount = c.length - (iNumberOfPointsTMP - iNumberOfPoints) + i;
				boolFinish = true;
			}else
				iCount = c.length;
				
			for (int j = 1; j < iCount; j++) {
				DirectPositionType direct = lsType.addNewPos();
				if(!sGraphSRS.equals(sResponseSRS))
					c[j] = CoordTransform.transformGetCoord(sGraphSRS, sResponseSRS, c[j]);
				direct.setStringValue(FormatUtility.formatCoordinate(c[j], withElevation));
			}
				
			if(boolFinish)
				break;
		}

		return lsType;
	}
}