/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.routeservice;

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

/**
 * <p><b>Title: RSConfigurator</b></p>
 * <p><b>Description:</b> Class to configure the RouteService Servlet <br>
 * Read the ConfigData from "config.properties.xml" and create ..... see Constructor!!!</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-07-24
 * @version 1.1 2008-04-21
 */
public class RSConfigurator {
   
    /** RouteServiceConfigurator instance - for access from another classes */
    private static RSConfigurator mInstance = null;
	
	/** Properties for read the ConfigData from the File */
    private Properties mProperties;

    /** Logger, used to log errors(exceptions) and additionally information */
    private static Logger mLogger = Logger.getLogger(RSConfigurator.class.getName());

	/** Route Geometry Parameter */
	/** double value for devide the scale parameter to get a flatness value for generalize the calculated route*/
//TODO
	private double m_dDivScaleValue = 0;
	
	/** temp and www paths */
	private String mTempPath = "";
	private String mPathWWW = "";
	
	/** int value for radius to find an Edge/Street around the Point/Position */
	private double mPointRadiusEdge = 0;
	
	/** OpenLS Location Utility Service Parameters */
	private String mOpenLS_LUS_Path = "";
	/** OpenLS Directory Service Parameters */
	private String mOpenLS_DS_Path = "";
	
	/** OpenLS Presentation Parameters */
	private String mOpenLS_PS_Path = "";
	private ArrayList<String> mOpenLS_PS_LayerNames = new ArrayList<String>();
	/** Name of Styles which should be used */
	private ArrayList<String> mOpenLS_PS_StyleNames = new ArrayList<String>();

	/** RouteInstructions WordTags **/
	private HashMap<String, InstructionLanguageTags> mLanguageCode2InstructionTags = new HashMap<String, InstructionLanguageTags>();
	
	/** Service Name */
	private String mServiceName = null;
	
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
	private RSConfigurator(){
		
		try{
			///////////////////////////////////////
			//*** Open ConfigFile ***
			String fileNameProperties = "../RouteService.properties.xml";
        	URL url = RSServlet.class.getClassLoader().getResource(fileNameProperties);
        	InputStream inputstream = url.openStream();
        	mProperties = loadProperties(inputstream);

			///////////////////////////////////////
			//Logging
        	String fileNameLog = mProperties.getProperty("LOGFILE");
        	URL urlLogs = RSConfigurator.class.getClassLoader().getResource(fileNameLog);
	        LoggerConfig.initLogger(new File(urlLogs.getFile()));
	        
	        //Service Name
	        mServiceName = mProperties.getProperty("SERVICENAME");
	        
			mLogger.info("***********************************************************************************");
			mLogger.info("* * OpenLS Route Service Configurator - ServiceName: "+mServiceName+" *");

			///////////////////////////////////////
			//RouteGeometryParameters
			m_dDivScaleValue = Double.parseDouble(mProperties.getProperty("ROUTEGEOM_SCALE"));

			///////////////////////////////////////
			//Paths for save and access to the RouteHandles
			mTempPath = mProperties.getProperty("TMP_PATH");
			mPathWWW = mProperties.getProperty("WWW_PATH");

			///////////////////////////////////////
			//Radius to find Edge/Street around Point/Position
			mPointRadiusEdge = Double.parseDouble(mProperties.getProperty("POINT_RADIUS_EDGE"));

			///////////////////////////////////////
			//Parameter to connect OpenLS Location Utility Service
			mOpenLS_LUS_Path = mProperties.getProperty("OPENLS_LUS");
			
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
			
			///////////////////////////////////////
			//Parameter to connect OpenLS Directory Service
			mOpenLS_DS_Path = mProperties.getProperty("OPENLS_DS");

			///////////////////////////////////////
			//HashMap Languagecode To InstructionTags 
			mLanguageCode2InstructionTags = loadRouteInstructionTags();
			
			//Initialize GraphManager
			RouteProfileManager.getInstance();

			mLogger.info("*  OpenLS Presentation Service Path: "+mOpenLS_PS_Path);
			mLogger.info("*   LayerNames: >"+mOpenLS_PS_LayerNames+"< StyleNames: >"+mOpenLS_PS_StyleNames+"<");
			mLogger.info("*  OpenLS Location Utility Service Path: "+mOpenLS_LUS_Path);
			mLogger.info("*  OpenLS Directory Service Path: "+mOpenLS_DS_Path);
			mLogger.info("*  Config-File loaded:  s u c c e s s f u l l y ");
			mLogger.info("***********************************************************************************");
		}
		catch (Exception e) {
			mLogger.error("- Exception in OpenLS Route Service Configurator \n Message:  "+e);
		}
	}

    /**
     * Method that returns a instance of the RouteServiceConfigurator
     * 
     * @return RouteServiceConfigurator
     */
	public static synchronized RSConfigurator getInstance(){
		if (mInstance == null)
			mInstance = new RSConfigurator();
		return mInstance;
	}
    
    /**
     * Method that returns the value for division the scale for generalize the calculated route
     * 
     * @return double
     */
    public double getRouteGeomDivScaleValue(){
    	return m_dDivScaleValue;
    }

    /**
     * Method that returns path to save files temp
     * @return String
     */
    public String getTempPath(){
    	return mTempPath;
    }

    /**
     * Method that returns path for temp file over WWW
     * @return String
     */
    public String getWWWPath(){
    	return mPathWWW;
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
     * Method that return radius to find Edge/Street around Point/Position
     * @return double
     */
    public double getPointRadiusEdge(){
    	return mPointRadiusEdge;
    }

    /**
     * Method that returns Path to OpenLS Location Utility Service
     * @return String
     */
    public String getOpenLSLocationUtilityServicePath(){
    	return mOpenLS_LUS_Path;
    }
    
    /**
     * Method that returns Path to OpenLS Presentation Service
     * @return String
     */
    public String getOpenLSPresentationServicePath(){
    	return mOpenLS_PS_Path;
    }

    /**
     * Method that returns Path to OpenLS Directory Service
     * @return String
     */
    public String getOpenLSDirectoryServicePath(){
    	return mOpenLS_DS_Path;
    }

    /**
     * Method that returns Hashmap for languagecode to instructionstags
     * @return String
     */
    public HashMap<String,InstructionLanguageTags> getHashMapLanguageCodeToInstrunsTags(){
    	return mLanguageCode2InstructionTags;
    }
 
    private Properties loadProperties(InputStream is)throws UnavailableException, IOException {
        Properties properties = new Properties();
        properties.loadFromXML(is);
        return properties;
    }
    
    /**
     * Method that read language.xml file
     * 
     * @return HashMap<String,InstructionLangTags>
     * @throws ServiceError
     */
    private HashMap<String,InstructionLanguageTags> loadRouteInstructionTags(){
    	HashMap<String, InstructionLanguageTags> hmRouteInstructionLanguages = new HashMap<String, InstructionLanguageTags>();
    	String languageInfo = "";
    	
    	try{
			//Read Languages from file
	        File file = new File("../RouteService.languages.xml");
	        InputStream inputstream;
	        
	        if(file.exists())
	        	inputstream = new FileInputStream(file);
	        else{
	        	URL url = RSServlet.class.getClassLoader().getResource(file.toString());
		        inputstream = url.openStream();
	        }

	        //*** Read languages.xml File ***
	        InstructionTagsDocument InstrucTagsDoc = (InstructionTagsDocument) XmlObject.Factory.parse(inputstream);
			InstructionTags InstrucTags = InstrucTagsDoc.getInstructionTags();
	        Language[] lang = InstrucTags.getLanguageArray();
			
			for(int i=0 ; i<lang.length ; i++){
								
				//** Attributes **
				String sLanguage = lang[i].getDescription();
				languageInfo += sLanguage+"; ";
				String sLangCode = lang[i].getCode();
				
				//** Elements **
				//MobilityBasedMovementInstruction
				MovementType movetype = lang[i].getMobilityBasedMovementInstruction();
				String sDrive = movetype.getVehicle();
				String sGo = movetype.getPedestrian();
				
				//Time
				TimeType time = lang[i].getTime();
				String sApprox = time.getAppoximation();
				String sDay = time.getDay();
				String sHour = time.getHour();
				String sMinute = time.getMinute();
				String sSecond = time.getSecond();
				
				//Direction
				DirectionType direction = lang[i].getDirection();
				String sTurn = direction.getCurve(); 
				String sHalfLeft = direction.getHalfLeft();
				String sHalfRight = direction.getHalfRight();
				String sSharpLeft = direction.getSharpLeft();
				String sLeft = direction.getLeft();
				String sRight = direction.getRight();
				String sSharpRight = direction.getSharpRight();
				String sStraightForward = direction.getStraightForward();
				String sInitialHeading = direction.getInitialHeading();
				String sRoundabout = direction.getRoundabout();
				
				//ActionNumber
				String sActionNr = lang[i].getActionNumber();
				
				//FillWords
				FillWordType fillword = lang[i].getFillWord();
				String sStart = fillword.getStartTag();
				String sFinish = fillword.getEndTag();
				String sFor = fillword.getFor();
				String sOn = fillword.getOn();
				String sBefore = fillword.getBefore();
				String sAfter = fillword.getAfter();
				
				InstructionLanguageTags instructag = new InstructionLanguageTags(sLangCode, sLanguage,
						sActionNr, sStart, sFinish, sDrive, sGo, sTurn,
						sStraightForward, sLeft, sSharpLeft, sHalfLeft, sRight, sSharpRight, sHalfRight, sInitialHeading,sRoundabout,
						sApprox, sDay, sHour, sMinute, sSecond,
						sOn, sFor, sBefore, sAfter);
				hmRouteInstructionLanguages.put(sLangCode, instructag);
			}

		}catch (FileNotFoundException fnfe) {
            mLogger.error("loadRouteInstructionTags() - FileNotFoundException: languages.xml File not found. " + fnfe.getMessage());
		}catch (XmlException xmle) {
            mLogger.error("loadRouteInstructionTags() - FileNotFoundException: languages.xml File not found. " + xmle.getMessage());
		}
		catch (IOException e) {
            mLogger.error("loadRouteInstructionTags() - IOException: " + e.getMessage());
	    }
    	
		mLogger.info("*  Languages: "+languageInfo);
    	return hmRouteInstructionLanguages;
    }

	/**
	 * @return the ServiceName
	 */
	public String getServiceName() {
		return mServiceName;
	}
}
