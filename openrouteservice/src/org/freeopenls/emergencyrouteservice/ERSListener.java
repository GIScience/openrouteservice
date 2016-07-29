/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.emergencyrouteservice;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;


import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.freeopenls.emergencyrouteservice.RespRouteXLSDoc;
import org.freeopenls.error.ServiceError;
import org.freeopenls.location.WayPointList;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.vividsolutions.jts.geom.Coordinate;

import net.opengis.gml.AbstractRingPropertyType;
import net.opengis.gml.AbstractRingType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.PointType;
import net.opengis.gml.PolygonType;
import net.opengis.xls.AbstractBodyType;
import net.opengis.xls.AbstractLocationType;
import net.opengis.xls.AbstractRequestParametersType;
import net.opengis.xls.AreaOfInterestType;
import net.opengis.xls.AvoidListType;
import net.opengis.xls.DetermineRouteRequestType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.PositionType;
import net.opengis.xls.RequestType;
import net.opengis.xls.RoutePlanType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.WayPointListType;
import net.opengis.xls.WayPointType;
import net.opengis.xls.XLSDocument;
import net.opengis.xls.XLSType;



/**
 * <p><b>Title: Class ERSListener </b></p>
 * <p><b>Description:</b> handles the RouteRequest to the ERS </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-08-01
 */
public class ERSListener {
	/** Logger, used to log errors(exceptions) and additionally information */
    private static final Logger mLogger = Logger.getLogger(ERSListener.class.getName());
    private static final Logger mLoggerCounter = Logger.getLogger(ERSListener.class.getName()+".Counter");
    /** ERSConfigurator **/
    private ERSConfigurator mERSConfigurator;

    /**
     * Constructor
     */
    public ERSListener() {
    	mERSConfigurator = ERSConfigurator.getInstance();
    }

    /**
     * Method that validate the request, send the RouteRequest(s) to Route Service<br>
     * and return the RouteResponse.<br>
     * 
     * @param request
     *			XmlObject that contains the XLSDocument
     * @return RespRouteXLSDoc
     * 			- Returns Response XLSDocument from the RouteService
     */
    public synchronized RespRouteXLSDoc receiveCompleteRequest(XmlObject request) {

		RespRouteXLSDoc response = null;
		XLSDocument xlsDoc = (XLSDocument) request;
		
		Coordinate cSource = null;
		Coordinate cDestination = null;
		int numberofAvoidAreasBefore = 0;
		int numberofAvoidAreasAfter = 0;

		try {
			///////////////////////////////////////
			//*** Validate XLSDoc ***
			validatexlsDoc(xlsDoc);
			
			//XLSDoc - resolve in types
			XLSType xlsType = xlsDoc.getXLS();					//*** XLSType ***
			AbstractBodyType abType[] = xlsType.getBodyArray();	//*** Header / Body ***

			//Add AvoidList and/or new AvoidAreas/Location
			int iNumberReceivedRequests = abType.length;
			for(int i=0; i < iNumberReceivedRequests ; i++){
				RequestType reqType = (RequestType) abType[i].changeType(RequestType.type);
				
				AbstractRequestParametersType abreqparType = reqType.getRequestParameters();
				DetermineRouteRequestType drrType = (DetermineRouteRequestType) abreqparType.changeType(DetermineRouteRequestType.type);
		
				if(drrType.isSetRoutePlan()){
					RoutePlanType routeplanType = drrType.getRoutePlan();
					AvoidListType avoidlistType = null;
					
					if(routeplanType.isSetAvoidList())
						avoidlistType = routeplanType.getAvoidList();
					else
						avoidlistType = routeplanType.addNewAvoidList();
					
					numberofAvoidAreasBefore = avoidlistType.sizeOfAOIArray();

				    ///////////////////////////////////////
					//*** GetWayPoints ***
					WayPointList wpl = new WayPointList(routeplanType, 
							mERSConfigurator.getOpenLSLocationUtilityServicePath(),
							mERSConfigurator.getOpenLSDirectoryServicePath());
					//Source/Start
					cSource = wpl.getSource().getCoordinate();
					//Destination/End
					cDestination = wpl.getDestination().getCoordinate();
					//Via Points
					Coordinate cViaPoints[] = null;
					if(wpl.getArrayViaPoints() != null){
						cViaPoints = new Coordinate[wpl.getArrayViaPoints().length];
						for(int index=0 ; index<wpl.getArrayViaPoints().length ; index++)
							cViaPoints[index] = wpl.getArrayViaPoints()[index].getCoordinate();
					}

				    ///////////////////////////////////////
					//*** SetWayPoints in Coordinate-Form ***
					WayPointListType waypointList = routeplanType.getWayPointList();
					removeANDaddNewLocation(waypointList.getStartPoint(), cSource.x, cSource.y);
					removeANDaddNewLocation(waypointList.getEndPoint(), cDestination.x, cDestination.y);
					
					///////////////////////////////////////
					//*** Create BBox - minX,minY AND maxX,maxY ***
					Coordinate cMin = new Coordinate();
					Coordinate cMax = new Coordinate();
					//Min
					if(cSource.y > cDestination.y) cMin.y = cDestination.y; else cMin.y = cSource.y;
					if(cSource.x > cDestination.x) cMin.x = cDestination.x; else cMin.x = cSource.x;
					//Max
					if(cSource.y < cDestination.y) cMax.y = cDestination.y; else cMax.y = cSource.y;
					if(cSource.x < cDestination.x) cMax.x = cDestination.x; else cMax.x = cSource.x;
					
					//ViaPoints for BBox
					if(cViaPoints != null){
						for (int j = 0; i < cViaPoints.length; i++) {
							if (cMin.x > cViaPoints[j].x) cMin.x = cViaPoints[j].x;
							if (cMax.x < cViaPoints[j].x) cMax.x = cViaPoints[j].x;
							if (cMin.y > cViaPoints[j].y) cMin.y = cViaPoints[j].y;
							if (cMax.y < cViaPoints[j].y) cMax.y = cViaPoints[j].y;
						}
					}
					
					//Extend the BBox
					cMin.x = cMin.x-getAngle(mERSConfigurator.getWFSBBoxExtend());
					cMin.y = cMin.y-getAngle(mERSConfigurator.getWFSBBoxExtend());
					cMax.x = cMax.x+getAngle(mERSConfigurator.getWFSBBoxExtend());
					cMax.y = cMax.y+getAngle(mERSConfigurator.getWFSBBoxExtend());

//--------------------------------------------------------------------

				    ///////////////////////////////////////
					//*** Request to WFS ***
					String sRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
										+"<wfs:GetFeature service=\"WFS\" version=\"1.0.0\" \n"
										+" outputFormat=\"GML2\" \n"
										+" "+mERSConfigurator.getWFS_XMLNS()+" \n"
										+" xmlns:wfs=\"http://www.opengis.net/wfs\" \n"
										+" xmlns:ogc=\"http://www.opengis.net/ogc\" \n"
										+" xmlns:gml=\"http://www.opengis.net/gml\" \n"
										+" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
										+" xsi:schemaLocation=\"http://www.opengis.net/wfs \n"
										+"         http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd\"> \n"
										+"	  <wfs:Query typeName=\""+mERSConfigurator.getWFSLayername()+"\"> \n"
										+"	    <ogc:Filter> \n"
										+"	      <ogc:BBOX> \n"
										+"	        <ogc:PropertyName>"+mERSConfigurator.getWFSColumName()+"</ogc:PropertyName> \n"
										+"	        <gml:Box srsName=\""+mERSConfigurator.getWFSSRS()+"\"> \n"
										+"	           <gml:coordinates>"+cMin.x+","+cMin.y+" "+cMax.x+","+cMax.y+"</gml:coordinates> \n"
										+"	        </gml:Box> \n"
										+"	      </ogc:BBOX> \n"
										+"	    </ogc:Filter> \n"
										+"    </wfs:Query> \n"
										+"</wfs:GetFeature>";

					// Send data
					URL u = new URL(mERSConfigurator.getWFSPath());
	                HttpURLConnection acon = (HttpURLConnection) u.openConnection();
	                acon.setAllowUserInteraction(false);
	                acon.setRequestMethod("POST");
	                acon.setRequestProperty("Content-Type", "application/xml");
	                acon.setDoOutput(true);
	                acon.setDoInput(true);
	                acon.setUseCaches(false);
	                PrintWriter xmlOut = null;
	                xmlOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(acon.getOutputStream())));
	                xmlOut = new java.io.PrintWriter(acon.getOutputStream());
//System.out.println("WFS Request: "+sRequest);
	                xmlOut.write(sRequest);
	                xmlOut.flush();
	                xmlOut.close();

					// Get the response
	                InputStream is = acon.getInputStream();
	                //ArrayList for TMP the Coordinates
	                ArrayList<String> arrayAvoidPolygonsCoordStrings = new ArrayList<String>();
	                
	   
	        		//Read XML InsputStream
	        		SAXBuilder builder = new SAXBuilder();
	        		Document doc = builder.build(is);
	        		is.close();
	        		
	        		//RootElement
	        		Element elementRoot = doc.getRootElement();
//System.out.println("WFS Response: "+is.toString());
	        		List listMainElements = elementRoot.getChildren();
	        		
	        		for (int j = 0; j < listMainElements.size(); j++) {
	        			Element elementMain = (Element) (listMainElements.get(j));

	        			if (elementMain.getName().equals("featureMember")) {
	        				List listCildren = elementMain.getChildren();

	        				for (int k = 0; k < listCildren.size(); k++) {
	        					Element elementLayer = (Element) (listCildren.get(k));
	        					List listLayerChildren = elementLayer.getChildren();

	        					for (int l = 0; l < listLayerChildren.size(); l++) {
	        						Element elementTMP = (Element) (listLayerChildren.get(l));
	        							
	        						//Add Geometry to the Arraylist
	        						if (elementTMP.getName().equals(mERSConfigurator.getWFSColumName()))
	        							arrayAvoidPolygonsCoordStrings.add(elementTMP.getValue().trim());
	        					}
	        				}
	        			}
	        		}

				    ///////////////////////////////////////
					//*** Add AOI / Polygon ***
	        		for(int iIndex=0 ; iIndex < arrayAvoidPolygonsCoordStrings.size() ; iIndex++){
						//Add new AOI and Polygon
						AreaOfInterestType aoi = avoidlistType.addNewAOI();
						PolygonType polygonType = aoi.addNewPolygon();
						AbstractRingPropertyType aringprop = polygonType.addNewExterior();
						AbstractRingType aring = aringprop.addNewRing();
						LinearRingType linearRing = (LinearRingType) aring.changeType(LinearRingType.type);
						linearRing.setSrsName(mERSConfigurator.getWFSSRS());
						
						String sCoordsTMP = arrayAvoidPolygonsCoordStrings.get(iIndex);
						
						//Set Coordinates in gml:pos in gml:polygon
						// TMP First==Last Point for a closed LineString 
						String sPointTMP = sCoordsTMP.substring(0, sCoordsTMP.indexOf(" ")).replace( "," , " " );
						while (sCoordsTMP.indexOf(" ") > 0) {
							DirectPositionType posType = linearRing.addNewPos();
							posType.setStringValue(sCoordsTMP.substring(0, sCoordsTMP.indexOf(" ")).replace( "," , " " ));
							sCoordsTMP = sCoordsTMP.substring(sCoordsTMP.indexOf(" ") + 1, sCoordsTMP.length());
						}
						//Last Point!!
						DirectPositionType posType = linearRing.addNewPos();
						posType.setStringValue(sPointTMP);
						
						
					//For well-formed xml
					//---
						XmlCursor xlsCursor = aringprop.newCursor();
						if (xlsCursor.toFirstChild()) {
							xlsCursor.setName(new QName("http://www.opengis.net/gml","LinearRing"));
						}
						xlsCursor.dispose();
					//---
					}
	        		numberofAvoidAreasAfter = arrayAvoidPolygonsCoordStrings.size();
				}
			}
			
			mLoggerCounter.info(" ERS ; ; ; AvoidAreas ; Before: "+numberofAvoidAreasBefore+
    				" ; After: "+(numberofAvoidAreasAfter+numberofAvoidAreasBefore));
			
//--------------------------------------------------------------------

		    ///////////////////////////////////////
			//*** Request to RouteService ***
			// Send data
			URL u = new URL(mERSConfigurator.getOpenLSRouteServicePath());
            HttpURLConnection acon = (HttpURLConnection) u.openConnection();
            acon.setAllowUserInteraction(false);
            acon.setRequestMethod("POST");
            acon.setRequestProperty("Content-Type", "application/xml");
            acon.setDoOutput(true);
            acon.setDoInput(true);
            acon.setUseCaches(false);
            PrintWriter xmlOut = null;
            xmlOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(acon.getOutputStream())));
            xmlOut = new java.io.PrintWriter(acon.getOutputStream());
//log.info(xlsDoc.toString());
            xmlOut.write(xlsDoc.toString());
            xmlOut.flush();
            xmlOut.close();

			// Get the response
            InputStream is = acon.getInputStream();

			//Create New XLSDoc with InputStream
			XLSDocument xlsNEW = XLSDocument.Factory.parse(is);
			is.close();
			
			response = new RespRouteXLSDoc(xlsNEW);

	}
	catch (ServiceError se) {
		mLogger.error(se);
            return new RespRouteXLSDoc(se.getErrorListXLSDocument(""));
	}
	catch (Exception e) {
		mLogger.error(e);
		ServiceError se = new ServiceError(SeverityType.ERROR);
        se.addInternalError(ErrorCodeType.UNKNOWN, "ERS", e);
        return new RespRouteXLSDoc(se.getErrorListXLSDocument(""));
	}
	return response;

	}

    /**
     * Method that validate XLSDocument
     * 
     * @param xlsDoc
     *			XLSDocument
     * @throws ServiceError
     */
    private void validatexlsDoc(XLSDocument xlsDoc) throws ServiceError {
    	//Create an XmlOptions instance and set the error listener.
        ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(validationErrors);

        //Validate the XML document
        boolean isValid = xlsDoc.validate(validationOptions);

        //Create exception with error message if the xml document is invalid
        if (!isValid) {
            String message = null;
            String parameterName = null;

            //Get validation error and throw service exception for the first error
            Iterator<XmlError> iter = validationErrors.iterator();
            while (iter.hasNext()) {
                //Get name of the missing or invalid parameter
                message = iter.next().getMessage();
                if (message != null) {
                    String[] messageParts = message.split(" ");

                    if (messageParts.length > 3) {
                        parameterName = messageParts[2];
                    }

                    //Create ServiceError
                    ServiceError se = new ServiceError(SeverityType.ERROR);
                    se.addError(ErrorCodeType.OTHER_XML, parameterName, "XmlBeans validation error: " + message);
                    throw se;
                }
            }
        }
    }
	/**
	 * Method that removes the old Location and adds a Position to a Waypoint
	 * 
	 * @param wp
	 * 			WayPoint
	 * @param sPosX
	 * @param sPosY
	 */
	private void removeANDaddNewLocation(WayPointType wp, double dPosX, double dPosY){
		XmlCursor cursorWPremove = wp.newCursor();
		cursorWPremove.removeXmlContents();
		cursorWPremove.dispose();
		
		AbstractLocationType ab = wp.addNewLocation();
		PositionType position = (PositionType) ab.changeType(PositionType.type);
		PointType point = position.addNewPoint();
		point.setSrsName(ERSConstants.GRAPH_SRS);
		DirectPositionType directpos = point.addNewPos();
		directpos.setStringValue(dPosX +" "+ dPosY);
		//------
			//For well formed XML-Doc
			XmlCursor cursorSetName = wp.newCursor();
			if (cursorSetName.toChild(new QName("http://www.opengis.net/xls", "_Location"))) {
				cursorSetName.setName(new QName("http://www.opengis.net/xls","Position"));
			}
			cursorSetName.dispose();
		//------
	}
	
	private double getAngle(double distance){
		return (180/Math.PI)*distance/6378000;
	}
}