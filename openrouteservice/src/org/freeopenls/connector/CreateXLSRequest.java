
package org.freeopenls.connector;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.gml.AbstractRingPropertyType;
import net.opengis.gml.AbstractRingType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.LineStringType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.PointType;
import net.opengis.gml.PolygonType;
import net.opengis.xls.AbstractBodyType;
import net.opengis.xls.AbstractHeaderType;
import net.opengis.xls.AbstractPOISelectionCriteriaType;
import net.opengis.xls.AbstractRequestParametersType;
import net.opengis.xls.AddressType;
import net.opengis.xls.DirectoryRequestType;
import net.opengis.xls.GeocodeRequestType;
import net.opengis.xls.LayerType;
import net.opengis.xls.OutputType;
import net.opengis.xls.OverlayType;
import net.opengis.xls.POIPropertyNameType;
import net.opengis.xls.POIPropertyType;
import net.opengis.xls.PortrayMapRequestType;
import net.opengis.xls.PositionType;
import net.opengis.xls.RequestHeaderType;
import net.opengis.xls.RequestType;
import net.opengis.xls.RouteGeometryType;
import net.opengis.xls.SortDirectionType;
import net.opengis.xls.StyleType;
import net.opengis.xls.XLSDocument;
import net.opengis.xls.XLSType;
import net.opengis.xls.LayerType.Layer;
import net.opengis.xls.POIPropertiesDocument.POIProperties;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;


/**
 * <p><b>Title: CreateXLSRequest</b></p>
 * <p><b>Description:</b> Class for create a XLS Request<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-07-24
 * @version 1.1 2008-04-21
 */
public class CreateXLSRequest {
	/** XLSDocument */
	private XLSDocument mXLSDoc;
	/** XLSType */
	private XLSType mXLSType;
	/** Version No. */
	private String mVersion = "";
	/** Session ID */
	private String mSessionID = "";
	
	/**
	 * Constructor - Create XLS Doc and Header
	 *
	 */
	public CreateXLSRequest(String sVersion, String sSessionID, String sResponseSRS){
		mVersion = sVersion;
		mSessionID = sSessionID;
		
		///////////////////////////////////////
		//*** Create XLS Document **
		mXLSDoc = XLSDocument.Factory.newInstance();
		mXLSType = mXLSDoc.addNewXLS();
		mXLSType.setVersion(new BigDecimal(mVersion));

		//*** XLS Header ***
		AbstractHeaderType abHeader = mXLSType.addNewHeader();
		RequestHeaderType reqHeader = (RequestHeaderType) abHeader.changeType(RequestHeaderType.type);
		reqHeader.setSessionID(mSessionID);
		reqHeader.setSrsName(sResponseSRS);
			//For well formed XML-Doc
				XmlCursor cursor01 = mXLSType.newCursor();
				if (cursor01.toChild(new QName("http://www.opengis.net/xls", "_Header"))) {
					cursor01.setName(new QName("http://www.opengis.net/xls","RequestHeader"));
				}
				cursor01.dispose();
			//------
	}

	/**
	 * Method that create a GeocodeRequest-Body (OpenLS Location Utility Service)
	 * 
	 * @param addressType
	 * @param sRequestID
	 * @return XLSDocument
	 */
	public XLSDocument createGeocodeRequest(AddressType addressType, String sRequestID){
		//*** XLS Body ***
		AbstractBodyType abBody = mXLSType.addNewBody();
		RequestType requestType = (RequestType) abBody.changeType(RequestType.type);
		requestType.setMethodName("GeocodeRequest");	//Set MethodName
		requestType.setRequestID(sRequestID);			//Set RequesttID
		requestType.setVersion(mVersion);				//Set Version = 1.1

		//*** GeocodeRequest ***
		AbstractRequestParametersType abrequestParam = requestType.addNewRequestParameters();
		GeocodeRequestType geocodeRequest = (GeocodeRequestType) abrequestParam.changeType(GeocodeRequestType.type);

		AddressType address = geocodeRequest.addNewAddress();
		address.set(addressType);

		//do well formed
		wellformedXLSDoc(requestType, "GeocodeRequest");

		return mXLSDoc;
	}

	/**
	 * Method that create a DirectoryRequest-Body (OpenLS Directory Service)
	 * 
	 * @param sPOIName
	 * @param sPhoneNumber
	 * @param sNameList
	 * @param sValueList
	 * @param sRequestID
	 * @return XLSDocument
	 */
	public XLSDocument createDirectoryRequest(String sPOIName, String sPhoneNumber, ArrayList<String> sNameList, ArrayList<String> sValueList, String sRequestID){
		//*** XLS Body ***
		AbstractBodyType abBody = mXLSType.addNewBody();
		RequestType requestType = (RequestType) abBody.changeType(RequestType.type);
		requestType.setMethodName("DirectoryRequest");	//Set MethodName
		requestType.setRequestID(sRequestID);			//Set RequesttID
		requestType.setVersion(mVersion);				//Set Version = 1.1

		//*** DirectoryRequest ***
		AbstractRequestParametersType abrequestParam = requestType.addNewRequestParameters();
		DirectoryRequestType directoryRequest = (DirectoryRequestType) abrequestParam.changeType(DirectoryRequestType.type);
		
		AbstractPOISelectionCriteriaType abpoiselec = directoryRequest.addNewPOISelectionCriteria();
		POIProperties poiprops = (POIProperties) abpoiselec.changeType(POIProperties.type);
		
		ArrayList<POIPropertyType> alPOIProps = new ArrayList<POIPropertyType>();
		
		if(sPOIName != null){
			POIPropertyType pTMP = POIPropertyType.Factory.newInstance();
			pTMP.setName(POIPropertyNameType.POI_NAME);
			pTMP.setValue(sPOIName);
			alPOIProps.add(pTMP);
		}
		if(sPhoneNumber != null){
			POIPropertyType pTMP = POIPropertyType.Factory.newInstance();
			pTMP.setName(POIPropertyNameType.PHONE_NUMBER);
			pTMP.setValue(sPhoneNumber);
			alPOIProps.add(pTMP);
		}
		
		for(int i=0 ; i<sNameList.size() ; i++){
			POIPropertyType pTMP = POIPropertyType.Factory.newInstance();
			pTMP.setName(POIPropertyNameType.OTHER);
			pTMP.setValue(sValueList.get(i));
			alPOIProps.add(pTMP);
		}

		//Set POIProperty's
		poiprops.setPOIPropertyArray((XmlObject[])alPOIProps.toArray());
		directoryRequest.setSortDirection(SortDirectionType.ASCENDING);
		
		//do well formed
		wellformedXLSDoc(requestType, "DirectoryRequest");

		return mXLSDoc;
	}

	/**
	 * Mehtod that create PortrayMapRequest-Body (OpenLS Presentation Service)
	 * 
	 * @param env
	 * @param sRouteMapFormat
	 * @param sRouteMapHeight
	 * @param sRouteMapWidth
	 * @param sRouteMapBGcolor
	 * @param boolRouteMapTransparent
	 * @param sFeatureSRS
	 * @param featcollRoute
	 * @param Layers
	 * @param Styles
	 * @param sRequestID
	 * @return XLSDocument
	 */
	public XLSDocument createPortrayMapRequest(EnvelopeType env, String sRouteMapFormat, String sRouteMapHeight, 
			String sRouteMapWidth, String sRouteMapBGcolor, boolean boolRouteMapTransparent, 
			String sFeatureSRS, FeatureCollection featcollRoute, ArrayList<Polygon> AvoidAreas, 
			ArrayList<LineString> AvoidAddresses, ArrayList<String> Layers, 
			ArrayList<String> Styles, String sRequestID){

		//*** XLS Body ***
		AbstractBodyType abBody = mXLSType.addNewBody();
		RequestType requestType = (RequestType) abBody.changeType(RequestType.type);
		requestType.setMethodName("PortrayMapRequest");	//Set MethodName
		requestType.setRequestID(sRequestID);			//Set RequesttID
		requestType.setVersion(mVersion);				//Set Version = 1.1

		//*** PortrayMapRequest ***
		AbstractRequestParametersType abrequestParam = requestType.addNewRequestParameters();
		PortrayMapRequestType portraymapRequest = (PortrayMapRequestType) abrequestParam.changeType(PortrayMapRequestType.type);

		//OutputType
		OutputType output = portraymapRequest.addNewOutput();
		output.setBBoxContext(env);
		output.setFormat(sRouteMapFormat);
		output.setHeight(new BigInteger(sRouteMapHeight));
		output.setWidth(new BigInteger(sRouteMapWidth));
		output.setBGcolor(sRouteMapBGcolor);
		output.setTransparent(boolRouteMapTransparent);
		
		//BaseMap
		LayerType layer = portraymapRequest.addNewBasemap();
		layer.setFilter(LayerType.Filter.INCLUDE);
		for(int j=0 ; j < Layers.size() ; j++){
			Layer layerTMP = layer.addNewLayer();
			layerTMP.setName(Layers.get(j));
			StyleType styleTMP = layerTMP.addNewStyle();
			
			if(j<Styles.size())
				styleTMP.setName(Styles.get(j));
			else
				styleTMP.setName("");
		}
		
		//Overlay
		// addRoute
		OverlayType overlayRoute = portraymapRequest.addNewOverlay();
		RouteGeometryType routegeom = overlayRoute.addNewRouteGeometry();
		routegeom.setLineString(createLineStringType(sFeatureSRS, featcollRoute));
		// addAvoidAreas
		if(AvoidAreas.size() > 0){
			for(int i=0 ; i<AvoidAreas.size() ; i++){
				OverlayType overlayAvoidAreas = portraymapRequest.addNewOverlay();
				PositionType position = overlayAvoidAreas.addNewPosition();
				position.setPoint(createPoinType(sFeatureSRS, AvoidAreas.get(i).getCentroid()));
				position.setPolygon(createPolygonType(sFeatureSRS, AvoidAreas.get(i)));				
			}
		}
		// add AvoidAddresses
		if(AvoidAddresses.size() > 0){
			for(int i=0 ; i<AvoidAddresses.size() ; i++){
				OverlayType overlayAvoidAddresses = portraymapRequest.addNewOverlay();
				PositionType position = overlayAvoidAddresses.addNewPosition();
				position.setPoint(createPoinType(sFeatureSRS, AvoidAddresses.get(i).getCentroid()));
				LineString ls = AvoidAddresses.get(i);
				Geometry g = ls.buffer(3);
				position.setPolygon(createPolygonType(sFeatureSRS, (Polygon)g));				
			}
		}
		
		//do well formed
		wellformedXLSDoc(requestType, "PortrayMapRequest");

		return mXLSDoc;
	}

	private void wellformedXLSDoc(RequestType requestType, String sRequestName){
		//For well formed XLS-Document
		XmlCursor cursorXLS = mXLSType.newCursor();
		XmlCursor cursorRequest = requestType.newCursor();
		if (cursorXLS.toChild(new QName("http://www.opengis.net/xls", "_Body")))
			cursorXLS.setName(new QName("http://www.opengis.net/xls","Request"));
		if (cursorRequest.toChild(new QName("http://www.opengis.net/xls", "_RequestParameters")))
			cursorRequest.setName(new QName("http://www.opengis.net/xls",sRequestName));
		cursorXLS.dispose();
		cursorRequest.dispose();
	}

	/**
	 * Method that create LineStringType
	 * 
	 * @param sGraphSRS
	 * @param featcoll
	 * @return LineStringType
	 */
	public static LineStringType createLineStringType(String sGraphSRS, FeatureCollection featcoll){
		LineStringType lsType = LineStringType.Factory.newInstance();
		lsType.setSrsName(sGraphSRS);
		
		List<Feature> l = featcoll.getFeatures();
		
		Feature firstfeat = l.get(0);
		LineString firstlineTMP = (LineString) firstfeat.getGeometry();
		Coordinate cFirst[] = firstlineTMP.getCoordinates();
		DirectPositionType directFirst = lsType.addNewPos();
		directFirst.setStringValue(cFirst[0].x+" "+cFirst[0].y);
				
		for (int i = 0; i < l.size(); i++) {
			Feature feat2 = l.get(i);
			LineString lineTMP = (LineString) feat2.getGeometry();
			Coordinate c[] = lineTMP.getCoordinates();
				
			for (int j = 1; j < c.length; j++) {
				DirectPositionType direct = lsType.addNewPos();
				direct.setStringValue(c[j].x+" "+c[j].y);
			}
		}
				
		return lsType;
	}
	
	/**
	 * Method that create LineStringType
	 * 
	 * @param sGraphSRS
	 * @param poly
	 * @return PolygonType
	 */
	public static PolygonType createPolygonType(String sGraphSRS, Polygon poly){
		PolygonType polyType = PolygonType.Factory.newInstance();
		polyType.setSrsName(sGraphSRS);
		
		AbstractRingPropertyType abringprop = polyType.addNewExterior();
		AbstractRingType abring = abringprop.addNewRing();
		LinearRingType lring = (LinearRingType) abring.changeType(LinearRingType.type);
		
		lring.setSrsName(sGraphSRS);
		Coordinate c[] = poly.getCoordinates();

		for (int i = 0; i < c.length; i++) {
				DirectPositionType direct = lring.addNewPos();
				direct.setStringValue(c[i].x+" "+c[i].y);
		}
		
		//For well formed XLS-Document
		XmlCursor cursor = abringprop.newCursor();
		if (cursor.toChild(new QName("http://www.opengis.net/gml", "_Ring")))
			cursor.setName(new QName("http://www.opengis.net/gml","LinearRing"));
		cursor.dispose();
			
		return polyType;
	}

	/**
	 * Method that create PointType
	 * 
	 * @param featureSRS
	 * @param p
	 * @return PointType
	 */
	public static PointType createPoinType(String featureSRS, Point p){
		PointType point = PointType.Factory.newInstance();
		point.setSrsName(featureSRS);
		DirectPositionType dp = point.addNewPos();
		dp.setStringValue(p.getCoordinate().x+" "+p.getCoordinate().y);
		
		return point;
	}
}
