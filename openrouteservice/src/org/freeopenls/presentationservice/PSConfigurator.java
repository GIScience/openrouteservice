/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.presentationservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.servlet.UnavailableException;

import org.apache.log4j.Logger;
import org.freeopenls.logger.LoggerConfig;


/**
 * <p><b>Title: PSConfigurator </b></p>
 * <p><b>Description:</b> Class OpenLS Presentation Service Configurator </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-05-24
 */
public class PSConfigurator {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static Logger mLogger = Logger.getLogger(PSConfigurator.class.getName());
	/** PresentationConfigurator instance - for access from another classes */
	private static PSConfigurator mInstance = null;
	/** Properties for read the ConfigData from the File */
	private Properties mProperties;
	
	/** WMS Path */
	private String m_sWMS_Path = "";

	/** Maps Path */
	private String m_sMapPath = "";
	private String m_sMapPathWWW = "";
	
	/** RouteMap Destination Symbol Path */
	private String sRouteMapDestinationSymbolPath = "";
	/** RouteMap Destination Symbol Format */
	private String sRouteMapDestinationSymbolFormat = "";
	
    /** Service Name */
	private String mServiceName = null;

	/**
	 * Constructor
	 */
	public PSConfigurator() {
		try {
			///////////////////////////////////////
			//*** Open ConfigFile ***
			File file = new File("../PresentationService.properties.xml");
			URL url = PSConfigurator.class.getClassLoader().getResource(file.toString());
			InputStream inputstream = url.openStream();
			mProperties = loadProperties(inputstream);

			///////////////////////////////////////
			//Logging
        	String fileNameLog = mProperties.getProperty("LOGFILE");
        	URL urlLogs = PSConfigurator.class.getClassLoader().getResource(fileNameLog);
	        LoggerConfig.initLogger(new File(urlLogs.getFile()));
	        
	        //Service Name
	        mServiceName = mProperties.getProperty("SERVICENAME");

			//WMS Path
			this.m_sWMS_Path = mProperties.getProperty("WMS_PATH");

			//Paths for save and access to Maps
			this.m_sMapPath = this.mProperties.getProperty("MAPS_PATH");
			this.m_sMapPathWWW = this.mProperties.getProperty("MAPS_PATH_WWW");
			
			//RouteGeometry
			this.sRouteMapDestinationSymbolPath = mProperties.getProperty("ROUTEMAP_DESTINATION_SYMBOL_PATH");
			this.sRouteMapDestinationSymbolFormat = mProperties.getProperty("ROUTEMAP_DESTINATION_SYMBOL_FORMAT");

			mLogger.info("***********************************************************************************");
			mLogger.info("* * OpenLS Presentation Service Configurator - ServiceName: "+mServiceName+" *");
			mLogger.info("*  WMS Path: "+ m_sWMS_Path);
			mLogger.info("*  Config-File loaded:  s u c c e s s f u l l y ");
			mLogger.info("***********************************************************************************");

			
		} catch (Exception e) {
			mLogger.error(e);
		}

	}

	/**
	 * Method that returns a instance of the PresentationConfigurator
	 * 
	 * @return PresentationConfigurator
	 */
	public static synchronized PSConfigurator getInstance() {
		if (mInstance == null) {
			mInstance = new PSConfigurator();
		}
		return mInstance;
	}

	/**
     * Method that returns Path to WMS
     * @return String
     */
    public String getWMSPath(){
    	return this.m_sWMS_Path;
    }

    /**
     * Method that returns Path for save the Maps
     * @return String
     */
    public String getMapsPath(){
    	return this.m_sMapPath;
    }

    /**
     * Method that returns Path for access the Maps
     * @return String
     */
    public String getMapsPathWWW(){
    	return this.m_sMapPathWWW;
    }

    /**
     * Method that returns Path to the destination symbol in the RouteMap
     * @return String
     */
    public String getRouteMapDestinationSymbolPath(){
    	return this.sRouteMapDestinationSymbolPath;
    }

    /**
     * Method that returns Format of the destination symbol in the RouteMap
     * @return String
     */
    public String getRouteMapDestinationSymbolFormat(){
    	return this.sRouteMapDestinationSymbolFormat;
    }

    /**
	 * @return the ServiceName
	 */
	public String getServiceName() {
		return mServiceName;
	}

	/**
	 * Method that load and returns Properties
	 * 
	 * @param is
	 * 			InputStream for read properties data
	 * @return Properties
	 * @throws UnavailableException
	 * @throws IOException
	 */
	private Properties loadProperties(InputStream is)
			throws UnavailableException, IOException {
		Properties properties = new Properties();
		properties.loadFromXML(is);

		return properties;
	}
}
