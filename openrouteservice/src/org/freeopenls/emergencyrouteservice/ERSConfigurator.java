/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.emergencyrouteservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import javax.servlet.UnavailableException;

import org.apache.log4j.Logger;
import org.freeopenls.logger.LoggerConfig;


/**
 * <p><b>Title: ERSConfigurator </b></p>
 * <p><b>Description:</b> Class for configurate the ERS Servlet<br>
 * Read the ConfigData from "config.properties.xml" and create ..... see Constructor!!!  </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-07-24
 */
public class ERSConfigurator {
    /** Logger, used to log errors(exceptions) and additionally information */
    private static Logger mLogger = Logger.getLogger(ERSConfigurator.class.getName());
    
    /** Properties for read the ConfigData from the File */
    private Properties mProperties;

    /** ERSConfigurator instance - for access from another classes */
    private static ERSConfigurator mInstance = null;

	/** OpenLS Route Service Parameters */
	private String mOpenLS_RS_Path = "";
	
	/** OpenLS Location Utility Service Parameters */
	private String mOpenLS_LUS_Path = "";

	/** OpenLS Directory Service Parameters */
	private String mOpenLS_DS_Path = "";
	
    /** Path to WFS with AvoidAreas*/
    private String mWFS_Path = "";
    private String mWFS_Layername = "";
    private String mWFS_GeomColumnName = "";
    private String mWFS_XMLNS = "";
    private String mWFS_SRS= "";
    private double mWFS_BBox_Extend = 0;
	
	/** Service Name */
	private String mServiceName = null;
	
    /**
     * Constructor
     */
	private ERSConfigurator(){

		try{
			///////////////////////////////////////
			//*** Open ConfigFile ***
			File file = new File("../EmergencyRouteService.properties.xml");
        	URL url = ERSServlet.class.getClassLoader().getResource(file.toString());
        	InputStream inputstream = url.openStream();
        	mProperties = loadProperties(inputstream);

			///////////////////////////////////////
			//Logging
        	String fileNameLog = mProperties.getProperty("LOGFILE");
        	URL urlLogs = ERSConfigurator.class.getClassLoader().getResource(fileNameLog);
	        LoggerConfig.initLogger(new File(urlLogs.getFile()));

	        //Service Name
	        mServiceName = mProperties.getProperty("SERVICENAME");

			mLogger.info("***********************************************************************************");
			mLogger.info("* * OpenLS Emergency Route Service Configurator - ServiceName: "+mServiceName+" *");

			///////////////////////////////////////
			//Parameter to connect OpenLS Route Service
			mOpenLS_RS_Path = mProperties.getProperty("OPENLS_RS");

			///////////////////////////////////////
			//Parameter to connect OpenLS Location Utility Service
			mOpenLS_LUS_Path = mProperties.getProperty("OPENLS_LUS");
			
			///////////////////////////////////////
			//Parameter to connect OpenLS Directory Service
			mOpenLS_DS_Path = mProperties.getProperty("OPENLS_DS");
			
			///////////////////////////////////////
			//Save Path and Layername of WFS
			mWFS_Path = mProperties.getProperty("WFS_PATH");
			mWFS_Layername = mProperties.getProperty("WFS_LAYERNAME");
			mWFS_GeomColumnName = mProperties.getProperty("WFS_GEOMCOLUMN");
			mWFS_XMLNS = mProperties.getProperty("WFS_XMLNS");
			mWFS_SRS = mProperties.getProperty("WFS_SRS");
			mWFS_BBox_Extend = Double.parseDouble(mProperties.getProperty("WFS_BBOX_EXTEND"));		
			

			mLogger.info("*  OpenLS Route Service Path: "+mOpenLS_RS_Path);
			mLogger.info("*  OpenLS Location Utility Service Path: "+mOpenLS_LUS_Path);
			mLogger.info("*  OpenLS Directory Service Path: "+mOpenLS_DS_Path);
			mLogger.info("*  WFS Path: "+mWFS_Path+" LayerName: "+mWFS_Layername);
			mLogger.info("*    Column: "+mWFS_GeomColumnName+" XMLNS: "+mWFS_XMLNS);
			mLogger.info("*  Config-File loaded:  s u c c e s s f u l l y ");
			mLogger.info("***********************************************************************************");

		}
		catch (Exception e) {
			mLogger.error(e);
		}
	}

    /**
     * Method that returns a instance of the ERSConfigurator
     * 
     * @return ERSConfigurator
     */
	public static synchronized ERSConfigurator getInstance(){
		if (mInstance == null) {
			mInstance = new ERSConfigurator();
		}

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

    /**
     * Method that returns Path to OpenLS Directory Service
     * @return String
     */
    public String getOpenLSDirectoryServicePath(){
    	return mOpenLS_DS_Path;
    }
    
    /**
     * Method that returns Path to WFS
     * @return String
     */
    public String getWFSPath(){
    	return mWFS_Path;
    }

    /**
     * Method that returns the Layername of the AvoidAreas (WFS)
     * @return String
     */
    public String getWFSLayername(){
    	return mWFS_Layername;
    }
    
    /**
     * Method that returns the ColumnName of the AvoidAreas (WFS)
     * @return String
     */
    public String getWFSColumName(){
    	return mWFS_GeomColumnName;
    }
    
    /**
     * Method that returns the SRS of the AvoidAreas (WFS)
     * @return String
     */
    public String getWFSSRS(){
    	return mWFS_SRS;
    }
 
    /**
     * Method that returns the value of the extend of the bbox to get AvoidAreas (WFS)
     * @return String
     */
    public double getWFSBBoxExtend(){
    	return mWFS_BBox_Extend;
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

	/**
	 * @return the mWFS_XMLNS
	 */
	public String getWFS_XMLNS() {
		return mWFS_XMLNS;
	}
}
