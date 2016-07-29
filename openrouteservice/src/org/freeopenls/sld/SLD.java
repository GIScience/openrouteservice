

package org.freeopenls.sld;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import net.opengis.gml.BoxType;
import net.opengis.gml.CoordType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.LineStringType;
import net.opengis.ows.ExceptionsType;
import net.opengis.ows.FormatType;
import net.opengis.ows.GetMapDocument;
import net.opengis.ows.OWSType;
import net.opengis.ows.GetMapDocument.GetMap;
import net.opengis.ows.GetMapDocument.GetMap.Output;
import net.opengis.ows.GetMapDocument.GetMap.Output.Size;
import net.opengis.sld.NamedLayerDocument.NamedLayer;
import net.opengis.sld.NamedStyleDocument.NamedStyle;
import net.opengis.sld.StyledLayerDescriptorDocument.StyledLayerDescriptor;
import net.opengis.sld.UserStyleDocument.UserStyle;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.MapType;
import net.opengis.xls.PointOfInterestType;
import net.opengis.xls.PositionType;
import net.opengis.xls.RouteGeometryType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.StyleType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.freeopenls.error.ServiceError;
import org.freeopenls.gml.Polygon;
import org.freeopenls.gml.Pos;
import org.freeopenls.presentationservice.documents.RequestXLSDocument.LayerAndStyle;
import org.freeopenls.presentationservice.documents.RequestXLSDocument.OverlayADT;
import org.freeopenls.tools.CoordTransform;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * <p><b>Title: FileDelete</b></p>
 * <p><b>Description:</b> Class for FileDelete<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-12-10
 */
public class SLD {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger.getLogger(SLD.class.getName());
	
	public static String createSLD(ArrayList<LayerAndStyle> arraylistLayerNameAndStyle, ArrayList<OverlayADT> alOverlayADT,
			boolean boolMapTransparent, String sMapBGcolor,
			String sSRS, double dXLower, double dYLower, double dXUpper, double dYUpper,
			String sMapHeight, String sMapWidth, 
			String filePath, String fileName)throws ServiceError{
		
		GetMapDocument getmapDoc = GetMapDocument.Factory.newInstance();
		
		try{
			GetMap getmap = getmapDoc.addNewGetMap();
			getmap.setVersion("1.2.0");
			getmap.setService(OWSType.WMS);

			XmlCursor cursor = getmapDoc.newCursor();
			if (cursor.toFirstChild()) {
				cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation"),
						"http://www.opengis.net/ows/"+"GetMap.xsd");
			}
			cursor.dispose();

			//Add StyledLayerDescriptor
			StyledLayerDescriptor styledescr = getmap.addNewStyledLayerDescriptor();
			styledescr.setVersion("1.0.0");

			///////////////////////////////////////
			//*** Add Layers and Styles to GetMap Request ***
			for(int i=0 ; i<arraylistLayerNameAndStyle.size() ; i++){
				NamedLayer namLayer = styledescr.addNewNamedLayer();
				namLayer.setName(arraylistLayerNameAndStyle.get(i).getLayerName());

				//Style
				if(arraylistLayerNameAndStyle.get(i) == null){
					NamedStyle namStyle = namLayer.addNewNamedStyle();
					namStyle.setName("");
				}
				else if(arraylistLayerNameAndStyle.get(i).getStyleType().isSetName()){
					NamedStyle namStyle = namLayer.addNewNamedStyle();
					namStyle.setName(arraylistLayerNameAndStyle.get(i).getStyleType().getName());
				}
				else{
					UserStyle us = namLayer.addNewUserStyle();
					us.set(XmlObject.Factory.parse(arraylistLayerNameAndStyle.get(i).getStyleType().getStyleContent()));
				}					
			}

			///////////////////////////////////////
			//*** Add Overlay Elements to GetMap Request ***
			addADT(styledescr, alOverlayADT);
			
			///////////////////////////////////////
			//*** BoundingBox ***
			BoxType box = getmap.addNewBoundingBox();
			box.setSrsName("http://www.opengis.net/gml/srs/epsg.xml#"+sSRS);
			CoordType coordLower = box.addNewCoord();
			double dAddition = 0;
			coordLower.setX(new BigDecimal(dXLower-dAddition));
			coordLower.setY(new BigDecimal(dYLower-dAddition));
			CoordType coordUpper = box.addNewCoord();
			coordUpper.setX(new BigDecimal(dXUpper+dAddition));
			coordUpper.setY(new BigDecimal(dYUpper+dAddition));

			///////////////////////////////////////
			//*** Output ***
			Output out = getmap.addNewOutput();
			out.setFormat(FormatType.IMAGE_PNG);
			out.setTransparent(boolMapTransparent);
			out.setBGcolor(sMapBGcolor);
			Size size = Size.Factory.newInstance();
			size.setHeight(new BigInteger(sMapHeight));
			size.setWidth(new BigInteger(sMapWidth));
			out.setSize(size);
			///////////////////////////////////////
			//*** Exception ***
			ExceptionsType.Enum e = ExceptionsType.Enum.forString("application/vnd.ogc.se+xml");
			getmap.setExceptions(e);
			
		} catch (XmlException xmle) {
			mLogger.error(xmle);
			ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.UNKNOWN, "SLD", "XML-Problem! Message: "+se.getMessages());
            throw se;
		}

		try{
			if(filePath != null && fileName != null){
				String file = filePath+"/"+fileName;
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(getmapDoc.toString().getBytes());
				fos.close();
			}
		}catch(IOException ioe){
			mLogger.error(ioe);
			ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.UNKNOWN, "SLD", "IO-Problem! Message: "+se.getMessages());
            throw se;
		}
		
		return getmapDoc.toString();
	}
	
	/**
	 * Method that first sort the Layers in the right order and later add it to the SLD-File for GetMap
	 * 
	 * @param styledescr
	 * @param alOverlayADT
	 * @throws ServiceError
	 * @throws XmlException
	 */
	private static void addADT(StyledLayerDescriptor styledescr, ArrayList<OverlayADT> alOverlayADT)throws ServiceError, XmlException{
		ArrayList<OverlayADT> alOverlayLayer = new ArrayList<OverlayADT>();
		Object aOverlayADT[] = alOverlayADT.toArray();

		///////////////////////////////////////
		//Sort List by iIndex of OverlayADT element
		for(int i=1 ; i<aOverlayADT.length ; i++){
			for(int j=0 ; j<alOverlayADT.size()-1 ; j++){
				OverlayADT olI = (OverlayADT) aOverlayADT[i];
				OverlayADT olJ = (OverlayADT) aOverlayADT[j];
				
				if(olI.getIndex() > olJ.getIndex()){
					OverlayADT tmp = (OverlayADT) aOverlayADT[i];
					aOverlayADT[i] = aOverlayADT[j];
					aOverlayADT[j] = tmp;
				}
			}
		}
		for(int i=0 ; i<aOverlayADT.length ; i++){
			alOverlayLayer.add((OverlayADT)aOverlayADT[i]);
		}

		///////////////////////////////////////
		//Add Layer to WMS-SLD Request
		for(int i=0 ; i<alOverlayLayer.size() ; i++){
			Object obj = alOverlayLayer.get(i).getObject();
			StyleType style = alOverlayLayer.get(i).getStyle();
			
			String sSRS = "4326";
			
			//ADT PointOfInterest
			if(obj instanceof PointOfInterestType){
				PointOfInterestType poi = (PointOfInterestType) obj;
				String sPOIName = "POI";
				String sPOIDescription = " "+i;
				Coordinate cPoint = new Coordinate();
				
				if(poi.isSetPOIName()) sPOIName = poi.getPOIName();
				if(poi.isSetDescription()) sPOIDescription = "- "+poi.getDescription();
				if(poi.isSetPoint()){
					cPoint = Pos.getCoord(poi.getPoint().getPos().getStringValue());
					if(poi.getPoint().isSetSrsName())
						sSRS = CoordTransform.getEPSGCodeNumber(poi.getPoint().getSrsName());
				}
				else{
					//TODO: POI IS NOT SUPPORTED!!!
					ServiceError se = new ServiceError(SeverityType.ERROR);
		            se.addError(ErrorCodeType.NOT_SUPPORTED,
		                                 "POI",
		                                 "It's only POI:Point supported!");
		            throw se;
				}

				if(style != null){
					if(style.isSetName()){
						ServiceError se = new ServiceError(SeverityType.ERROR);
			            se.addError(ErrorCodeType.NOT_SUPPORTED,
			                                 "POI - Style",
			                                 "It's not possible to use a NamedStyle in POI ADT!");
			            throw se;
					}
					if(style.isSetStyleContent()){
						String sTMP = style.getStyleContent();
						String sLandmarkGraphikPath = sTMP.substring(0, sTMP.indexOf(";"));
						sTMP = sTMP.substring(sTMP.indexOf(";")+1);
						String sLandmarkGraphikFormat = sTMP.substring(0, sTMP.indexOf(";"));
						sTMP = sTMP.substring(sTMP.indexOf(";")+1);
						String sLandmarkSize = sTMP.substring(0, sTMP.indexOf(";"));

						//*** Add User Point ***
						UserLayerPoint layerPoint = new UserLayerPoint("http://www.opengis.net/gml/srs/epsg.xml#"+sSRS);
						layerPoint.addPointGraphic(styledescr, cPoint, sPOIName+sPOIDescription, "#00000", "10", 
								sLandmarkSize, sLandmarkGraphikPath, sLandmarkGraphikFormat);
								//"30", "http://localhost:8080/geoserver/data/symbols/endflag.gif", "image/gif");
					}
				}else{
					//*** Add Default Point ***
					UserLayerPoint layerPoint = new UserLayerPoint("http://www.opengis.net/gml/srs/epsg.xml#"+sSRS);
					layerPoint.addPoint(styledescr, cPoint, sPOIName+sPOIDescription, "#00000", "12", "12.0", "1.0");
				}
			}

			//ADT RouteGeometry
			if(obj instanceof RouteGeometryType){
				RouteGeometryType rg = (RouteGeometryType) obj;
				LineStringType linestring = rg.getLineString();
				ArrayList<Coordinate> alRouteGeom = new ArrayList<Coordinate>();

				if(linestring.isSetSrsName())
					sSRS = CoordTransform.getEPSGCodeNumber(linestring.getSrsName());
				
				DirectPositionType direct[] = linestring.getPosArray();
				for(int j=0 ; j<direct.length ; j++)
					alRouteGeom.add(Pos.getCoord(direct[j].getStringValue()));
				
				//TODO - Style
				if(style != null){
					if(style.isSetName()){
						ServiceError se = new ServiceError(SeverityType.ERROR);
			            se.addError(ErrorCodeType.NOT_SUPPORTED,
			                                 "RouteGeomtry - Style",
			                                 "It's not possible to use a NamedStyle in RouteGeometry ADT!");
			            throw se;
					}
					if(style.isSetStyleContent()){
						//TODO
						
					}
				}
				else{
					///////////////////////////////////////
					//*** Add LinseString to GetMap Request ***
					UserLayerLineString layerRoute = new UserLayerLineString("http://www.opengis.net/gml/srs/epsg.xml#"+sSRS);
					layerRoute.addLineStrings(styledescr, "RouteGeom", alRouteGeom, "#FA0000", "4", "1.0");
					
					//StartPoint
					UserLayerPoint startPoint = new UserLayerPoint("http://www.opengis.net/gml/srs/epsg.xml#"+sSRS);
					startPoint.addPoint(styledescr, 
							Pos.getCoord(linestring.getPosArray()[0].getStringValue()),
							"Start", "#FA0000", "12", "8", "1.0");
					//EndPoint
					UserLayerPoint endPoint = new UserLayerPoint("http://www.opengis.net/gml/srs/epsg.xml#"+sSRS);
					//endPoint.addPointGraphic(styledescr,
					endPoint.addPoint(styledescr, 
							Pos.getCoord(linestring.getPosArray()[linestring.getPosArray().length-1].getStringValue()),
							"End", "#FA0000", "12", "8", "1.0");
							//"End", "#FA0000", "12", "30");
							//mPSConfigurator.getRouteMapDestinationSymbolPath(),
							//mPSConfigurator.getRouteMapDestinationSymbolFormat());
				}
			}

			//ADT Position
			if(obj instanceof PositionType){
				PositionType position = (PositionType) obj;
				
				//position.getPoint();
				if(position.isSetPolygon()){
					if(position.getPolygon().isSetSrsName())
						sSRS = CoordTransform.getEPSGCodeNumber(position.getPolygon().getSrsName());
					
					Polygon poly = new Polygon(sSRS, position.getPolygon());
					//*** Add Polygon to GetMap Request ***
					UserLayerPolygon layerPolygon = new UserLayerPolygon("http://www.opengis.net/gml/srs/epsg.xml#"+sSRS);
					layerPolygon.addPolygons(styledescr, "Polygon", poly.getPolygon(), "#FA0000", "0.6");
				}
				else if(position.isSetCircleByCenterPoint()){
					//TODO
				}
				else{
					//TODO: POSITION IS NOT SUPPORTED!!!
					ServiceError se = new ServiceError(SeverityType.ERROR);
		            se.addError(ErrorCodeType.NOT_SUPPORTED,
		                                 "OverLayer Position ADT",
		                                 "OverLayer Position ADT is not yet supported!");
		            throw se;					
				}

				if(style != null){
					if(style.isSetName()){}
					if(style.isSetStyleContent()){}
				}
			}

			//ADT Map
			if(obj instanceof MapType){
				MapType map = (MapType) obj;
				
				map.isSetBBoxContext();
				map.isSetCenterContext();
				map.isSetContent();
				//TODO: MAP IS NOT SUPPORTED!!!
				ServiceError se = new ServiceError(SeverityType.ERROR);
	            se.addError(ErrorCodeType.NOT_SUPPORTED,
	                                 "OverLayer Map ADT",
	                                 "OverLayer Map ADT is not yet supported!");
	            throw se;
/*
				//TODO - Style
				if(style != null){
					if(style.isSetName()){}
					if(style.isSetStyleContent()){}
				}
*/
			}
		}
		
	}
}
