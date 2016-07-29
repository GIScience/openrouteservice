
package org.freeopenls.routeservice.documents.map;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.freeopenls.connector.CreateXLSRequest;
import org.freeopenls.connector.WebServiceConnector;
import org.freeopenls.constants.RouteService;
import org.freeopenls.constants.RouteService.RouteMapRequestParameter;
import org.freeopenls.error.ServiceError;
import org.freeopenls.gml.Pos;
import org.freeopenls.presentationservice.documents.RequestXLSDocument.LayerAndStyle;
import org.freeopenls.presentationservice.documents.RequestXLSDocument.OverlayADT;
import org.freeopenls.routeservice.RSConfigurator;
import org.freeopenls.routeservice.documents.Envelope;
import org.freeopenls.routeservice.documents.instruction.DistanceUnit;
import org.freeopenls.routeservice.routing.RoutePlan;
import org.freeopenls.routeservice.routing.RouteResult;
import org.freeopenls.sld.SLD;
import org.freeopenls.sld.SLD3D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.PointType;
import net.opengis.xls.AbstractBodyType;
import net.opengis.xls.AbstractResponseParametersType;
import net.opengis.xls.CenterContextType;
import net.opengis.xls.ContentType;
import net.opengis.xls.DetermineRouteRequestType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.ErrorListType;
import net.opengis.xls.ErrorType;
import net.opengis.xls.MapType;
import net.opengis.xls.PortrayMapResponseType;
import net.opengis.xls.RadiusType;
import net.opengis.xls.ResponseType;
import net.opengis.xls.RouteGeometryType;
import net.opengis.xls.RouteMapOutputType;
import net.opengis.xls.RouteMapRequestType;
import net.opengis.xls.RouteMapStyleType;
import net.opengis.xls.RouteMapType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.XLSDocument;
import net.opengis.xls.XLSType;


/**
 * <p><b>Title: RouteMap</b></p>
 * <p><b>Description:</b> Class for Read the xls:RouteMapRequest element.<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-07-26
 * @version 1.1 2008-04-28
 */
public class RouteMap {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger.getLogger(RouteMap.class.getName());
	/** RSConfigurator */
	private RSConfigurator mRSConfigurator;

	/**
	 * Constructor - Set the Logger
	 *
	 */
	public RouteMap(){
    	mRSConfigurator = RSConfigurator.getInstance();
	}
	
    /**
     * Method that read every RouteMapRequest, send it to a OpenLS Presentation Service and create RouteMapResponse.<br>
     * 
     * @param drrType
     * @param routePlan
     * @param routeResult
     * @param AvoidAreas
     * @param AvoidAddresses
     * @return RouteMapType[]
     * @throws ServiceError
     */
    public RouteMapType[] createRouteMaps(DetermineRouteRequestType drrType, RoutePlan routePlan, RouteResult routeResult, ArrayList<Polygon> AvoidAreas, ArrayList<LineString> AvoidAddresses)throws ServiceError{

    	return null;
    	/* Runge 2016.04.28
		///////////////////////////////////////
		//Get RouteMapRequest from DetermineRouteRequest
		RouteMapRequestType routemapreqType = drrType.getRouteMapRequest();
		RouteMapOutputType routemapOutTypeArray[] = routemapreqType.getOutputArray(); //Mandatory
		//Create RouteMapType[] - RouteMapResponse
		RouteMapType routemapType[] = new RouteMapType[routemapOutTypeArray.length];
		
		for (int i = 0; i < routemapOutTypeArray.length; i++) {
			///////////////////////////////////////
			//Set variables for RouteMapRequest
			EnvelopeType envelopeBBox = routeResult.getEnvelopeRoute().getEnvelopeType();	//Optional		Default="FullRoute"
			String sRouteMapWidth = "400"; 						//Optional
			String sRouteMapHeight = "400"; 					//Optional
			String sRouteMapFormat = "png"; 					//Optional
			String sRouteMapBGcolor = "#FFFFFF"; 				//Optional		Default=#FFFFFF -> White
			Boolean boolRouteMapTransparent = false; 			//Optional		Default=false
			//RouteMapStyleType.Enum routemapstyleType = null; 	//Optional
			String sRouteMapStyle = RouteMapStyleType.OVERVIEW.toString();	//Optional	Default="Overview"
			
			String sMapURL = null;
			String sData = null;

			//Width
			if (routemapOutTypeArray[i].isSetWidth()){
				sRouteMapWidth = routemapOutTypeArray[i].getWidth().toString();
				if(sRouteMapWidth.equals("0")){
					ServiceError se = new ServiceError(SeverityType.ERROR);
					se.addError(ErrorCodeType.OTHER_XML,
	                                 RouteMapRequestParameter.width.toString(),
	                                 "The required value of the mandatory parameter '"
	                                 + RouteMapRequestParameter.width.toString() 
	                                 + "' must be '>0'. Delivered value was: " + sRouteMapWidth);
					throw se;
				}
			}

			//Height
			if (routemapOutTypeArray[i].isSetHeight()){
				sRouteMapHeight = routemapOutTypeArray[i].getHeight().toString();
				if(sRouteMapHeight.equals("0")){
					ServiceError se = new ServiceError(SeverityType.ERROR);
					se.addError(ErrorCodeType.OTHER_XML,
	                                 RouteMapRequestParameter.height.toString(),
	                                 "The required value of the mandatory parameter '"
	                                 + RouteMapRequestParameter.height.toString() 
	                                 + "' must be '>0'. Delivered value was: " + sRouteMapHeight);
					throw se;
				}
			}
			
			//Format
			if (routemapOutTypeArray[i].isSetFormat()){
				sRouteMapFormat = routemapOutTypeArray[i].getFormat();
			}
			
			//BGColor
			if (routemapOutTypeArray[i].isSetBGcolor())
				if(!routemapOutTypeArray[i].getBGcolor().equals("") && routemapOutTypeArray[i].getBGcolor() != null)
					sRouteMapBGcolor = routemapOutTypeArray[i].getBGcolor();
			//Transparent
			if (routemapOutTypeArray[i].isSetTransparent())
				boolRouteMapTransparent = routemapOutTypeArray[i].getTransparent();
			//Style
			if (routemapOutTypeArray[i].isSetStyle()) {
				//TODO
				//routemapstyleType = routemapOutTypeArray[i].getStyle();
				//sRouteMapStyle = routemapstyleType.toString();
			}

			//BBoxContext
			if (routemapOutTypeArray[i].isSetBBoxContext()) {
				sRouteMapStyle = RouteMapStyleType.MANEUVER.toString() + " " + i;
				envelopeBBox = routemapOutTypeArray[i].getBBoxContext();
			}else{//If not specified, defaults to full route.
				sRouteMapStyle = RouteMapStyleType.OVERVIEW.toString();
				envelopeBBox = createBBox(routeResult.getFeatCollSRS(), envelopeBBox);
			}

			///////////////////////////////////////
			//*** Create png, jpeg or gif RouteMap ***
			if(sRouteMapFormat.equals("png") || sRouteMapFormat.equals("jpeg") || sRouteMapFormat.equals("gif")){
				//XLS PortrayMap Request for OpenLS Presentation Service
				CreateXLSRequest req = new CreateXLSRequest("1.1", "1234", RouteService.GRAPH_SRS);
				XLSDocument xlsRequest = req.createPortrayMapRequest(envelopeBBox, sRouteMapFormat, sRouteMapHeight,
						sRouteMapWidth, sRouteMapBGcolor, boolRouteMapTransparent, routeResult.getFeatCollSRS(), routeResult.getFeatCollRoute(),
						AvoidAreas, AvoidAddresses, mRSConfigurator.getOpenLSPSLayers(), mRSConfigurator.getOpenLSPSStyles(), "1234");
	
				//Check OpenLS Path
				if(mRSConfigurator.getOpenLSPresentationServicePath().equals("") || mRSConfigurator.getOpenLSPresentationServicePath() == null){
					mLogger.error("OutputType is not supported - No OpenLS Presentation Service is available");
					ServiceError se = new ServiceError(SeverityType.ERROR);
					se.addError(ErrorCodeType.NOT_SUPPORTED,
							"OutputType",
							"OutputType in e.g. RouteMapRequest is not supported - No OpenLS Presentation Service is available");
					throw se;
				}
				//Send Request to OpenLS Presentation Service
					WebServiceConnector webserConn = new WebServiceConnector();
					XLSDocument xlsResponse = webserConn.connect(mRSConfigurator.getOpenLSPresentationServicePath(), xlsRequest.toString());
	
				//Read the XLSDoc (Response)
				XLSType xlsTypeResponse = xlsResponse.getXLS();
				AbstractBodyType abBodyResponse[] = xlsTypeResponse.getBodyArray();
				ResponseType response = (ResponseType) abBodyResponse[0].changeType(ResponseType.type);
					
				if(response.isSetErrorList()){
					//ERROR
					ErrorListType errorlist = response.getErrorList();
					ErrorType error[] = errorlist.getErrorArray();
					
					mLogger.error("Problem with the OpenLS Presentation Service, Message: "+error[0].toString());
					ServiceError se = new ServiceError(SeverityType.ERROR);
					se.addError(ErrorCodeType.UNKNOWN,
							"Problem with OpenLS Presentation Service",
							"Problem with the OpenLS Presentation Service, Message: "+error[0].toString());
					throw se;
				}
				else if(response.isSetResponseParameters()){
					//PortrayMapResponse
					AbstractResponseParametersType respParam = response.getResponseParameters();
					PortrayMapResponseType portrayResp = (PortrayMapResponseType) respParam.changeType(PortrayMapResponseType.type);
					MapType map[] = portrayResp.getMapArray();
						
					if(map[0].getContent().isSetURL())
						sMapURL = map[0].getContent().getURL();
					if(map[0].getContent().isSetData())
						sData = map[0].getContent().getData();
				}
			}
			///////////////////////////////////////
			//*** Create KML
			else if(sRouteMapFormat.equalsIgnoreCase("kml")){
				RouteToKML.createKML(
						mRSConfigurator.getTempPath(), routeResult.getRouteRequestID()+"_"+i+".kml", 
						"Route_"+routeResult.getRouteRequestID(), 
						routeResult.getFeatCollRoute(), RouteService.GRAPH_SRS);
				sMapURL = mRSConfigurator.getWWWPath()+"/"+routeResult.getRouteRequestID()+"_"+i+".kml";
			}
			///////////////////////////////////////
			//*** Create SLD
			else if(sRouteMapFormat.equalsIgnoreCase("sld")){
				
				ArrayList<LayerAndStyle> arraylistLayerNameAndStyle = new ArrayList<LayerAndStyle>();
				
				RouteGeometryType routegeom = RouteGeometryType.Factory.newInstance();
				routegeom.setLineString(CreateXLSRequest.createLineStringType(RouteService.GRAPH_SRS, routeResult.getFeatCollRoute()));
				ArrayList<OverlayADT> arraylistOverlayADT = new ArrayList<OverlayADT>();
				arraylistOverlayADT.add(new OverlayADT(0, routegeom, null));
				
				DirectPositionType directPosEnv[] = envelopeBBox.getPosArray();
				Coordinate cBBoxMin = Pos.getCoord(directPosEnv[0].getStringValue());
				Coordinate cBBoxMax = Pos.getCoord(directPosEnv[1].getStringValue());
				
				SLD.createSLD(
						arraylistLayerNameAndStyle, arraylistOverlayADT,
						boolRouteMapTransparent, sRouteMapBGcolor,
						RouteService.GRAPH_SRS, cBBoxMin.x, cBBoxMin.y, cBBoxMax.x, cBBoxMax.y,
						sRouteMapHeight, sRouteMapWidth,
						mRSConfigurator.getTempPath(), routeResult.getRouteRequestID()+"_"+i+".sld");
				
				sMapURL = mRSConfigurator.getWWWPath()+"/"+routeResult.getRouteRequestID()+"_"+i+".sld";
			}
			///////////////////////////////////////
			//*** Create SLD 3D
			else if(sRouteMapFormat.equalsIgnoreCase("sld3d")){
				
				ArrayList<String> arraylistLayerNames = new ArrayList<String>();
				
				DirectPositionType directPosEnv[] = envelopeBBox.getPosArray();
				Coordinate cBBoxMin = Pos.getCoord(directPosEnv[0].getStringValue());
				Coordinate cBBoxMax = Pos.getCoord(directPosEnv[1].getStringValue());
				
				SLD3D.createSLD3D(routeResult.getResponseSRS(), routeResult.getFeatCollRoute(), routeResult.getFeatCollSRS(),
						cBBoxMin.x, cBBoxMin.y, cBBoxMax.x, cBBoxMax.y,
						arraylistLayerNames,
						mRSConfigurator.getTempPath(), routeResult.getRouteRequestID()+"_"+i+".sld3d");
				
				sMapURL = mRSConfigurator.getWWWPath()+"/"+routeResult.getRouteRequestID()+"_"+i+".sld3d";
			}
			else{
				ServiceError se = new ServiceError(SeverityType.ERROR);
				se.addError(ErrorCodeType.OTHER_XML,
                                 RouteMapRequestParameter.format.toString(),
                                 "The required value of the mandatory parameter '" + RouteMapRequestParameter.format.toString() 
                                 + "' must be 'png, jpeg or gif or kml,sld'. Delivered value was: " + sRouteMapFormat);
				throw se;
			}
			
			///////////////////////////////////////
			//*** Create new RouteMapResponse Type ***
			routemapType[i] = RouteMapType.Factory.newInstance();
			
			routemapType[i].setDescription(sRouteMapStyle);

			//*** Add CenterContext ***
			CenterContextType centercontentType = routemapType[i].addNewCenterContext();
			centercontentType.setSRS(routeResult.getResponseSRS());
			PointType pt = centercontentType.addNewCenterPoint();
			DirectPositionType dp = pt.addNewPos();
			DirectPositionType directPosEnv[] = envelopeBBox.getPosArray();

			Coordinate cBBoxMin = Pos.getCoord(directPosEnv[0].getStringValue());
			Coordinate cBBoxMax = Pos.getCoord(directPosEnv[1].getStringValue());

			dp.setStringValue((cBBoxMin.x+cBBoxMax.x)/2 +" "+(cBBoxMin.y+cBBoxMax.y)/2);
			//Choice
				//centercontentType.setDisplayScale(new BigInteger(""));
				//centercontentType.setDPI(new BigInteger(""));
			//------
				RadiusType radius = centercontentType.addNewRadius();
				double dRadius = routeResult.getEnvelopeRoute().getRadiusInMeters();
				//Set Parameter for change the Length in the requested Unit
				double dUnitParameter = DistanceUnit.getUnitParameter(routeResult.getDistanceUnit());
				dRadius = dRadius * dUnitParameter;
				radius.setStringValue(Double.toString(dRadius));
				radius.setUnit(routeResult.getDistanceUnit());
			//

			//*** Add Content ***
			ContentType contentType = routemapType[i].addNewContent();
			contentType.setFormat(sRouteMapFormat);
			contentType.setHeight(new BigInteger(sRouteMapHeight));
			contentType.setWidth(new BigInteger(sRouteMapWidth));
				if(sMapURL != null)
					contentType.setURL(sMapURL);
				else if(sData != null)
					contentType.setData("");
		}
		
		return routemapType; */
	}

    /** TODO - must be overwork !! **/
    private EnvelopeType createBBox(String sSRS, EnvelopeType envelope)throws ServiceError{
    	
    	DirectPositionType directPosEnv[] = envelope.getPosArray();
		Coordinate cBBoxMin = Pos.getCoord(directPosEnv[0].getStringValue());
		Coordinate cBBoxMax = Pos.getCoord(directPosEnv[1].getStringValue());

		double dXLower = cBBoxMin.x; double dYLower = cBBoxMin.y;
		double dXUpper = cBBoxMax.x; double dYUpper = cBBoxMax.y;
		double dXTMP = Math.abs( dXUpper - dXLower );
		double dYTMP = Math.abs( dYUpper - dYLower );

		if(dXTMP > dYTMP){
			double dTMP = (dXTMP - dYTMP) / 2;
			if(dYLower < dYUpper){
				dYLower = dYLower - dTMP; dYUpper = dYUpper + dTMP;
			}
			else{
				dYLower = dYLower + dTMP; dYUpper = dYUpper - dTMP;
			}
		}else if(dYTMP > dXTMP){
			double dTMP = (dYTMP - dXTMP) / 2;
			if(dXLower < dXUpper){
				dXLower = dXLower - dTMP; dXUpper = dXUpper + dTMP;
			}
			else{
				dXLower = dXLower + dTMP; dXUpper = dXUpper - dTMP;
			}
		}
			
		cBBoxMin.x = dXLower; cBBoxMin.y = dYLower;
		cBBoxMax.x = dXUpper; cBBoxMax.y = dYUpper;
		
		return new Envelope(sSRS, new Coordinate(cBBoxMin.x-100, cBBoxMin.y-100),
				new Coordinate(cBBoxMax.x+100, cBBoxMax.y+100)).getEnvelopeType();
    }
}
