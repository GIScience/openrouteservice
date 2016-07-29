/****************************************************
 Copyright (C) 2007 by Pascal Neis

 Author: Pascal Neis

 Contact: Pascal Neis, Herm-Schuster-Str. 57,
 65510 Hünstetten, Germany, pascal.neis@gmail.com
*****************************************************/

package org.freeopenls.accessibilityanalyseservice.documents;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.freeopenls.accessibilityanalyseservice.AASConfigurator;
import org.freeopenls.routeservice.isochrones.Isochrone;
import org.freeopenls.routeservice.isochrones.IsochroneMap;
import org.freeopenls.tools.CoordTransform;
import org.freeopenls.tools.FormatUtility;

import net.opengis.gml.AbstractRingPropertyType;
import net.opengis.gml.AbstractRingType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.LineStringType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.PolygonType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

import de.fhMainz.geoinform.aas.AccessibilityGeometryRequestType;
import de.fhMainz.geoinform.aas.AccessibilityGeometryType;
import de.fhMainz.geoinform.aas.IsochroneGeometryType;
import de.fhMainz.geoinform.aas.IsochroneType;
import de.fhMainz.geoinform.aas.LineStringPreferenceType;
import de.fhMainz.geoinform.aas.PolygonPreferenceType;

/**
 * Class create Accessibility Geometry
 *  
 * @author Pascal Neis, pascal.neis@geoinform.fh-mainz.de
 * @version 1.0 2007-01-22
 */
public class AccessibilityGeometry {

	/**
	 * Method that creates for the response the AccessibilityGeometryType
	 * 
	 * @param analysegeomreqType
	 * @param arraylistPolygon
	 * @return AccessibilityGeometryType
	 * @throws ServiceError
	 * @throws Exception
	 */
	public static AccessibilityGeometryType getAnalyseGeometry(AccessibilityGeometryType analysegeomType, AASConfigurator AASconfig, AccessibilityGeometryRequestType analysegeomreqType, String sSRS, String sResponseSRS, IsochroneMap isochroneMap)throws ServiceError, Exception{
		
		if (isochroneMap == null || isochroneMap.isEmpty())
			return analysegeomType;
		
        ///////////////////////////////////////
		// Polygon Preference
		if(analysegeomreqType.isSetPolygonPreference()){
			PolygonPreferenceType.Enum polygonref = analysegeomreqType.getPolygonPreference();
			String sPref = polygonref.toString();
			
	        ///////////////////////////////////////
			//CONVEX Polygon
			if(sPref.equals(PolygonPreferenceType.CONVEX.toString())){
				Isochrone iso = isochroneMap.getIsochrone(0);
				Polygon isoPoly = (Polygon)iso.getGeometry();
				
				IsochroneType isochrone = analysegeomType.addNewIsochrone();
				isochrone.setTime(iso.getValue());
				IsochroneGeometryType isochroneGeom = isochrone.addNewIsochroneGeometry();
				isochroneGeom.setArea(iso.getArea(true));
				
				addPolygon(sSRS, sResponseSRS, (Polygon)isoPoly.convexHull(), isochroneGeom.addNewPolygon());
			}
	        ///////////////////////////////////////
			//DETAILED Polygon
			if(sPref.equals(PolygonPreferenceType.DETAILED.toString())){
				// *** RouteGeometryResponse ***
				for (Isochrone iso : isochroneMap.getIsochrones()) {
					Polygon isoPoly = (Polygon)iso.getGeometry();

					IsochroneType isochrone = analysegeomType.addNewIsochrone();
					isochrone.setTime(iso.getValue());
					IsochroneGeometryType isochroneGeom = isochrone.addNewIsochroneGeometry();
					isochroneGeom.setArea(iso.getArea(true));
					
					addPolygon(sSRS, sResponseSRS, isoPoly, isochroneGeom.addNewPolygon());
				}
			}
		}
        ///////////////////////////////////////
		// LineString Preference
		else if(analysegeomreqType.isSetLineStringPreference()){
			LineStringPreferenceType.Enum linestringpref = analysegeomreqType.getLineStringPreference();
			String sPref = linestringpref.toString();
			
	        ///////////////////////////////////////
			//STREETS_GEOM LineString
			if(sPref.equals(LineStringPreferenceType.STREETS_GEOM.toString())){
				
				//Create FeatureCollection Accessibility Polygons
				FeatureCollection featcoll;
				FeatureSchema featschGraph = new FeatureSchema();
				featschGraph.addAttribute("Geometry", AttributeType.GEOMETRY);
				featcoll = new FeatureDataset(featschGraph);
				
				for (Isochrone isochrone : isochroneMap.getIsochrones()) {
					Polygon poly = (Polygon)isochrone.getGeometry();
					Feature feat = new BasicFeature(featschGraph);
					feat.setAttribute("Geometry", poly);
					featcoll.add(feat);
				}

	
//		        ///////////////////////////////////////
//				//Overlay/Intersection Graph with Accessibility Polygons
//				Overlay doOverlay = new Overlay(GraphManager.getInstance().getIndexFeatCollGraph(), featcoll);
//				FeatureCollection featcollOut = doOverlay.getResultFeatColl();
//				List listFeature = featcollOut.getFeatures();
//				
//				ArrayList<LineString> arraylistLineString = new ArrayList<LineString>();
//				for(int i=0 ; i<listFeature.size() ; i++){
//					Feature feat = (Feature) listFeature.get(i);
//					if(feat.getGeometry() instanceof LineString)
//						arraylistLineString.add((LineString)feat.getGeometry());
//				}
//
//				// *** RouteGeometryResponse ***
//				for(int i=0 ; i<arraylistLineString.size() ; i++){
//					addLineString(sSRS, sResponseSRS, arraylistLineString.get(i), analysegeomType.addNewLineString());
//				}
				
				//TODO
				// not supported
			}
		}
		
		return analysegeomType;
	}

	/**
	 * Method that create and adds a polygon to the PolygonType for Output
	 * @param poly
	 * @param polyType
	 */
	private static void addPolygon(String sSRS, String sResponseSRS, Polygon poly, PolygonType polyType)throws ServiceError{
		//SET SRS
		polyType.setSrsName(sResponseSRS);
		
		boolean bSRSConvert = !sSRS.equals(sResponseSRS);
		
		Coordinate cTMP = new Coordinate();
		
		//Exterior Polygon
		AbstractRingPropertyType abringpropTypeEXTERIOR = polyType.addNewExterior();
		AbstractRingType abringTypeEXTERIOR = abringpropTypeEXTERIOR.addNewRing();
		LinearRingType linearring = (LinearRingType) abringTypeEXTERIOR.changeType(LinearRingType.type);
		LineString lsEXTERIOR = poly.getExteriorRing();
		Coordinate cEXTERIOR[] = lsEXTERIOR.getCoordinates();

		for(int i=0 ; i<cEXTERIOR.length ; i++){
			DirectPositionType direct = linearring.addNewPos();
			
			if(bSRSConvert){
				try{
					cTMP = CoordTransform.transformGetCoord(sSRS, sResponseSRS, cEXTERIOR[i]);
				}catch (org.freeopenls.error.ServiceError se) {
					// TODO: handle exception
				}
			}
			else
				cTMP = cEXTERIOR[i];
			
			direct.setStringValue(FormatUtility.formatCoordinate(cTMP));
		}
		
		//Interior Polygon(s)
		int iInteriorRings = poly.getNumInteriorRing();
		for(int i=0 ; i<iInteriorRings ; i++){
			AbstractRingPropertyType abringpropTypeINTERIOR = polyType.addNewInterior();
			AbstractRingType abringTypeINTERIOR = abringpropTypeINTERIOR.addNewRing();
			LinearRingType linearringINTERIOR = (LinearRingType) abringTypeINTERIOR.changeType(LinearRingType.type);
			LineString lsTMPINTERIOR = poly.getInteriorRingN(i);
			Coordinate cINTERIOR[] = lsTMPINTERIOR.getCoordinates();

			for(int j=0 ; j<cINTERIOR.length ; j++) {
				DirectPositionType direct = linearringINTERIOR.addNewPos();
				
				if(bSRSConvert){
					try{
						cTMP = CoordTransform.transformGetCoord(sSRS, sResponseSRS, cEXTERIOR[i]);
					}catch (org.freeopenls.error.ServiceError se) {
						// TODO: handle exception
					}
				}
				else
					cTMP = cINTERIOR[j];
				
				direct.setStringValue(FormatUtility.formatCoordinate(cTMP));
			}
			//------
				//For well-formed XML-Doc
				XmlCursor cursorINTERIOR = abringpropTypeINTERIOR.newCursor();
				if (cursorINTERIOR.toChild(new QName("http://www.opengis.net/gml", "_Ring"))) {
					cursorINTERIOR.setName(new QName("http://www.opengis.net/gml", "LinearRing"));
				}
				cursorINTERIOR.dispose();
			//------
		}
		
		//------
			//For well-formed XML-Doc
			XmlCursor cursorEXTERIOR = abringpropTypeEXTERIOR.newCursor();
			if (cursorEXTERIOR.toChild(new QName("http://www.opengis.net/gml", "_Ring"))) {
				cursorEXTERIOR.setName(new QName("http://www.opengis.net/gml", "LinearRing"));
			}
			cursorEXTERIOR.dispose();
		//-----
	}

	/**
	 * Method that create and adds a LineString to the LineStringType for Output
	 * @param poly
	 * @param polyType
	 */
	private static void addLineString(String sSRS, String sResponseSRS, LineString ls, LineStringType linestring)throws ServiceError{
		//SET SRS
		linestring.setSrsName("EPSG:"+sResponseSRS);
		
		Coordinate cTMP = new Coordinate();
		boolean bSRSConvert = !sSRS.equals(sResponseSRS);
		
		//LineString
		Coordinate c[] = ls.getCoordinates();
		for(int i=0 ; i<c.length ; i++){
			DirectPositionType directpos = linestring.addNewPos();
			
			if(bSRSConvert){
				try{
					cTMP = CoordTransform.transformGetCoord(sSRS, sResponseSRS, c[i]);
				}catch (org.freeopenls.error.ServiceError se) {
					// TODO: handle exception
				}
			}
			else
				cTMP = c[i];
			
			directpos.setStringValue(FormatUtility.formatCoordinate(cTMP));
		}

		//------
			//For well-formed XML-Doc
			XmlCursor cursor = linestring.newCursor();
			if (cursor.toChild(new QName("http://www.opengis.net/gml", "_Ring"))) {
				cursor.setName(new QName("http://www.opengis.net/gml", "LinearRing"));
			}
			cursor.dispose();
		//-----
	}
}
