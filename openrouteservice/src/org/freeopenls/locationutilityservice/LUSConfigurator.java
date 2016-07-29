/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.locationutilityservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.servlet.UnavailableException;

import org.apache.log4j.Logger;
import org.freeopenls.logger.LoggerConfig;


/**
 * <p><b>Title: LUSConfigurator</b></p>
 * <p><b>Description:</b> Class Location Utility Service Configurator</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-02-09
 */
public class LUSConfigurator {
	/** Location Utility Service Configurator instance - for access from another classes */
	private static LUSConfigurator mInstance = null;
	
	/** Properties for read the ConfigData from the File */
	private Properties mProperties;
	
    /** Logger, used to log errors(exceptions) and additionally information */
    private static Logger mLogger = Logger.getLogger(LUSConfigurator.class.getName());
	
	/** Service Name */
	private String mServiceName = null;
	private String mGeocoderName = null;
	private String mGeocodingURL = null;
	private String mReverseGeocodingURL = null;
	private String mUserAgent = null;

	/**
	 * Constructor
	 */
	public LUSConfigurator() {
		try {
			///////////////////////////////////////
			//*** Open ConfigFile ***
			String fileNameProperties = "../LocationUtilityService.properties.xml";
        	URL url = LUSServlet.class.getClassLoader().getResource(fileNameProperties);
        	InputStream inputstream = url.openStream();
        	mProperties = loadProperties(inputstream);

			///////////////////////////////////////
			//Logging
        	String fileNameLog = mProperties.getProperty("LOGFILE");
        	URL urlLogs = LUSConfigurator.class.getClassLoader().getResource(fileNameLog);
	        LoggerConfig.initLogger(new File(urlLogs.getFile()));
	        
	        //Service Name
	        mServiceName = mProperties.getProperty("SERVICE_NAME");
	        
			mLogger.info("***********************************************************************************");
			mLogger.info("* * OpenLS Location Utility Service Configurator - ServiceName: "+mServiceName+" *");

			mGeocoderName = mProperties.getProperty("GEOCODER_NAME"); 
			mGeocodingURL = mProperties.getProperty("GEOCODING_URL");
			mReverseGeocodingURL = mProperties.getProperty("REVERSE_GEOCODING_URL");
			mUserAgent = mProperties.getProperty("USER_AGENT");

			mLogger.info("*  Geocoding URL: "+mGeocodingURL);
			mLogger.info("*  Reverse Geocoding URL Reverse: "+mReverseGeocodingURL);
			mLogger.info("*  User Agent: "+mUserAgent);
			mLogger.info("*  Config-File loaded;  s u c c e s s f u l l y");
			mLogger.info("***********************************************************************************");

		} catch (Exception e) {
			e.printStackTrace();
			mLogger.error("Exception in Location Utility Service Configurator - Message: "+ e);
		}

	}

	/**
	 * Method that returns a instance of the AccessibilityConfigurator
	 * 
	 * @return AccessibilityConfigurator
	 */
	public static synchronized LUSConfigurator getInstance() {
		if (mInstance == null) {
			mInstance = new LUSConfigurator();
		}
		return mInstance;
	}

	/**
	 * Method that load and returns Porperties
	 * 
	 * @param is
	 * 			InputStream for read propdata
	 * @return Poperties
	 * @throws UnavailableException
	 * @throws IOException
	 */
	private Properties loadProperties(InputStream is)
			throws UnavailableException, IOException {
		Properties properties = new Properties();
		properties.loadFromXML(is);

		return properties;
	}

	/**
	 * @return the ServiceName
	 */
	public String getServiceName() {
		return mServiceName;
	}

	/**
	 * @return the URL of Nominatim Service which should be used
	 */
	public String getGeocodingURL() {
		return mGeocodingURL;
	}
	public String getReverseGeocodingURL() {
		return mReverseGeocodingURL;
	}
	
	public String getUserAgent() {
		return mUserAgent;
	}
	
	public String getGeocoderName() {
		return mGeocoderName;
	}
}
