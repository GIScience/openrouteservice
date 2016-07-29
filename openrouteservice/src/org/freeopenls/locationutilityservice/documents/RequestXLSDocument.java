package org.freeopenls.locationutilityservice.documents;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.freeopenls.constants.LocationUtilityService;
import org.freeopenls.constants.OpenLS.RequestParameter;
import org.freeopenls.error.ErrorTypes;
import org.freeopenls.error.ServiceError;
import org.freeopenls.location.Position;
import org.freeopenls.locationutilityservice.LUSConfigurator;
import org.freeopenls.locationutilityservice.geocoders.Geocoder;
import org.freeopenls.locationutilityservice.geocoders.GeocoderFactory;
import org.freeopenls.locationutilityservice.geocoders.GeocodingResult;
import org.freeopenls.locationutilityservice.geocoders.GeocodingUtils;
import org.freeopenls.tools.CoordTransform;
import org.freeopenls.tools.FormatUtility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.PointType;
import net.opengis.xls.AbstractRequestParametersType;
import net.opengis.xls.AddressType;
import net.opengis.xls.BuildingDocument;
import net.opengis.xls.BuildingLocatorType;
import net.opengis.xls.DistanceType;
import net.opengis.xls.DistanceUnitType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.GeocodeRequestType;
import net.opengis.xls.GeocodeResponseListType;
import net.opengis.xls.GeocodeResponseType;
import net.opengis.xls.GeocodedAddressType;
import net.opengis.xls.GeocodingQOSType;
import net.opengis.xls.NamedPlaceClassification;
import net.opengis.xls.NamedPlaceType;
import net.opengis.xls.RequestType;
import net.opengis.xls.ReverseGeocodePreferenceType;
import net.opengis.xls.ReverseGeocodeRequestType;
import net.opengis.xls.ReverseGeocodeResponseType;
import net.opengis.xls.ReverseGeocodedLocationType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.StreetAddressType;
import net.opengis.xls.StreetNameType;

/**
 * <p>
 * <b>Title: RequestXLSDocument</b>
 * </p>
 * <p>
 * <b>Description:</b> Class for read and create XLSDocument (Location Request
 * and Location Response)
 * </p>
 * 
 * <p>
 * <b>Copyright:</b> Copyright (c) 2008
 * </p>
 * <p>
 * <b>Institution:</b> University of Bonn, Department of Geography
 * </p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-02-15
 * @version 1.1 2008-04-20
 */
public class RequestXLSDocument {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static Logger mLogger = Logger.getLogger(LUSConfigurator.class.getName());

	/** LocationUtilityConfigurator Instance */
	private LUSConfigurator mLUSConfigurator;

	/** Response Document */
	private ResponseXLSDocument mResponseXLSDocument;

	/** SRSName in which the Response should be **/
	private String mResponseSRS = "";

	/** Number of maximum Responses */
	private int mMaximumResponses = 10;
	
	/**
	 * Constructor
	 * 
	 * @param sessionID
	 * @param responseSRS
	 */
	public RequestXLSDocument(String sessionID, String responseSRS) {
		mLUSConfigurator = LUSConfigurator.getInstance();
		mResponseSRS = responseSRS;

		mResponseXLSDocument = new ResponseXLSDocument(sessionID);
	}

	/**
	 * Method that add a request to the response
	 * 
	 * @param requestType 
	 * @throws ServiceError
	 * @throws Exception
	 */
	public void doGeocodeRequest(RequestType requestType) throws ServiceError, Exception {

		// /////////////////////////////////////
		// *** GetRequestParameters ***
		// Parameter MaximumResponses is Optional
		if (requestType.isSetMaximumResponses())
			if (requestType.getMaximumResponses().intValue() <= 0)
				throw ErrorTypes.parameterMissing(RequestParameter.maximumResponses.toString());
			else
				mMaximumResponses = requestType.getMaximumResponses().intValue();
		// Parameter MethodName is Mandatory
		if (!requestType.getMethodName().equalsIgnoreCase(LocationUtilityService.METHODNAME_GEOCODE)
				&& !requestType.getMethodName().equalsIgnoreCase(LocationUtilityService.METHODNAME_REVERSEGEOCODE))
			throw ErrorTypes.methodNameError(requestType.getMethodName(), LocationUtilityService.METHODNAME_GEOCODE
					+ " or " + LocationUtilityService.METHODNAME_REVERSEGEOCODE);
		// Parameter RequestID is Mandatory
		if (requestType.getRequestID().equalsIgnoreCase(""))
			throw ErrorTypes.parameterMissing(RequestParameter.requestID.toString());
		// Parameter Version is Mandatory
		if (requestType.getVersion().equalsIgnoreCase(""))
			throw ErrorTypes.parameterMissing(RequestParameter.version.toString());
		// Parameter Version is Mandatory
		if (!requestType.getVersion().equalsIgnoreCase(LocationUtilityService.SERVICE_VERSION))
			throw ErrorTypes.parameterMissing(RequestParameter.version.toString());

		// Check What Request it is
		AbstractRequestParametersType abreqparType = requestType.getRequestParameters();
		if (abreqparType instanceof GeocodeRequestType) {
			if (!requestType.getMethodName().equalsIgnoreCase(LocationUtilityService.METHODNAME_GEOCODE))
				throw ErrorTypes
						.methodNameError(requestType.getMethodName(), LocationUtilityService.METHODNAME_GEOCODE);

			// /////////////////////////////////////
			// *** GeocodeRequest ***
			GeocodeRequestType geocodeRequest = (GeocodeRequestType) abreqparType.changeType(GeocodeRequestType.type);
			doGeocodeRequest(geocodeRequest, requestType.getRequestID(), requestType.getVersion());
		} else if (abreqparType instanceof ReverseGeocodeRequestType) {
			if (!requestType.getMethodName().equalsIgnoreCase(LocationUtilityService.METHODNAME_REVERSEGEOCODE))
				throw ErrorTypes.methodNameError(requestType.getMethodName(),
						LocationUtilityService.METHODNAME_REVERSEGEOCODE);

			// /////////////////////////////////////
			// *** ReverseGeocodeRequest ***
			ReverseGeocodeRequestType reverseGeocodeRequest = (ReverseGeocodeRequestType) abreqparType
					.changeType(ReverseGeocodeRequestType.type);
			doReverseGeocodeRequest(reverseGeocodeRequest, requestType.getRequestID(), requestType.getVersion());
		} else {
			throw ErrorTypes.methodNameError(requestType.getMethodName(), LocationUtilityService.METHODNAME_GEOCODE
					+ " or " + LocationUtilityService.METHODNAME_REVERSEGEOCODE);
		}
	}

	/**
	 * Method that do GeocodeRequest <br>
	 * Search for an address the coordinate!
	 * 
	 * @param geocodeRequest
	 * @throws Exception 
	 */
	private void doGeocodeRequest(GeocodeRequestType geocodeRequest, String requestID, String version)
			throws Exception {

		// Address Array
		AddressType addressArray[] = geocodeRequest.getAddressArray();

		// Create GeocodeResponse
		mResponseXLSDocument.createResponse(requestID, version, new BigInteger("" + addressArray.length));
		GeocodeResponseType geocodeResponse = mResponseXLSDocument.addResponseParametersGeocode();

		Geocoder geocoder = GeocoderFactory.createGeocoder(mLUSConfigurator.getGeocoderName(), mLUSConfigurator.getGeocodingURL(), mLUSConfigurator.getReverseGeocodingURL(), mLUSConfigurator.getUserAgent()); 

		for (int i = 0; i < addressArray.length; i++) {
			if (addressArray[i].isSetFreeFormAddress()) {
				String freeFormAddress = addressArray[i].getFreeFormAddress();

				try {

					String code = addressArray[i].getCountryCode();

					GeocodingResult[] gresults = geocoder.geocode(freeFormAddress, code, mMaximumResponses); 

					if (gresults != null && gresults.length > 0)
					{
						GeocodedAddressType[] arrAddressType = new GeocodedAddressType[gresults.length];

						for (int j = 0; j < gresults.length; j++) {
							GeocodingResult gr = gresults[j];

							GeocodedAddressType g = GeocodedAddressType.Factory.newInstance();
							AddressType addr = g.addNewAddress();
							fillAddress(addr, gr);

							PointType pTMP = g.addNewPoint();
							DirectPositionType directposTMP = pTMP.addNewPos();
							Coordinate cPos = new Coordinate(gr.longitude, gr.latitude);
							if (!"EPSG:4326".equals(this.mResponseSRS)) {
								cPos = CoordTransform.transformGetCoord("EPSG:4326", mResponseSRS, cPos);
								// TroubleShooting with the Coordinate
								if (cPos == null) {
									ServiceError se = new ServiceError(SeverityType.ERROR);
									se.addError(ErrorCodeType.OTHER_XML, "Coordinate - Pos",
											"Problem with the Coordinate Transform: Check the Coordinate values or the SRS discription!!!");
									throw se;
								}
							}
							directposTMP.setStringValue(FormatUtility.formatCoordinate(cPos));
							directposTMP.setSrsName(mResponseSRS);

							// GeocodeMatchCode
							GeocodingQOSType QOS = g.addNewGeocodeMatchCode();
							QOS.setAccuracy(gr.accuracy);

							arrAddressType[j] = g;
						}

						GeocodeResponseListType georesplistType = geocodeResponse.addNewGeocodeResponseList();
						georesplistType.setGeocodedAddressArray(arrAddressType);
						georesplistType.setNumberOfGeocodedAddresses(BigInteger.valueOf(arrAddressType.length));

						mLogger.info(">RequestFreeform:" + freeFormAddress + ";Responses:"+ arrAddressType.length);
					}
				} catch (Exception ex) {
					mLogger.error("Error with Request FreeForm:" + freeFormAddress);
					ex.printStackTrace();
					ServiceError se = new ServiceError(SeverityType.ERROR);
					se.addError(ErrorCodeType.UNKNOWN, "Can not connect to Nominatim!", "RequestXLSDocument");
					throw se;
				}
			}
		}

		mResponseXLSDocument.doWellFormedGeocodeResponse();
		mLogger.debug("GeocodeRequest finish!");
	}
	
	private void fillAddress(AddressType addr, GeocodingResult gr)
	{
		addr.setCountryCode(gr.countryCode);
		
		if (!Helper.isEmpty(gr.country)) {
			NamedPlaceType countryType = addr.addNewPlace();
			countryType.setType(NamedPlaceClassification.COUNTRY);
			countryType.setStringValue(gr.country);
		}
		
		if (!Helper.isEmpty(gr.postalCode))
			addr.setPostalCode(gr.postalCode);
		if (!Helper.isEmpty(gr.road) || !Helper.isEmpty(gr.houseNumber)) {
			// Create the <Street> for the <Address>
			StreetAddressType streetAddress = addr.addNewStreetAddress();
			// Create the <Building> for the <Street>
			
			if (!Helper.isEmpty(gr.houseNumber) || !Helper.isEmpty(gr.house) || !Helper.isEmpty(gr.objectName))
			{
				BuildingDocument building2 = BuildingDocument.Factory.newInstance();
				BuildingLocatorType building = building2.addNewBuilding();
				if (!Helper.isEmpty(gr.house))
				   building.setBuildingName(gr.house);
				else if (!Helper.isEmpty(gr.objectName))
				   building.setBuildingName(gr.objectName);
				if (!Helper.isEmpty(gr.houseNumber))
					building.setNumber(gr.houseNumber);
				// Add the <Building> to the <StreetAddress>
				streetAddress.set(building2); // TODO Runge
			}
			// Create the <Street> for the <StreetAddress>
			StreetNameType street = streetAddress.addNewStreet();
			street.setOfficialName(gr.road);
		}
		// Add the <Street> to <StreetAddress>
		// Create the <Place> Municipality / Suburb for the
		// <Address>
		if (!Helper.isEmpty(gr.state) || !Helper.isEmpty(gr.stateDistrict) || !Helper.isEmpty(gr.county)
				|| !Helper.isEmpty(gr.city) || !Helper.isEmpty(gr.suburb)) {
			if (!Helper.isEmpty(gr.state)) {
				NamedPlaceType countrySubdivisionType = addr.addNewPlace();
				countrySubdivisionType.setType(NamedPlaceClassification.COUNTRY_SUBDIVISION);
				countrySubdivisionType.setStringValue(gr.state);
			}
			if (!Helper.isEmpty(gr.stateDistrict)) {
				NamedPlaceType countrySubdivisionType = addr.addNewPlace();
				countrySubdivisionType
						.setType(NamedPlaceClassification.COUNTRY_SECONDARY_SUBDIVISION);
				countrySubdivisionType.setStringValue(gr.stateDistrict);
			}

			if (!Helper.isEmpty(gr.county) && Helper.isEmpty(gr.stateDistrict)) {
				NamedPlaceType countrySubdivisionType = addr.addNewPlace();
				countrySubdivisionType
						.setType(NamedPlaceClassification.COUNTRY_SECONDARY_SUBDIVISION);
				countrySubdivisionType.setStringValue(gr.county);
			}

			if (!Helper.isEmpty(gr.city)) {
				NamedPlaceType municipalityType = addr.addNewPlace();
				municipalityType.setType(NamedPlaceClassification.MUNICIPALITY);
				municipalityType.setStringValue(gr.city);
			}

			if (!Helper.isEmpty(gr.suburb)) {
				NamedPlaceType municipalityType = addr.addNewPlace();
				municipalityType.setType(NamedPlaceClassification.MUNICIPALITY_SUBDIVISION);
				municipalityType.setStringValue(gr.suburb);
			}
		}
	}
	

	/**
	 * Method that do ReverseGeocodeRequest <br>
	 * Search for a coordinate the address!
	 * 
	 * @param georeq
	 * @param sRequestID
	 * @param sVersion
	 * @throws Exception 
	 */
	private void doReverseGeocodeRequest(ReverseGeocodeRequestType revgeoreq, String requestID, String version)
			throws Exception {
		// ///////////////////////////////////
		// Get ReverseGeocodePreference
		String sReverseGeocodePrefernece = ReverseGeocodePreferenceType.STREET_ADDRESS.toString();
		if (revgeoreq.sizeOfReverseGeocodePreferenceArray() > 0) {
			ReverseGeocodePreferenceType.Enum revgeopref[] = revgeoreq.getReverseGeocodePreferenceArray();
			// sReverseGeocodePrefernece = revgeopref[0].toString();
			if (!sReverseGeocodePrefernece.equals(revgeopref[0].toString())) {
				ServiceError se = new ServiceError(SeverityType.ERROR);
				se.addError(ErrorCodeType.NOT_SUPPORTED, "ReverseGeocodePreference",
						"The Value '" + revgeopref[0].toString()
								+ "' is NOT supported by this OpenLS Location Utility Service Version. Possible is: '"
								+ ReverseGeocodePreferenceType.STREET_ADDRESS.toString() + "'.");
				throw se;
			}
		}

		Position position = new Position();
		position.setPosition(LocationUtilityService.ADDRESS_SRS, revgeoreq.getPosition());
		Coordinate cLocation = position.getPosition().getCoordinate();

		Geocoder geocoder = GeocoderFactory.createGeocoder(mLUSConfigurator.getGeocoderName(), mLUSConfigurator.getGeocodingURL(), mLUSConfigurator.getReverseGeocodingURL(), mLUSConfigurator.getUserAgent()); 

		try {
			GeocodingResult gr = geocoder.reverseGeocode(cLocation.y, cLocation.x, mMaximumResponses);

			// ReverseGeocodedLocation
			ReverseGeocodedLocationType revgeolocation[] = new ReverseGeocodedLocationType[1];
			ReverseGeocodedLocationType loc = ReverseGeocodedLocationType.Factory.newInstance();
			revgeolocation[0] = loc;

			// Position
			PointType pTMP = loc.addNewPoint();
			DirectPositionType directposTMP = pTMP.addNewPos();
			Coordinate cPos = new Coordinate(gr.longitude, gr.latitude);
			directposTMP.setStringValue(FormatUtility.formatCoordinate(cPos));
			directposTMP.setSrsName(mResponseSRS);

			DistanceType distance = loc.addNewSearchCentreDistance();
			double dDistanceToAddress = cLocation.distance(cPos);
			dDistanceToAddress = GeocodingUtils.getDistance(dDistanceToAddress);

			if (dDistanceToAddress > 1000) {
				dDistanceToAddress = dDistanceToAddress / 1000;
				distance.setUom(DistanceUnitType.KM);
			} else
				distance.setUom(DistanceUnitType.M);

			DecimalFormat df = new DecimalFormat("0.0");
			distance.setValue(new BigDecimal(df.format(dDistanceToAddress).replace(",",".")));

			AddressType addr = loc.addNewAddress();
			fillAddress(addr, gr);

			// /////////////////////////////////////
			// Create GeocodeResponse
			mResponseXLSDocument.createResponse(requestID, version, new BigInteger("" + 1));
			ReverseGeocodeResponseType reverseGeocodeResponse = mResponseXLSDocument
					.addResponseParametersReverseGeocode();
			reverseGeocodeResponse.setReverseGeocodedLocationArray(revgeolocation);

			mLogger.info(">RequestReverse:" + cLocation.x + " " + cLocation.y + ";NominatimResponse:1");

		} catch (Exception ex) {
			mLogger.error("Error with Request Location:" + cLocation);
			ex.printStackTrace();
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.UNKNOWN, "Can not connect to Nominatim!", "RequestXLSDocument");
			throw se;
		}

		mResponseXLSDocument.doWellFormedReverseGeocodeResponse();
		mLogger.debug("ReverseGeocodeRequest finish!");
	}

	/**
	 * Method that returns response XLSDocument
	 * 
	 * @return XLSDocument
	 */
	public ResponseXLSDocument getResponseXLSDocument() {
		return mResponseXLSDocument;
	}
}
