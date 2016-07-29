
package org.freeopenls.presentationservice.documents;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.freeopenls.constants.RouteService;
import org.freeopenls.constants.OpenLS.RequestParameter;
import org.freeopenls.error.ErrorTypes;
import org.freeopenls.error.ServiceError;
import org.freeopenls.gml.Pos;
import org.freeopenls.presentationservice.PSConfigurator;
import org.freeopenls.tools.CoordTransform;
import org.freeopenls.wms.getcapabilities.GetCapabilitiesWMS;
import org.freeopenls.wms.getmap.GetMapWMS;

import com.vividsolutions.jts.geom.Coordinate;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.PointType;
import net.opengis.xls.AbstractRequestParametersType;
import net.opengis.xls.AvailableFormatsType;
import net.opengis.xls.AvailableLayersType;
import net.opengis.xls.AvailableSRSType;
import net.opengis.xls.AvailableStylesType;
import net.opengis.xls.ContentType;
import net.opengis.xls.DistanceUnitType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.GetPortrayMapCapabilitiesRequestType;
import net.opengis.xls.GetPortrayMapCapabilitiesResponseType;
import net.opengis.xls.LayerType;
import net.opengis.xls.MapType;
import net.opengis.xls.OutputType;
import net.opengis.xls.OverlayType;
import net.opengis.xls.PortrayMapRequestType;
import net.opengis.xls.PortrayMapResponseType;
import net.opengis.xls.PresentationContentType;
import net.opengis.xls.RequestType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.StyleType;
import net.opengis.xls.LayerType.Layer;


/**
 * <p><b>Title: FileDelete</b></p>
 * <p><b>Description:</b> Class for FileDelete<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-05-24
 */
public class RequestXLSDocument {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger.getLogger(RequestXLSDocument.class.getName());
	private static final Logger mLoggerCounter = Logger.getLogger(RequestXLSDocument.class.getName()+".Counter");
	/** PresentationConfigurator Instance */
	private PSConfigurator mPSConfigurator;
	/** Response Document */
	private ResponseXLSDocument mResponseXLSDocument;
	/** SRSName in which the Response should be **/
	private String mResponseSRS = "";

	/**
	 * Constructor
	 * 
	 * @param sessionID
	 */
	public RequestXLSDocument(String sessionID, String responseSRS){
		mResponseSRS = responseSRS;
		mPSConfigurator = PSConfigurator.getInstance();
		mResponseXLSDocument = new ResponseXLSDocument(sessionID);
	}

	/**
	 * Method that add a request to the response
	 * 
	 * @param requestType
	 * @throws ServiceError
	 * @throws Exception
	 */
	public void addRequest(RequestType requestType)throws ServiceError, Exception{
        ///////////////////////////////////////
		//*** GetRequestParameters ***
		//Parameter RequestID is Mandatory
		if (requestType.getRequestID().equalsIgnoreCase(""))
        	throw ErrorTypes.parameterMissing(RequestParameter.requestID.toString());
		//Parameter Version is Mandatory
		if (requestType.getVersion().equalsIgnoreCase(""))
			throw ErrorTypes.parameterMissing(RequestParameter.version.toString());
		//Parameter Version is Mandatory
		if (!requestType.getVersion().equalsIgnoreCase(RouteService.SERVICE_VERSION))
			throw ErrorTypes.parameterMissing(RequestParameter.version.toString());
	
        ///////////////////////////////////////
		// *** Check What Request it is ***
		AbstractRequestParametersType abreqparType = requestType.getRequestParameters();
		
		///////////////////////////////////////
		//*** GetPortrayMapCapabilitiesRequestType ***
		if(abreqparType instanceof GetPortrayMapCapabilitiesRequestType){
			if (requestType.getMethodName().equalsIgnoreCase("GetPortrayMapCapabilitiesRequest")) {
				///////////////////////////////////////
				//*** Create PortrayMapCapabilitiesResponse ***
				mResponseXLSDocument.createResponse(requestType.getRequestID(), requestType.getVersion(), new BigInteger("1"));
				GetPortrayMapCapabilitiesResponseType getportrayresp = mResponseXLSDocument.addResponseParametersMapCapabilities();

				///////////////////////////////////////
				//*** Add Capabilities (GetCapabilities) ***
				GetCapabilitiesWMS getcapaWMS = new GetCapabilitiesWMS();
				getcapaWMS.doGet(this.mPSConfigurator.getWMSPath());

					//* Add SRS *
					AvailableSRSType srs = getportrayresp.addNewAvailableSRS();
					for(int i=0 ; i<getcapaWMS.getSRSs().size() ; i++){
						srs.addSRS(getcapaWMS.getSRSs().get(i));
					}
	
					//* Add Layers *
					AvailableLayersType layers = getportrayresp.addNewAvailableLayers();
					for(int i=0 ; i<getcapaWMS.getLayers().size() ; i++){
						layers.addLayer(getcapaWMS.getLayers().get(i));
					}
	
					//* Add Formats *
					AvailableFormatsType formats = getportrayresp.addNewAvailableFormats();
					for(int i=0 ; i<getcapaWMS.getFormats().size() ; i++){
						formats.addFormat(getcapaWMS.getFormats().get(i));
					}
	
					//* Add Styles *
					AvailableStylesType styles = getportrayresp.addNewAvailableStyles();
					HashSet<String> hsStylesTMP = new HashSet<String>();
					for(int i=0 ; i<getcapaWMS.getStyles().size() ; i++){
						if(!hsStylesTMP.contains(getcapaWMS.getStyles().get(i))){
							hsStylesTMP.add(getcapaWMS.getStyles().get(i));
						}
					}
					Object arrayStyles[] =  hsStylesTMP.toArray();
					for(int i=0 ; i<arrayStyles.length ; i++){
						styles.addStyle((String)arrayStyles[i]);
					}
					mResponseXLSDocument.doWellFormedGetPortrayMapCapabilitiesResponse();
					mLoggerCounter.info(" ; ; ; ; GetPortrayMapCapabilitiesResponse");
			}
			else{
				ServiceError se = new ServiceError(SeverityType.ERROR);
	            se.addError(ErrorCodeType.OTHER_XML, "RequestParameters",
	                                 "The Parameters of the Request are unknown, MethodName='"+requestType.getMethodName()+
	            					 "'! Possible are 'GetPortrayMapCapabilitiesRequest' and 'PortrayMapRequest'!");
	            throw se;
			}
		}
		
		///////////////////////////////////////
		//*** PortrayMapRequestType ***
		else if(abreqparType instanceof PortrayMapRequestType){
			if (requestType.getMethodName().equalsIgnoreCase("PortrayMapRequest")) {
				
				//*** Get Attributes and Elements ***
				PortrayMapRequestType portraymapreq = (PortrayMapRequestType) abreqparType.changeType(PortrayMapRequestType.type);

				//*** Basemap ***
				//Get LayerName and LayerStyle
				ArrayList<LayerAndStyle> alLayerNameAndStyle = new ArrayList<LayerAndStyle>();
				LayerType.Filter.Enum sBasemapLayerFilter = LayerType.Filter.INCLUDE;
				if(portraymapreq.isSetBasemap()){
					Layer[] layerArray = portraymapreq.getBasemap().getLayerArray();
					
					//TODO Check Filter ; only Include is supported
					sBasemapLayerFilter = portraymapreq.getBasemap().getFilter();
					if(sBasemapLayerFilter.equals(LayerType.Filter.EXCLUDE)){
						ServiceError se = new ServiceError(SeverityType.ERROR);
			            se.addError(ErrorCodeType.NOT_SUPPORTED,
			                                 "Layer Filter",
			                                 "The Value of the Layer Filter Attribute 'EXCLUDE' is not supported!");
			            throw se;
					}
					
					for(int i=0 ; i<layerArray.length ; i++){
						String sTMP = layerArray[i].getName();
						
						if(layerArray[i].isSetStyle())
							alLayerNameAndStyle.add(new LayerAndStyle(sTMP, layerArray[i].getStyle()));
						else
							alLayerNameAndStyle.add(new LayerAndStyle(sTMP, null));
					}
				}
				
				//*** Overlay ***
				OverlayType[] overlay = portraymapreq.getOverlayArray();
				ArrayList<OverlayADT> alOverlayADT = new ArrayList<OverlayADT>();
				for(int i=0 ; i<overlay.length ; i++){
					int iZOrder = 0;
					if(overlay[i].isSetZorder()) iZOrder = overlay[i].getZorder().intValue();
					else iZOrder = alOverlayADT.size() + 1;

					Object obj = new Object();
					StyleType style = null;
					
					if(overlay[i].isSetPOI()) 
						obj = overlay[i].getPOI();
					if(overlay[i].isSetRouteGeometry())
						obj = overlay[i].getRouteGeometry();
					if(overlay[i].isSetPosition()) 
						obj = overlay[i].getPosition();
					if(overlay[i].isSetMap()) 
						obj = overlay[i].getMap();
					if(overlay[i].isSetStyle()) 
						style = overlay[i].getStyle();
					
					alOverlayADT.add(new OverlayADT(iZOrder, obj, style));
				}
				
				///////////////////////////////////////
				//*** Create PortrayMapCapabilitiesResponse ***
				mResponseXLSDocument.createResponse(requestType.getRequestID(), requestType.getVersion(), new BigInteger("1"));
				PortrayMapResponseType portraymapresp = mResponseXLSDocument.addResponseParametersPortrayMap();				
				
				//*** Output ***
				OutputType[] output = portraymapreq.getOutputArray();
				for(int i=0 ; i<output.length ; i++){
					String sWidth = "400"; String sHeight = "400"; String sFormat = "png";
					String sBGColor = ""; boolean boolTransparent = false; PresentationContentType.Enum content = PresentationContentType.DATA;
					double dXLower=0, dYLower=0, dXUpper=0, dYUpper=0;
					String sSRS="4326";
					String sContent = PresentationContentType.URL.toString();
					
					if(output[i].isSetWidth()) sWidth = ""+output[i].getWidth();
					if(output[i].isSetHeight()) sHeight = ""+output[i].getHeight();
					if(output[i].isSetFormat()) sFormat = output[i].getFormat();
					if(output[i].isSetBGcolor()) sBGColor = output[i].getBGcolor();
					if(output[i].isSetTransparent()) boolTransparent = output[i].getTransparent();
					if(output[i].isSetContent()){
						content = output[i].getContent();
						sContent = content.toString();
					}

					if(output[i].isSetBBoxContext()){
						EnvelopeType env = output[i].getBBoxContext();
						sSRS = env.getSrsName();
						DirectPositionType[] directpos = env.getPosArray();
						Coordinate cTMP = Pos.getCoord(directpos[0].getStringValue());
						dXLower = cTMP.x; dYLower = cTMP.y;
						cTMP = Pos.getCoord(directpos[1].getStringValue());
						dXUpper = cTMP.x; dYUpper = cTMP.y;
					}
					if(output[i].isSetCenterContext()){
						PointType point = output[i].getCenterContext().getCenterPoint();
						sSRS = point.getSrsName();
						Coordinate cTMP = Pos.getCoord(point.getPos().getStringValue());
						if(!output[i].getCenterContext().isSetRadius()){
							//TODO : ALLES ANDERE WIRD NETTTT SUPPORTED!!!!
						}
						double dRadius = Double.parseDouble(output[i].getCenterContext().getRadius().getStringValue());
						DistanceUnitType.Enum unit = output[i].getCenterContext().getRadius().getUnit();

						dRadius = dRadius * DistanceUnit.getUnitParameter(unit);

						dXLower = cTMP.x - dRadius; dYLower = cTMP.y - dRadius;
						dXUpper = cTMP.x + dRadius; dYUpper = cTMP.y + dRadius;
					}

					///////////////////////////////////////
					//*** GetMap WMS ***
					GetMapWMS mapWMS = new GetMapWMS(mPSConfigurator, requestType.getRequestID()
							+"_"+i+"_"+System.currentTimeMillis(), CoordTransform.getEPSGCodeNumber(sSRS), 
							dXLower, dYLower, dXUpper, dYUpper, 
							boolTransparent, sBGColor, sHeight, sWidth, sFormat, alLayerNameAndStyle, alOverlayADT);

					///////////////////////////////////////
					//*** Create Response ***
					//Add new Map
					MapType mapResp = portraymapresp.addNewMap();
					ContentType contentResp = mapResp.addNewContent();
					if(sContent.equals(PresentationContentType.URL.toString())){
						contentResp.setURL(mapWMS.getMapURL());	
					}else{
						contentResp.setData(mapWMS.getMapbase64decoded());	
					}
					contentResp.setFormat(sFormat);
					contentResp.setWidth(new BigInteger(sWidth));
					contentResp.setHeight(new BigInteger(sHeight));
					
					EnvelopeType env = mapResp.addNewBBoxContext();
					if(!mResponseSRS.equals(sSRS)){
						
					}
					env.setSrsName(sSRS);
					env.addNewPos().setStringValue(dXLower+" "+dYLower);
					env.addNewPos().setStringValue(dXUpper+" "+dYUpper);
				}
				
				mResponseXLSDocument.doWellFormedPortrayMapResponse();
				mLoggerCounter.info(" ; ; ; ; PortrayMap");
			}
			else{
				ServiceError se = new ServiceError(SeverityType.ERROR);
	            se.addError(ErrorCodeType.OTHER_XML,
	                                 "RequestParameters",
	                                 "The Parameters of the Request are unknown, MethodName='"+requestType.getMethodName()+
	            					 "'! Possible are 'GetPortrayMapCapabilitiesRequest' and 'PortrayMapRequest'!");
	            throw se;
			}
		}
		//*** UNKNOWN ***
		else{
	       	ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.OTHER_XML, "RequestParameters",
                                 "The Parameters of the Request are unknown! MethodName='"+requestType.getMethodName()+"' ?!?");
            mLogger.error(se.getMessages());
            throw se;
		}
	}

	/**
	 * Method that returns response XLSDocument
	 * @return XLSDocument
	 */
	public ResponseXLSDocument getResponseXLSDocument() {
		return mResponseXLSDocument;
	}

//------------------------------------------------------------------------
	///////////////////////////////////////
	//*** Class LayerAndStyle ***
	public static class LayerAndStyle {
		private String m_sLayerName = ""; private StyleType m_styleType = null;
		public LayerAndStyle(String sLayerName, StyleType styleType){m_sLayerName=sLayerName; m_styleType=styleType;}
		public String getLayerName(){return m_sLayerName;}
		public StyleType getStyleType(){return m_styleType;}
	}
	///////////////////////////////////////
	//*** Class OverlayADT ***
	public static class OverlayADT {
		private int m_iIndex=0; private Object m_obj=null; private StyleType m_styleTyp=null; 
		public OverlayADT(int iIndex, Object obj, StyleType style){m_iIndex=iIndex; m_obj=obj; m_styleTyp=style;}
		public int getIndex(){return m_iIndex;}
		public Object getObject(){return m_obj;}
		public StyleType getStyle(){return m_styleTyp;}
	}
//------------------------------------------------------------------------
}
