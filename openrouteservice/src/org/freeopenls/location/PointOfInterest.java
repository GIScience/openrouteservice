
package org.freeopenls.location;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.freeopenls.connector.CreateXLSRequest;
import org.freeopenls.connector.WebServiceConnector;
import org.freeopenls.constants.RouteService;
import org.freeopenls.error.ServiceError;
import org.freeopenls.gml.Pos;

import com.vividsolutions.jts.geom.Coordinate;

import net.opengis.gml.PointType;
import net.opengis.xls.AbstractBodyType;
import net.opengis.xls.AbstractResponseParametersType;
import net.opengis.xls.DirectoryResponseType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.ErrorListType;
import net.opengis.xls.ErrorType;
import net.opengis.xls.POIAttributeListType;
import net.opengis.xls.POIInfoListType;
import net.opengis.xls.POIInfoType;
import net.opengis.xls.POIWithDistanceType;
import net.opengis.xls.PointOfInterestType;
import net.opengis.xls.ResponseType;
import net.opengis.xls.SeverityType;

import net.opengis.xls.XLSDocument;
import net.opengis.xls.XLSType;


/**
 * Class for read the xls:POI Element
 * 
 * Copyright: Copyright (c) 2008 by Pascal Neis
 * 
 * @author Pascal Neis, i3mainz, neis@geographie.uni-bonn.de
 * @version 1.0 2007-07-27
 */
public class PointOfInterest{
	/** Logger, used to log errors(exceptions) and additonaly information */
	private static final Logger log = Logger.getLogger(PointOfInterest.class.getName());

	/** SRS for the POI Coordinate */
	private String m_sTargetSRS = "";
	/** WayPoint of the POI */
	private WayPoint mPosition = null;
	/** POI ID */
	private String m_sID = null;
	/** POI Name */
	private String m_sPOIName = null;
	/** POI Phonenumber */
	private String m_sPhoneNumber = null;
	/** POI Description */
	private String m_sDescription = null;
	
	/** OpenLS Location Utility Service Path */
	private String mOpenLSLocationUtilityServicePath = null;
	/** OpenLS OpenLS Directory Service Path */
	private String mOpenLSDirectoryServicePath = null;
	
	/**
	 * Constructor
	 *
	 */
	public PointOfInterest(String openLSLocationUtilityServicePath, String openLSDirectoryServicePath){
		mOpenLSLocationUtilityServicePath = openLSLocationUtilityServicePath;
		mOpenLSDirectoryServicePath = openLSDirectoryServicePath;
	}

	/**
	 * Method that read the PointOfInterest elements.<br>
	 * 
	 * @param sTargetSRS
	 * 			SRS in which the Coordinate should be (EPSG:XXXX)
	 * @param poiType
	 * 			PointOfInterestType
	 * @throws ServiceError
	 */
	public void setPointOfInterest(String sTargetSRS, PointOfInterestType poiType)throws ServiceError{
		
		m_sTargetSRS = sTargetSRS;
		
		ArrayList<String> sPOINameList = new ArrayList<String>();
		ArrayList<String> sPOIValueList = new ArrayList<String>();
		
		//ID
		m_sID = poiType.getID();
		//POIName
		if(poiType.isSetPOIName())
			m_sPOIName = poiType.getPOIName();
		//PhoneNumber
		if(poiType.isSetPhoneNumber())
			m_sPhoneNumber = poiType.getPhoneNumber();
		//Description
		if(poiType.isSetDescription())
			m_sDescription = poiType.getDescription();


		//POIAttributeList
		if(poiType.isSetPOIAttributeList()){
			POIAttributeListType poiAttListType = poiType.getPOIAttributeList();
			
			//POIInfoList
			if(poiAttListType.isSetPOIInfoList()){
				POIInfoListType poiInfoListType = poiAttListType.getPOIInfoList();
				
				//POIInfoType
				POIInfoType poiInfoArray[] = poiInfoListType.getPOIInfoArray();
				if(poiInfoArray.length > 0){
					
					for(int i=0 ; i<poiInfoArray.length ; i++){
						sPOINameList.add(poiInfoArray[i].getName());
						sPOIValueList.add(poiInfoArray[i].getValue());
					}
					
					queryDirectotyService(sPOINameList,sPOIValueList);
				}
			}
			//ReferenceSystem
			if(poiAttListType.isSetReferenceSystem()){
				/*TODO
				ReferenceSystemType referenceSystType = poiAttListType.getReferenceSystem();
				AbstractNamedReferenceSystem aNamedReference[] = referenceSystType.getNamedReferenceSystemArray();
				//reference system: NACE, NAICS and SIC
				*/
			}
		}
		
		//Point
		if(poiType.isSetPoint()){
			PointType pointType = poiType.getPoint();
			if(pointType.isSetSrsName())
				mPosition = new WayPoint(Pos.getCoord(m_sTargetSRS, pointType.getSrsName(), pointType.getPos()));
			else
				mPosition = new WayPoint(Pos.getCoord(m_sTargetSRS, null, pointType.getPos()));
		}

		//Point & Adress
		if(poiType.isSetPoint() && poiType.isSetAddress()){
			Coordinate c = null;
			String name = null;
			
			PointType pointType = poiType.getPoint();
			if(pointType.isSetSrsName())
				c = Pos.getCoord(m_sTargetSRS, pointType.getSrsName(), pointType.getPos());
			else
				c = (Pos.getCoord(m_sTargetSRS, null, pointType.getPos()));
			
			if(poiType.getAddress().getStreetAddress().getStreetArray(0).isSetOfficialName())
				name = poiType.getAddress().getStreetAddress().getStreetArray(0).getOfficialName();
			
			mPosition = new WayPoint(c, name);
		}

		//Address
		if(poiType.isSetAddress() && mPosition == null){
			GeocodeAddress address = new GeocodeAddress(mOpenLSLocationUtilityServicePath);
			mPosition = address.geocode(poiType.getAddress());
		}
		
		//If no coordinate is find, throws a ServiceError!
		if(mPosition == null){
			log.error("Coordinate of the found POI is 'null'");
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,"POIType",
					"Sorry, no Position for the POI with ID: '"+m_sID+"' found!!");
			throw se;
		}
	}
	
	/**
	 * Method that return Coordinate of the POI
	 * @return WayPoint
	 */
	public WayPoint getPosition(){
		return mPosition;
	}
	
	/**
	 * Method that return ID of the POI (Optional-Value)
	 * @return String
	 */
	public String getPOIID(){
		return m_sID;
	}

	/**
	 * Method that return Name of the POI (Optional-Value)
	 * @return String
	 */
	public String getPOIName(){
		return m_sPOIName;
	}

	/**
	 * Method that return PhoneNumber of the POI (Optional-Value)
	 * @return String
	 */
	public String getPOIPhoneNumber(){
		return m_sPhoneNumber;
	}

	/**
	 * Method that return Description of the POI (Optional-Value)
	 * @return String
	 */
	public String getPOIDescription(){
		return m_sDescription;
	}
	
	/**
	 * Method that querys a OpenLS Directory Service ...
	 * 
	 * @param sNameList Name Attributes of the POI
	 * @param sValueList Vlues of the POI
	 * @throws ServiceError
	 */
	private void queryDirectotyService(ArrayList<String> sNameList, ArrayList<String> sValueList)throws ServiceError{
		
		XLSDocument xlsResponse = null;
		
		///////////////////////////////////////
		//*** Create XLS Directory Request ***
		CreateXLSRequest req = new CreateXLSRequest("1.1", "1234", RouteService.GRAPH_SRS);
		XLSDocument xlsRequest = req.createDirectoryRequest(m_sPOIName, m_sPhoneNumber, sNameList, sValueList, "1234");

		//Check OpenLS Path
		if(mOpenLSDirectoryServicePath.equals("") || mOpenLSDirectoryServicePath == null){
			log.error("POIType is not full supported - No OpenLS Directory Service is available");
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.NOT_SUPPORTED,"POIType",
					"POIType in Position, AvoidList etc. is not full supported - No OpenLS Directory Service is available");
			System.out.println("HERE in SERVICEERROR");
			throw se;
		}

		///////////////////////////////////////
		//*** Connect to OpenLS Directory Service ***
		WebServiceConnector webserConn = new WebServiceConnector();
		xlsResponse = webserConn.connect(mOpenLSDirectoryServicePath, xlsRequest.toString());
		
		///////////////////////////////////////
		//*** Read the XLSDoc ***
		XLSType xlsTypeResponse = xlsResponse.getXLS();
		AbstractBodyType abBodyResponse[] = xlsTypeResponse.getBodyArray();
		ResponseType response = (ResponseType) abBodyResponse[0].changeType(ResponseType.type);
		
		if(response.isSetErrorList()){
			///////////////////////////////////////
			//	*** ERROR ***
			ErrorListType errorlist = response.getErrorList();
			ErrorType error[] = errorlist.getErrorArray();

			log.error("Problem with the OpenLS Directory Service, Message: "+error[0].toString());
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.UNKNOWN,"Problem with OpenLS Directory Service",
					"Problem with the OpenLS Directory Service, Message: "+error[0].toString());
			throw se;
		}
		else if(response.isSetResponseParameters()){
			///////////////////////////////////////
			//*** DirectoryRequest Address ***
			AbstractResponseParametersType respParam = response.getResponseParameters();
			DirectoryResponseType directoryResp = (DirectoryResponseType) respParam.changeType(DirectoryResponseType.type);
			POIWithDistanceType poiwithdist[] = directoryResp.getPOIContextArray();
			
			//Save the FIRST Coordinate!!
			for(int i=0 ; i<poiwithdist.length ; i++){
				PointOfInterestType poiTypeTMP = poiwithdist[i].getPOI();
				
				//Point
				if(poiTypeTMP.isSetPoint()){
					PointType pointType = poiTypeTMP.getPoint();
					if(pointType.isSetSrsName())
						mPosition = new WayPoint(Pos.getCoord(m_sTargetSRS, pointType.getSrsName(), pointType.getPos()));
					else
						mPosition = new WayPoint(Pos.getCoord(m_sTargetSRS, "", pointType.getPos()));
				}

				//Address
				if(poiTypeTMP.isSetAddress()){
					GeocodeAddress address = new GeocodeAddress(mOpenLSLocationUtilityServicePath);
					mPosition = address.geocode(poiTypeTMP.getAddress());
				}
			}
		}
	}
}
