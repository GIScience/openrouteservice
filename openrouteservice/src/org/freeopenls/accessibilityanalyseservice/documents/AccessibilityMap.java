/****************************************************
 Copyright (C) 2006-2007 by Pascal Neis

 Author: Pascal Neis

 Contact: Pascal Neis, Herm-Schuster-Str. 57,
 65510 Hünstetten, Germany, pascal.neis@gmail.com
*****************************************************/

package org.freeopenls.accessibilityanalyseservice.documents;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;





import org.freeopenls.accessibilityanalyseservice.AASConfigurator;
import org.freeopenls.gml.Pos;
import org.freeopenls.overlay.Overlay;
import org.freeopenls.routeservice.isochrones.Isochrone;
import org.freeopenls.routeservice.isochrones.IsochroneMap;
import org.freeopenls.tools.CoordTransform;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

import de.fhMainz.geoinform.aas.AccessibilityMapOutputType;
import de.fhMainz.geoinform.aas.AccessibilityMapPreferenceType;
import de.fhMainz.geoinform.aas.AccessibilityMapRequestType;
import de.fhMainz.geoinform.aas.AccessibilityMapType;
import de.fhMainz.geoinform.aas.AccessibilityResponseType;
import de.fhMainz.geoinform.aas.ContentType;
import de.fhMainz.geoinform.aas.DetermineAccessibilityRequestType;
import de.fhMainz.geoinform.aas.ErrorCodeType;
import de.fhMainz.geoinform.aas.MapType;
import de.fhMainz.geoinform.aas.SeverityType;

/**
 * Class create AccessibilityMapType
 *  
 * @author Pascal Neis, pascal.neis@geoinform.fh-mainz.de
 * @version 1.0 2007-01-22
 */
public class AccessibilityMap {

	/**
	 * Method that creates for the response the AccessibilityMapType
	 * 
	 * @param darType
	 * @param bb
	 * @param sAnalyseRequestID
	 * @param cLocation
	 * @param arraylistPolygons
	 * @return AccessibilityMapType[]
	 * @throws ServiceError
	 * @throws Exception
	 */
	public static AccessibilityMapType[] getAnalyseMaps(AccessibilityResponseType arrespType,
			AASConfigurator AASconfig, DetermineAccessibilityRequestType darType, String sSRS, String sResponseSRS,
			EnvelopeType bb, String sAnalyseRequestID, Coordinate cLocation, IsochroneMap isochroneMap)throws ServiceError, Exception{
		
		// TODO Runge
		ArrayList<Polygon> arraylistPolygons = new ArrayList<Polygon>();
		if (isochroneMap != null && !isochroneMap.isEmpty())
		{
			for (Isochrone isochrone : isochroneMap.getIsochrones()) {
				arraylistPolygons.add((Polygon)isochrone.getGeometry());
				//isochrone.getValue();
			}
		}
		
		int iMapNumber = 1;
		
        ///////////////////////////////////////
		//Get AnalyseMapRequest
		AccessibilityMapRequestType analysemapreqType = darType.getAccessibilityMapRequest();
		AccessibilityMapOutputType analysemapOutTypeArray[] = analysemapreqType.getOutputArray(); //Mandatory
		//Create AnalyseMapType[] - AnalyseMapResponse
		AccessibilityMapType analysemapType[] = new AccessibilityMapType[analysemapOutTypeArray.length];

		for (int i = 0; i < analysemapOutTypeArray.length; i++) {
			///////////////////////////////////////
			//Set variables for AnalyseMapRequest
			EnvelopeType envelopeBBoxType = bb; 				//Optional		Default="FullPolygon"
			String sAnalyseMapWidth = "400"; 					//Optional
			String sAnalyseMapHeight = "400"; 					//Optional
			String sAnalyseMapFormat = "png"; 					//Optional
			String sAnalyseMapBGcolor = "#FFFFFF"; 				//Optional		Default=#FFFFFF -> White
			Boolean boolAnalyseMapTransparent = false; 			//Optional		Default=false
			
			//Width
			if (analysemapOutTypeArray[i].isSetWidth()){
				sAnalyseMapWidth = analysemapOutTypeArray[i].getWidth().toString();
				if(sAnalyseMapWidth.equals("0")){
					ServiceError se = new ServiceError(SeverityType.ERROR);
					se.addError(ErrorCodeType.OTHER_XML,
							"AnalyseMapRequestParameter - Width",
	                        "The required value of the mandatory parameter '"
	                        + "AnalyseMapRequestParameter - Width"
	                        + "' must be '>0'. Delivered value was: " + sAnalyseMapWidth);
					throw se;
				}
			}

			//Height
			if (analysemapOutTypeArray[i].isSetHeight()){
				sAnalyseMapHeight = analysemapOutTypeArray[i].getHeight().toString();
				if(sAnalyseMapHeight.equals("0")){
					ServiceError se = new ServiceError(SeverityType.ERROR);
					se.addError(ErrorCodeType.OTHER_XML,
							"AnalyseMapRequestParameter - Height",
							"The required value of the mandatory parameter '"
	                        + "AnalyseMapRequestParameter - Height" 
	                        + "' must be '>0'. Delivered value was: " + sAnalyseMapHeight);
					throw se;
				}
			}
				
			//Format
			if (analysemapOutTypeArray[i].isSetFormat()){
				sAnalyseMapFormat = analysemapOutTypeArray[i].getFormat();
				if(!sAnalyseMapFormat.equals("png")&&!sAnalyseMapFormat.equals("jpeg")&&!sAnalyseMapFormat.equals("gif")){
					ServiceError se = new ServiceError(SeverityType.ERROR);
					se.addError(ErrorCodeType.OTHER_XML,
							"AnalyseMapRequestParameter - Format",
							"The required value of the mandatory parameter '"
	                        + "AnalyseMapRequestParameter - Format" 
	                        + "' must be 'png, jpeg or gif'. Delivered value was: " + sAnalyseMapFormat);
						throw se;
				}
			}

			//BGColor
			if (analysemapOutTypeArray[i].isSetBGcolor())
				if(!analysemapOutTypeArray[i].getBGcolor().equals("") && analysemapOutTypeArray[i].getBGcolor() != null)
					sAnalyseMapBGcolor = analysemapOutTypeArray[i].getBGcolor();
			//Transparent
			if (analysemapOutTypeArray[i].isSetTransparent())
				boolAnalyseMapTransparent = analysemapOutTypeArray[i].getTransparent();
			
			//AccessibilityMapPreference
			String sAccessMapPref = "";
			ArrayList<Polygon> arraylistPolygonsForMap = new ArrayList<Polygon>();
			ArrayList<LineString> arraylistLineStringsForMap = new ArrayList<LineString>();
			if(analysemapOutTypeArray[i].isSetAccessibilityMapPreference()){
				AccessibilityMapPreferenceType.Enum mappref = analysemapOutTypeArray[i].getAccessibilityMapPreference();
				sAccessMapPref = mappref.toString();
				
				//DETAILED_POLYGON
				if(sAccessMapPref.equalsIgnoreCase(AccessibilityMapPreferenceType.DETAILED_POLYGON.toString())){
					arraylistPolygonsForMap.addAll(arraylistPolygons);
				}
				//CONVEX_POLYGON
				if(sAccessMapPref.equalsIgnoreCase(AccessibilityMapPreferenceType.CONVEX_POLYGON.toString())){
					Polygon polyTMP = arraylistPolygons.get(0);
					arraylistPolygonsForMap.add((Polygon)polyTMP.convexHull());
				}
				//STREETS_BUFFER
				if(sAccessMapPref.equalsIgnoreCase(AccessibilityMapPreferenceType.STREETS_BUFFER.toString())){
					
					//Create FeatureCollection Accessibility Polygons
					FeatureCollection featcoll;
					FeatureSchema featschGraph = new FeatureSchema();
					featschGraph.addAttribute("Geometry", AttributeType.GEOMETRY);
					featcoll = new FeatureDataset(featschGraph);
					
					for(int j=0 ; j<arraylistPolygons.size() ; j++){
						Feature feat = new BasicFeature(featschGraph);
						feat.setAttribute("Geometry", arraylistPolygons.get(j));
						featcoll.add(feat);
					}

//					//Overlay / Intersection
//					Overlay doOverlay = new Overlay(GraphManager.getInstance().getIndexFeatCollGraph(), featcoll);
//					FeatureCollection featcollOut = doOverlay.getResultFeatColl();
//					List listFeature = featcollOut.getFeatures();
//					
//					for(int j=0 ; j<listFeature.size() ; j++){
//						Feature feat = (Feature) listFeature.get(j);
//
//						if(feat.getGeometry() instanceof LineString)
//							arraylistLineStringsForMap.add((LineString)feat.getGeometry());
//					}
					
					//TODO
					// not supported
					
				}
			}else{
				sAccessMapPref = AccessibilityMapPreferenceType.DETAILED_POLYGON.toString();
				arraylistPolygonsForMap.addAll(arraylistPolygons);
			}
				

			///////////////////////////////////////
			//*** Create new AnalyseMapResponse Type ***
			analysemapType[i] = arrespType.addNewAccessibilityMap();// AccessibilityMapType.Factory.newInstance();
			//Rectangular area to be displayed in the rendered map.
			if (analysemapOutTypeArray[i].isSetBBoxContext()) {
				analysemapType[i].setDescription("MapNumber: "+iMapNumber+" - "+sAccessMapPref);
				envelopeBBoxType = analysemapOutTypeArray[i].getBBoxContext();
				
				//Coordinate Transform
				if(envelopeBBoxType.getSrsName() == null){
					DirectPositionType pos0 = envelopeBBoxType.getPosArray(0);
					Coordinate cPos0 = Pos.getCoord(pos0.getStringValue());
					try{
						cPos0 = CoordTransform.transformGetCoord(sSRS, sResponseSRS, cPos0);
					}catch (org.freeopenls.error.ServiceError se) {
						// TODO: handle exception
					}
					DirectPositionType pos1 = envelopeBBoxType.getPosArray(1);
					Coordinate cPos1 = Pos.getCoord(pos1.getStringValue());
					try{
						cPos1 = CoordTransform.transformGetCoord(sSRS, sResponseSRS, cPos1);
					}catch (org.freeopenls.error.ServiceError se) {
						// TODO: handle exception
					}
					DirectPositionType pos0NEW = DirectPositionType.Factory.newInstance();
					pos0NEW.setStringValue(cPos0.x+" "+cPos0.y);
					DirectPositionType pos1NEW = DirectPositionType.Factory.newInstance();
					pos1NEW.setStringValue(cPos1.x+" "+cPos1.y);
					envelopeBBoxType.setPosArray(0, pos0NEW);
					envelopeBBoxType.setPosArray(1, pos1NEW);
				}
				//Coordinate Transform
				if(!sSRS.equals(envelopeBBoxType.getSrsName())){
					DirectPositionType pos0 = envelopeBBoxType.getPosArray(0);
					Coordinate cPos0 = Pos.getCoord(pos0.getStringValue());
					try{
						cPos0 = CoordTransform.transformGetCoord(sSRS, sResponseSRS, cPos0);
					}catch (org.freeopenls.error.ServiceError se) {
						// TODO: handle exception
					}
					DirectPositionType pos1 = envelopeBBoxType.getPosArray(1);
					Coordinate cPos1 = Pos.getCoord(pos1.getStringValue());
					try{
						cPos1 = CoordTransform.transformGetCoord(sSRS, sResponseSRS, cPos1);
					}catch (org.freeopenls.error.ServiceError se) {
						// TODO: handle exception
					}
					DirectPositionType pos0NEW = DirectPositionType.Factory.newInstance();
					pos0NEW.setStringValue(cPos0.x+" "+cPos0.y);
					DirectPositionType pos1NEW = DirectPositionType.Factory.newInstance();
					pos1NEW.setStringValue(cPos1.x+" "+cPos1.y);
					envelopeBBoxType.setPosArray(0, pos0NEW);
					envelopeBBoxType.setPosArray(1, pos1NEW);
				}
			}else{//If not specified, defaults to full Accessibility
				analysemapType[i].setDescription("MapNumber: "+iMapNumber+" - "+sAccessMapPref);
			}

			///////////////////////////////////////
			//Calculate bbox
			DirectPositionType dp0 = envelopeBBoxType.getPosArray(0);
			DirectPositionType dp1 = envelopeBBoxType.getPosArray(1);

			Coordinate cMin = new Coordinate(Double.parseDouble( dp0.getStringValue().substring(0, dp0.getStringValue().indexOf(" "))),
					Double.parseDouble( dp0.getStringValue().substring(dp0.getStringValue().indexOf(" "), dp0.getStringValue().length())));
			
			Coordinate cMax = new Coordinate(Double.parseDouble( dp1.getStringValue().substring(0, dp1.getStringValue().indexOf(" "))),
					Double.parseDouble( dp1.getStringValue().substring(dp1.getStringValue().indexOf(" "), dp1.getStringValue().length())));
			
		
			double dXLower = cMin.x;
			double dYLower = cMin.y;
			double dXUpper = cMax.x;
			double dYUpper = cMax.y;
			double dXTMP = Math.abs( dXUpper - dXLower );
			double dYTMP = Math.abs( dYUpper - dYLower );
			
//			double dXLower = Double.parseDouble( dp0.getStringValue().substring(0, dp0.getStringValue().indexOf(" ")));
//			double dYLower = Double.parseDouble( dp0.getStringValue().substring(dp0.getStringValue().indexOf(" "), dp0.getStringValue().length()));
//			double dXUpper = Double.parseDouble( dp1.getStringValue().substring(0, dp1.getStringValue().indexOf(" ")));
//			double dYUpper = Double.parseDouble( dp1.getStringValue().substring(dp1.getStringValue().indexOf(" "), dp1.getStringValue().length()));
//			double dXTMP = Math.abs( dXUpper - dXLower );
//			double dYTMP = Math.abs( dYUpper - dYLower );
//System.out.println(dXLower+" "+dYLower);
//System.out.println(dXUpper+" "+dYUpper);
//System.out.println(dXTMP+" "+dYTMP);

			if(dXTMP > dYTMP){
				double dTMP = (dXTMP - dYTMP) / 2;
				if(dYLower < dYUpper){
					dYLower = dYLower - dTMP;
					dYUpper = dYUpper + dTMP;
				}
				else{
					dYLower = dYLower + dTMP;
					dYUpper = dYUpper - dTMP;
				}
			}else if(dYTMP > dXTMP){
				double dTMP = (dYTMP - dXTMP) / 2;
				if(dXLower < dXUpper){
					dXLower = dXLower - dTMP;
					dXUpper = dXUpper + dTMP;
				}
				else{
					dXLower = dXLower + dTMP;
					dXUpper = dXUpper - dTMP;
				}
			}
			
			///////////////////////////////////////
			//*** Increase the BoundingBox ***
			dXTMP = Math.abs( dXUpper - dXLower ) * 0.05;
			dYTMP = Math.abs( dYUpper - dYLower ) * 0.05;
			
			cMin.x = dXLower - dXTMP;
			cMin.y = dYLower - dYTMP;
			cMax.x = dXUpper + dXTMP;
			cMax.y = dYUpper + dYTMP;
			
			dXLower = cMin.x;
			dYLower = cMin.y;
			dXUpper = cMax.x;
			dYUpper = cMax.y;

			//Transform, if neccessary!!
			if(!sSRS.equals(sResponseSRS)){
				try{
					cMin = CoordTransform.transformGetCoord(sSRS, sResponseSRS, cMin);
					cMax = CoordTransform.transformGetCoord(sSRS, sResponseSRS, cMax);
				}catch (org.freeopenls.error.ServiceError se) {
					// TODO: handle exception
				}
			}
			
			//Set BoundingBox
			DirectPositionType pos0 = DirectPositionType.Factory.newInstance();
			pos0.setStringValue(cMin.x+" "+cMin.y);
			DirectPositionType pos1 = DirectPositionType.Factory.newInstance();
			pos1.setStringValue(cMax.x+" "+cMax.y);
			envelopeBBoxType.setPosArray(0, pos0);
			envelopeBBoxType.setPosArray(1, pos1);

			///////////////////////////////////////
			//*** Add Map ***
			MapType mapType = analysemapType[i].addNewMap();
				
			///////////////////////////////////////
			//*** Add Content ***
			ContentType contentType = mapType.addNewContent();
			contentType.setFormat(sAnalyseMapFormat);
			contentType.setHeight(new BigInteger(sAnalyseMapHeight));
			contentType.setWidth(new BigInteger(sAnalyseMapWidth));
			contentType.setURL(AASconfig.getMapsPathWWW()+"/"+sAnalyseRequestID+"_"+iMapNumber+"."+sAnalyseMapFormat);

			///////////////////////////////////////
			//*** Add BBoxContent ***
			EnvelopeType env = mapType.addNewBBoxContext();
			env.setSrsName("EPSG:"+sResponseSRS);
			env.setPosArray(envelopeBBoxType.getPosArray());

			///////////////////////////////////////
			//*** Create Analyse Map ***
//			GetMapWMS accessMap = new GetMapWMS(AASconfig);
//			accessMap.getMap(sAnalyseRequestID, sSRS, iMapNumber, dXLower, dYLower, dXUpper, dYUpper,
//					boolAnalyseMapTransparent, sAnalyseMapBGcolor, sAnalyseMapHeight, sAnalyseMapWidth, 
//					sAnalyseMapFormat, cLocation, arraylistPolygonsForMap, 
//					AASconfig.getColorAccessbilityPolygon(), arraylistLineStringsForMap,
//					AASconfig.getWMSUnderLayers(), AASconfig.getWMSUnderStyles(), 
//					AASconfig.getWMSUpperLayers(), AASconfig.getWMSUpperStyles());
			iMapNumber++;
		}

		return analysemapType;
	}
}
