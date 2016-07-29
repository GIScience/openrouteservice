/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package org.freeopenls.shortlinkservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.UnavailableException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.freeopenls.logger.LoggerConfig;
import org.freeopenls.routeservice.documents.instruction.InstructionLanguageTags;
import org.freeopenls.routeservice.routing.RouteProfileManager;

import de.uniBonn.geographie.rs.DirectionType;
import de.uniBonn.geographie.rs.FillWordType;
import de.uniBonn.geographie.rs.InstructionTagsDocument;
import de.uniBonn.geographie.rs.MovementType;
import de.uniBonn.geographie.rs.TimeType;
import de.uniBonn.geographie.rs.InstructionTagsDocument.InstructionTags;
import de.uniBonn.geographie.rs.LanguageDocument.Language;

public class SLConfigurator {
   
    /** RouteServiceConfigurator instance - for access from another classes */
    private static SLConfigurator mInstance = null;
	
	/** Properties for read the ConfigData from the File */
    private Properties mProperties;

    /** Logger, used to log errors(exceptions) and additionally information */
    private static Logger mLogger = Logger.getLogger(SLConfigurator.class.getName());

	
	private String mUserName = null;
	private String mPassword = null;
	private String mApiKey = null;
	
    /**
     * Constructor<br>
     * Read data from ConfigFile and sets:.<br>
     * - LogLevel, LogDIR<br>
     * - RouteGeometryParameters<br>
     * - Average Speed<br>
     * - Paths for save and access to RouteHandles<br>
     * - PostGIS Parameters Graph<br>
     * - Paths to OpenLS Services
     * 
     * <br>
     * Creates:<br>
     * - Graph<br>
     * - EdgeWeighters<br>
     * - FeatureCollection Graph<br>
     * - IndexedFeatureCollection Graph<br>
     * - HashMaps: EdgeIDtoEdge, hmEdgeIDtoStreetName<br>
     * - HashSets: EdgeIDAvoidFeatureHighway, EdgeIDAvoidFeatureTollway<br>
     * - HashMaps: CoordToNodeGraph<br>
     */
	private SLConfigurator(){
		
		try{
			///////////////////////////////////////
			//*** Open ConfigFile ***
			String fileNameProperties = "../ShortlinkService.properties.xml";
        	URL url = SLServlet.class.getClassLoader().getResource(fileNameProperties);
        	InputStream inputstream = url.openStream();
        	mProperties = loadProperties(inputstream);

			///////////////////////////////////////
			//Logging
        	String fileNameLog = mProperties.getProperty("LOGFILE");
        	URL urlLogs = SLConfigurator.class.getClassLoader().getResource(fileNameLog);
	        LoggerConfig.initLogger(new File(urlLogs.getFile()));
	        
        	mUserName = mProperties.getProperty("USER_NAME");
        	mPassword = mProperties.getProperty("USER_PASSWORD");
        	mApiKey = mProperties.getProperty("API_KEY"); 
		}
		catch (Exception e) {
			mLogger.error("- Exception in OpenLS Shortlink Service Configurator \n Message:  "+e);
		}
	}

	 
    private Properties loadProperties(InputStream is)throws UnavailableException, IOException {
        Properties properties = new Properties();
        properties.loadFromXML(is);
        return properties;
    }
    
    /**
     * Method that returns a instance of the RouteServiceConfigurator
     * 
     * @return RouteServiceConfigurator
     */
	public static synchronized SLConfigurator getInstance(){
		if (mInstance == null)
			mInstance = new SLConfigurator();
		return mInstance;
	}
    
    public String getUserName(){
    	return mUserName;
    }

    public String getPassword(){
    	return mPassword;
    }
    
    public String getApiKey(){
    	return mApiKey;
    }
}
