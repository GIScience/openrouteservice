
package org.freeopenls.routeservice.documents;

import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.freeopenls.constants.RouteService;
import org.freeopenls.constants.RouteService.RouteHandleParameter;
import org.freeopenls.error.ServiceError;
import org.freeopenls.routeservice.RSConfigurator;
import org.freeopenls.tools.FileUtility;

import net.opengis.xls.DetermineRouteRequestType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.RouteHandleType;
import net.opengis.xls.SeverityType;

import com.vividsolutions.jump.io.IllegalParametersException;

/**
 * Class for RouteHandle.<br>
 * Save/Read DetermineRouteRequest.
 * 
 * @author Pascal Neis, i3mainz, neis@geoinform.fh-mainz.de
 * @version 1.0 2006-05-15
 */

/**
 * <p><b>Title: RouteHandle</b></p>
 * <p><b>Description:</b> Class for RouteHandle - Save/Read DetermineRouteRequest.<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-05-15
 */
public class RouteHandle {
	/** Logger, used to log errors(exceptions) and additonaly information */
	private static final Logger log = Logger.getLogger(RouteHandle.class.getName());
    /** Path to save/read RouteHandle-Parameters */
    private String m_sRouteMapPath = "";
	
    /**
     * Constructor - Sets LogHandler/Level, RouteMap-Path form RSConfigurator
     */
	public RouteHandle(){
    	RSConfigurator config = RSConfigurator.getInstance();
		m_sRouteMapPath = config.getTempPath();
	}

	/**
	 * Method that saves DetermineRouteRequestType and creates RouteHandleType<br>
	 * NOT supported and TODO:<br>
	 * RouteHandle: -ServiceID<br
	 * 
	 * @param routehandleType
	 * @param sRouteRequestID
	 * @param drrType
	 * @throws IllegalParametersException
	 * @throws Exception
	 */
	public void saveRouteHandle(RouteHandleType routehandleType, String sRouteRequestID, DetermineRouteRequestType drrType)throws IllegalParametersException, Exception{
		//SetRouteID
		routehandleType.setRouteID(sRouteRequestID);
		//Save *.xml file
		String sFileName = m_sRouteMapPath+"/"+sRouteRequestID+".xml";
		FileOutputStream out = new FileOutputStream(sFileName);
		out.write(drrType.toString().getBytes());
		out.close();
		//SetServiceID
		routehandleType.setServiceID(RouteService.SERVICE_VERSION.toString());

//log.info("doProvideRouteHandle, RouteID="+sRouteRequestID);
	}

	/**
	 * Method that read DetermineRouteRequestType from file: "RouteID".xml<br>
	 * 
	 * @param drrType
	 * @param sRequestID
	 * @param sVersion
	 * @return DetermineRouteRequestType
	 * @throws ServiceError
	 */
	public DetermineRouteRequestType readRouteHandle(DetermineRouteRequestType drrType, String sRequestID, String sVersion)throws ServiceError {

		XmlObject doc = null;
		
		//Set RouteHandle variables
		RouteHandleType routehandleIn = null;
		String sRouteID = null; 		// Mandatory
		String sServiceID = null; 		// Optional

		//Get RouteHandle Parameters
		routehandleIn = drrType.getRouteHandle();
		sRouteID = routehandleIn.getRouteID();
		if (sRouteID.equalsIgnoreCase("")) {
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,
					RouteHandleParameter.RouteID.toString(),
					"The required value of the mandatory parameter '"
							+ RouteHandleParameter.RouteID.toString() + "'"
							+ " is missing");
			throw se;
		}
		if (routehandleIn.isSetServiceID()) {
			sServiceID = routehandleIn.getServiceID();

			if (sServiceID.equalsIgnoreCase("")|| sServiceID.equalsIgnoreCase(RouteService.SERVICE_VERSION)) {
				ServiceError se = new ServiceError(SeverityType.ERROR);
				se.addError(ErrorCodeType.REQUEST_VERSION_MISMATCH,
						RouteHandleParameter.ServiceID.toString(),
						"Version of Request Schema not supported."
								+ "The value of the optional parameter '"
								+ RouteHandleParameter.ServiceID.toString() + "'"
								+ "must be '"+ RouteService.SERVICE_VERSION+"'. Delivered value was: " + sServiceID);
			}
		}

		//Get saved Route
		try {
			String sFileName = m_sRouteMapPath+"/"+sRouteID+".xml";
			String inputString = FileUtility.readFile(sFileName);
			// decode the application/x-www-form-urlencoded query string
			String decodedString = java.net.URLDecoder.decode(inputString, "UTF-8");
			
			doc = XmlObject.Factory.parse(decodedString);

		} catch (Exception e) {
			ServiceError se = new ServiceError(SeverityType.ERROR);
			log.info("- Exception: RouteHandle - The system can't find the indicated RouteID! \n Message:" + e.toString() + " -");
			se.addError(ErrorCodeType.UNKNOWN,
									"RouteHandle routeID",
									"The system can't find the indicated routeID!");
			throw se;
		}
//log.info("doRouteHandle, RouteID="+sRouteID);
		
		return (DetermineRouteRequestType)doc.changeType(DetermineRouteRequestType.type);
	}
}