/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.restful;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import javax.servlet.UnavailableException;

import org.apache.log4j.Logger;
import org.freeopenls.logger.LoggerConfig;


/**
 * <p><b>Title: RESTfulRSConfigurator</b></p>
 * <p><b>Description:</b> Class to configure the RESTful Servlet <br>
 * Read the ConfigData from "RESTful.properties.xml"</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-08-01
 */
public class RESTfulConfigurator {
   
    /** Configurator instance - for access from another classes */
    private static RESTfulConfigurator mInstance = null;
	
	/** Properties for read the ConfigData from the File */
    private Properties mProperties;

    /** Logger, used to log errors(exceptions) and additionally information */
    private static Logger mLogger = Logger.getLogger(RESTfulConfigurator.class.getName());

	/** OpenLS Route Service Parameters */
	private String mOpenLS_RS_Path = "";
	/** OpenLS Location Utility Service Parameters */
	private String mOpenLS_LUS_Path = "";

	/** Service Name */
	private String mServiceName = null;
	
    /**
     * Constructor
     */
	private RESTfulConfigurator(){
		
		try{
			//*** Open ConfigFile ***
			String fileNameProperties = "../RESTful.properties.xml";
        	URL url = RESTfulServlet.class.getClassLoader().getResource(fileNameProperties);
        	InputStream inputstream = url.openStream();
        	mProperties = loadProperties(inputstream);

			//Logging
        	String fileNameLog = mProperties.getProperty("LOGFILE");
        	URL urlLogs = RESTfulConfigurator.class.getClassLoader().getResource(fileNameLog);
	        LoggerConfig.initLogger(new File(urlLogs.getFile()));
	        
	        //Service Name
	        mServiceName = mProperties.getProperty("SERVICENAME");
	        
			mLogger.info("***********************************************************************************");
			mLogger.info("* * RESTful Configurator - ServiceName: "+mServiceName+" *");

			//Parameter to connect OpenLS Route Service
			mOpenLS_RS_Path = mProperties.getProperty("OPENLS_RS");
			//Parameter to connect OpenLS Route Service
			mOpenLS_LUS_Path = mProperties.getProperty("OPENLS_LUS");
			
			mLogger.info("*  OpenLS Route Service Path: "+mOpenLS_RS_Path);
			mLogger.info("*  OpenLS Location Utility Service Path: "+mOpenLS_RS_Path);
			mLogger.info("*  Config-File loaded:  s u c c e s s f u l l y ");
			mLogger.info("***********************************************************************************");
		}
		catch (Exception e) {
			mLogger.error(e);
		}
	}

    /**
     * Method that returns a instance of the RESTfulRSConfigurator
     * 
     * @return RESTfulRSConfigurator
     */
	public static synchronized RESTfulConfigurator getInstance(){
		if (mInstance == null)
			mInstance = new RESTfulConfigurator();
		return mInstance;
	}
 
    /**
     * Method that returns Path to OpenLS Route Service
     * @return String
     */
    public String getOpenLSRouteServicePath(){
    	return mOpenLS_RS_Path;
    }
    
    /**
     * Method that returns Path to OpenLS Location Utility Service
     * @return String
     */
    public String getOpenLSLocationUtilityServicePath(){
    	return mOpenLS_LUS_Path;
    }

    private Properties loadProperties(InputStream is)throws UnavailableException, IOException {
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
}
