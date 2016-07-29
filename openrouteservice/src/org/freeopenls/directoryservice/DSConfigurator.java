/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.directoryservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import javax.servlet.UnavailableException;

import org.apache.log4j.Logger;
import org.freeopenls.database.ConnectionManager;
import org.freeopenls.database.ConnectionParameter;
import org.freeopenls.logger.LoggerConfig;


/**
 * <p><b>Title: RequestOperator</b></p>
 * <p><b>Description:</b> Class to configure the DirectoryService Servlet <br>
 * Read the ConfigData from "DirectoryService.properties.xml"</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-04-21
 */
public class DSConfigurator {
   
    /** RouteServiceConfigurator instance - for access from another classes */
    private static DSConfigurator mInstance = null;
	
	/** Properties for read the ConfigData from the File */
    private Properties mProperties;

    /** Logger, used to log errors(exceptions) and additionally information */
    private static Logger mLogger = Logger.getLogger(DSConfigurator.class.getName());

	/** PostGIS Parameters for Address-Search */
    private ConnectionManager mConnectionManager;
    private String mPOIDatabaseSRS = "";
    
	/** OpenLS Location Utility Service Parameters */
	private String mOpenLS_LUS_Path = "";
	/** OpenLS Directory Service Parameters */
	private String mOpenLS_DS_Path = "";
	
	/** Min and max distance value in meter **/
	private int mMinDistanceValue = 0;
	private int mMaxDistanceValue = 0;
	
    /** Service Name */
	private String mServiceName = null;
	
    /**
     * Constructor
     */
	private DSConfigurator(){
		
		try{
			///////////////////////////////////////
			//*** Open ConfigFile ***
			String fileNameProperties = "../DirectoryService.properties.xml";
        	URL url = DSServlet.class.getClassLoader().getResource(fileNameProperties);
        	InputStream inputstream = url.openStream();
        	mProperties = loadProperties(inputstream);

			///////////////////////////////////////
			//Logging
        	String fileNameLog = mProperties.getProperty("LOGFILE");
        	URL urlLogs = DSConfigurator.class.getClassLoader().getResource(fileNameLog);
	        LoggerConfig.initLogger(new File(urlLogs.getFile()));
	        
			//Min and max distance value in meter
			mMinDistanceValue = Integer.parseInt(mProperties.getProperty("MIN_VALUE"));
			mMaxDistanceValue = Integer.parseInt(mProperties.getProperty("MAX_VALUE"));
	        
	        //Service Name
	        mServiceName = mProperties.getProperty("SERVICENAME");
	        
			mLogger.info("***********************************************************************************");
			mLogger.info("* * OpenLS Directory Service Configurator - ServiceName: "+mServiceName+" *");

			///////////////////////////////////////
			//PostGIS Parameters for POI search
			ConnectionParameter connParam = new ConnectionParameter("PostGIS", mProperties.getProperty("PG_POI_SERVER"),
					mProperties.getProperty("PG_POI_PORT"), mProperties.getProperty("PG_POI_DB"),
					 mProperties.getProperty("PG_POI_TABLE"), mProperties.getProperty("PG_POI_USER"),
					mProperties.getProperty("PG_POI_PASSWD"));
			
			mPOIDatabaseSRS = mProperties.getProperty("PG_POI_SRS");
			int numberofconnections = Integer.parseInt(mProperties.getProperty("PG_POI_NUMBEROFCONNECTIONS"));
			
			//Parameter to connect OpenLS Location Utility Service
			mOpenLS_LUS_Path = mProperties.getProperty("OPENLS_LUS");
			//Parameter to connect OpenLS Directory Service
			mOpenLS_DS_Path = mProperties.getProperty("OPENLS_DS");
	
			mConnectionManager = new ConnectionManager(connParam,numberofconnections);

			mLogger.info("*  DB Path: "+connParam.getHost());
			mLogger.info("*  DB Name: "+connParam.getDBName());
			mLogger.info("*  Port   : "+connParam.getPort());
			mLogger.info("*  Table  : "+connParam.getTableName());
			mLogger.info("*  Connections : "+numberofconnections);
			mLogger.info("*  Search Distances - Min: "+mMinDistanceValue+"m Max: "+mMaxDistanceValue+"m");
			mLogger.info("*  OpenLS Location Utility Service Path: "+mOpenLS_LUS_Path);
			mLogger.info("*  OpenLS Directory Service Path: "+mOpenLS_DS_Path);
			mLogger.info("*  Config-File loaded;  s u c c e s s f u l l y");
			mLogger.info("***********************************************************************************");

		}
		catch (Exception e) {
			mLogger.error("- Exception in OpenLS Directory Service Configurator \n Message:  "+e);
		}
	}

    /**
     * Method that returns a instance of the DirectoryServiceConfigurator
     * 
     * @return DirectoryServiceConfigurator
     */
	public static synchronized DSConfigurator getInstance(){
		if (mInstance == null)
			mInstance = new DSConfigurator();
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
     * Method that returns Path to OpenLS Location Utility Service
     * @return String
     */
    public String getOpenLSLocationUtilityServicePath(){
    	return mOpenLS_LUS_Path;
    }

    /**
     * Method that returns Path to OpenLS Directory Service
     * @return String
     */
    public String getOpenLSDirectoryServicePath(){
    	return mOpenLS_DS_Path;
    }

	/**
	 * @return the ServiceName
	 */
	public String getServiceName() {
		return mServiceName;
	}

    /**
     * Method that returns Minimum Distance
     * @return int
     */
    public int getMinDistance(){
    	return mMinDistanceValue;
    }

    /**
     * Method that returns Maximum Distance
     * @return int
     */
    public int getMaxDistance(){
    	return mMaxDistanceValue;
    }

	/**
	 * @return the ConnectionManager
	 */
	public ConnectionManager getConnectionManager() {
		return mConnectionManager;
	}
	
	/**
	 * @return the POIDatabaseSRS
	 */
	public String getPOIDatabaseSRS() {
		return mPOIDatabaseSRS;
	}
}
