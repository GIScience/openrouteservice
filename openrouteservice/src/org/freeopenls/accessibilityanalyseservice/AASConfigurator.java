/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.accessibilityanalyseservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.UnavailableException;

import org.apache.log4j.Logger;
//import org.freeopenls.database.ConnectionManager;
//import org.freeopenls.database.ConnectionParameter;
//import org.freeopenls.graph.GraphManager;
import org.freeopenls.logger.LoggerConfig;

import com.graphhopper.util.Helper;


/**
 * <p><b>Title: AASConfigurator</b></p>
 * <p><b>Description:</b> Class to configure the AAS Servlet <br>
 * Read the ConfigData from "config.properties.xml" and create ..... see Constructor!!!</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 2.0 2006-11-28
 */
public class AASConfigurator {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static Logger mLogger = Logger.getLogger(AASConfigurator.class.getName());

	/** AccessibilityConfigurator instance - for access from another classes */
	private static AASConfigurator mInstance = null;

	/** OpenLS Presentation Parameters */
	private String mOpenLS_PS_Path = "";
	private ArrayList<String> mOpenLS_PS_LayerNames = new ArrayList<String>();
	/** Name of Styles which should be used */
	private ArrayList<String> mOpenLS_PS_StyleNames = new ArrayList<String>();
	/** Color of the Accessibility Polygon **/
	private String sColorAccessibilityPoly = "";
	
	/** AnalyseMap Paths */
	private String sMapPath = "";
	private String sMapPathWWW = "";

	/** Graph SRSName **/
	private String sGraphSRSName = "EPSG:4326";
	
	/** Min and Max Analysis Values in Seconds **/
	private int iMinAnalysisValue = 0;
	private int iMaxAnalysisValue = 0;
	
	private double dGridSize = 200;
	
	/** Maximum Distance for search a Node to a Location, in Meters [m] **/
	private double dMaxSearchDistanceNode = 0;
	
	/** Properties for read the ConfigData from the File */
	private Properties mProperties;

	/** Service Name */
	private String mServiceName = null;


	/**
	 * Constructor
	 */
	public AASConfigurator() {
		try {
			///////////////////////////////////////
			//*** Open ConfigFile ***
			String fileNameProperties = "../AccessibilityAnalysisService.properties.xml";
        	URL url = AASConfigurator.class.getClassLoader().getResource(fileNameProperties);
        	InputStream inputstream = url.openStream();
        	mProperties = loadProperties(inputstream);

			///////////////////////////////////////
			//Logging
        	String fileNameLog = mProperties.getProperty("LOGFILE");
        	URL urlLogs = AASConfigurator.class.getClassLoader().getResource(fileNameLog);
	        LoggerConfig.initLogger(new File(urlLogs.getFile()));

	        //Service Name
	        mServiceName = mProperties.getProperty("SERVICENAME");
	        
			mLogger.info("***********************************************************************************");
			mLogger.info("* * Accessibility Analyse Service Configurator - ServiceName: "+mServiceName+" *");
			
			///////////////////////////////////////
			//Parameter to connect OpenLS Presentation Service
			mOpenLS_PS_Path = mProperties.getProperty("OPENLS_PS");
			//Layer Names
			String sTMPLayers = mProperties.getProperty("OPENLS_PS_LAYERS");
			while(sTMPLayers.indexOf(",") > 0){
				mOpenLS_PS_LayerNames.add( sTMPLayers.substring(0, sTMPLayers.indexOf(",")) );
				sTMPLayers = sTMPLayers.substring(sTMPLayers.indexOf(",")+1, sTMPLayers.length());
				if(sTMPLayers.indexOf(",") < 0)
					mOpenLS_PS_LayerNames.add( sTMPLayers );
			}
			if(mOpenLS_PS_LayerNames.size() == 0){
				mOpenLS_PS_LayerNames.add(sTMPLayers);
			}
			//Style Names
			String sTMPStyles = mProperties.getProperty("OPENLS_PS_STYLES");
			while(sTMPStyles.indexOf(",") > 0){				
				mOpenLS_PS_StyleNames.add( sTMPStyles.substring(0, sTMPStyles.indexOf(",")) );
				sTMPStyles = sTMPStyles.substring(sTMPStyles.indexOf(",")+1, sTMPStyles.length());
				if(sTMPStyles.indexOf(",") < 0)
					mOpenLS_PS_StyleNames.add( sTMPStyles );
			}
			if(mOpenLS_PS_StyleNames.size() == 0){
				mOpenLS_PS_StyleNames.add(sTMPStyles);
			}

			//Color of the Accessibility Polygon
			this.sColorAccessibilityPoly = mProperties.getProperty("COLOR_ACCESSIBILITY_POLYGON");

			///////////////////////////////////////
			//Paths for save and access to RouteMaps and RouteHandles
			this.sMapPath = mProperties.getProperty("MAPS_PATH");
			this.sMapPathWWW = mProperties.getProperty("MAPS_PATH_WWW");
			
			///////////////////////////////////////
			//Min and Max Analysis Values in Seconds
			this.iMinAnalysisValue = Integer.parseInt(mProperties.getProperty("MIN_VALUE"));
			this.iMaxAnalysisValue = Integer.parseInt(mProperties.getProperty("MAX_VALUE"));
			
			String propValue = mProperties.getProperty("GRID_SIZE");
			if (!Helper.isEmpty(propValue))
				dGridSize = Math.max(50, Integer.parseInt(propValue));

			///////////////////////////////////////
			//Maximum Distance for search a Node to a Location, in Meters [m]
			this.dMaxSearchDistanceNode = Double.parseDouble( mProperties.getProperty("MAX_SEARCH_DISTANCE_NODE") );
				
			mLogger.info("*  OpenLS Presentation Service Path: "+mOpenLS_PS_Path);
			mLogger.info("*  Config-File loaded:  s u c c e s s f u l l y ");
			mLogger.info("***********************************************************************************");

		} catch (Exception e) {
			mLogger.error(e);
		}

	}

	/**
	 * Method that returns a instance of the AccessibilityConfigurator
	 * 
	 * @return AccessibilityConfigurator
	 */
	public static synchronized AASConfigurator getInstance() {
		if (mInstance == null) {
			mInstance = new AASConfigurator();
		}
		return mInstance;
	}

    /**
     * Method that returns Path to OpenLS Presentation Service
     * @return String
     */
    public String getOpenLSPresentationServicePath(){
    	return mOpenLS_PS_Path;
    }

    /**
     * Method that returns ArrayList with Layer Names for PortrayMap Request OpenLS Presentation Service
     * @return ArrayList
     */
    public ArrayList<String> getOpenLSPSLayers(){
    	return mOpenLS_PS_LayerNames;
    }

    /**
     * Method that returns ArrayList with Style Names for PortrayMap Request OpenLS Presentation Service
     * @return ArrayList
     */
    public ArrayList<String> getOpenLSPSStyles(){
    	return mOpenLS_PS_StyleNames;
    }

    /**
     * Method that returns the Color of the Accessbility Polygon
     * @return ArrayList
     */
    public String getColorAccessbilityPolygon(){
    	return this.sColorAccessibilityPoly;
    }

    /**
     * Method that returns Path for save the Maps
     * @return String
     */
    public String getMapsPath(){
    	return this.sMapPath;
    }

    /**
     * Method that returns Path for access the Maps
     * @return String
     */
    public String getMapsPathWWW(){
    	return this.sMapPathWWW;
    }

    /**
     * Method that returns Graph SRS
     * @return String
     */
    public String getGraphSRS(){
    	return this.sGraphSRSName;
    }

    /**
     * Method that returns Minimum Analysis Value
     * @return int
     */
    public int getMinAnalysisValue(){
    	return this.iMinAnalysisValue;
    }

    /**
     * Method that returns Maximum Analysis Value
     * @return int
     */
    public int getMaxAnalysisValue(){
    	return this.iMaxAnalysisValue;
    }

    /**
     * Method that returns the Maximum Distance for 
     * search a Node to a Location, in Meters [m].
     * 
     * @return double dMaxSearchDistanceNode
     */
    public double getMaxSearchDistanceNode(){
    	return this.dMaxSearchDistanceNode;
    }
    
    public double getGridSize()
    {
    	return dGridSize;
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
	 * @return properties
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
