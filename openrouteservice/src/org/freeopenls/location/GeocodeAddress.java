

package org.freeopenls.location;

import org.apache.log4j.Logger;
import org.freeopenls.connector.CreateXLSRequest;
import org.freeopenls.connector.WebServiceConnector;
import org.freeopenls.constants.RouteService;
import org.freeopenls.error.ServiceError;
import org.freeopenls.gml.Pos;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.PointType;
import net.opengis.xls.AbstractBodyType;
import net.opengis.xls.AbstractResponseParametersType;
import net.opengis.xls.AddressType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.ErrorListType;
import net.opengis.xls.ErrorType;
import net.opengis.xls.GeocodeResponseListType;
import net.opengis.xls.GeocodeResponseType;
import net.opengis.xls.GeocodedAddressType;
import net.opengis.xls.ResponseType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.XLSDocument;
import net.opengis.xls.XLSType;



/**
 * Class for read the xls:Address element and send request to OpenLS Location Utility Service<br>
 * 
 * Copyright: Copyright (c) 2008 by Pascal Neis
 * 
 * @author Pascal Neis, i3mainz, neis@geographie.uni-bonn.de
 * @version 1.0 2007-07-24
 */
public class GeocodeAddress{
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger log = Logger.getLogger(GeocodeAddress.class.getName());
	/** Coordinate of the Position/Address */
	private WayPoint mPosition = null;
	/** OpenLS Location Utility Service Path */
	private String mOpenLSLocationUtilityServicePath = null;

	/**
	 * Constructor
	 *
	 */
	public GeocodeAddress(String openLSLocationUtilityServicePath){
		mOpenLSLocationUtilityServicePath = openLSLocationUtilityServicePath;
	}

	
	/**
	 * Method that geocode the AddressType with a OpenLS Location Utility Service.<br>
	 * 
	 * @param addressType
	 * 			AddressType of the requested Address
	 * @throws ServiceError
	 */
	public WayPoint geocode(AddressType addressType)throws ServiceError{
	
		///////////////////////////////////////
		//*** Create XLS Geocode Request ***
		CreateXLSRequest req = new CreateXLSRequest("1.1", "1234", RouteService.GRAPH_SRS);
		XLSDocument xlsRequest = req.createGeocodeRequest(addressType, "1234");
		
		//Check OpenLS Path
		if(mOpenLSLocationUtilityServicePath.equals("") || mOpenLSLocationUtilityServicePath == null){
			log.error("AddressType is not supported - No OpenLS Location Utility Service is available");
			ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.NOT_SUPPORTED,
                                 "AddressType",
                                 "AddressType in Position, AvoidList etc. is not supported - No OpenLS Location Utility Service is available");
            throw se;
		}

		///////////////////////////////////////
		//*** OpenLS Location Utility Service ***
			WebServiceConnector webserConn = new WebServiceConnector();
			XLSDocument xlsResponse = webserConn.connect(mOpenLSLocationUtilityServicePath, xlsRequest.toString());
		///////////////////////////////////////
		
		///////////////////////////////////////
		//Read the XLSDoc
		XLSType xlsTypeResponse = xlsResponse.getXLS();
		AbstractBodyType abBodyResponse[] = xlsTypeResponse.getBodyArray();
		ResponseType response = (ResponseType) abBodyResponse[0].changeType(ResponseType.type);
		
		if(response.isSetErrorList()){
			///////////////////////////////////////
			//	*** ERROR ***
			ErrorListType errorlist = response.getErrorList();
			ErrorType error[] = errorlist.getErrorArray();
			
			log.error("Problem with the OpenLS Location Utility Service, Message: "+error[0].toString());
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.UNKNOWN,
					"Problem with OpenLS Location Utility Service",
					"Problem with the OpenLS Location Utility Service, Message: "+error[0].toString());
			throw se;
		 }
		 else if(response.isSetResponseParameters()){
			 ///////////////////////////////////////
			 //	*** GeoCoded Address ***
			 AbstractResponseParametersType respParam = response.getResponseParameters();
			 GeocodeResponseType geocodeResp = (GeocodeResponseType) respParam.changeType(GeocodeResponseType.type);
			 GeocodeResponseListType geocodeList[] = geocodeResp.getGeocodeResponseListArray();
			 
			 //Save the FIRST Coordinate!!
			 for(int i=0 ; i<geocodeList.length ; i++){
				 GeocodedAddressType geocodedList[] = geocodeList[i].getGeocodedAddressArray();
				 for(int j=0 ; j<geocodedList.length ; j++){
					 if(mPosition == null){
						 PointType point = geocodedList[j].getPoint();
						 DirectPositionType dp = point.getPos();

						 if(geocodedList[j].getAddress().getStreetAddress().getStreetArray(0).isSetOfficialName())
							 mPosition = new WayPoint(Pos.getCoord(dp.getStringValue()), geocodedList[j].getAddress().getStreetAddress().getStreetArray(0).getOfficialName());
						 else
							 mPosition = new WayPoint(Pos.getCoord(dp.getStringValue()));
					 }
				 }
			 }
		 }
		
		 return mPosition;
	}
}