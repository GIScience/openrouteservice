

package org.freeopenls.wms.getcapabilities;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.freeopenls.wms.getcapabilities.ReadOutCapabilities.LayerInfo;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;


/**
 * Class for send and read out a GetCapabilities to a WMS
 *  
 * @author Pascal Neis, pascal.neis@geoinform.fh-mainz.de
 * @version 1.0 2007-05-25
 */

/**
 * <p><b>Title: GetCapabilitiesWMS</b></p>
 * <p><b>Description:</b> Class for GetCapabilitiesWMS<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-12-10
 */
public class GetCapabilitiesWMS {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger log = Logger.getLogger(GetCapabilitiesWMS.class.getName());
	/** WMS GetCapabilities Response Document */
	private Document m_Doc;
	/** WMS Capabilites **/
	private ReadOutCapabilities m_WMSCapabilities;
	/** LayerList of WMS */
	private ArrayList<String> m_alLayers = new ArrayList<String>();
	/** SRSList of WMS */
	private ArrayList<String> m_alSRSs = new ArrayList<String>();
	/** StyleList of WMS */
	private ArrayList<String> m_alStyles = new ArrayList<String>();
	/** FormatList of WMS */
	private ArrayList<String> m_alFormats = new ArrayList<String>();

	public void doGet(String sWMSPath) {

		try {
			///////////////////////////////////////
			//*** Send Request to WMS ***
			//Send data
			//URL urlWMS = new URL("http://webmap:8080/geoserver/wms?service=WMS&request=GetCapabilities");
			URL urlWMS = new URL(sWMSPath+"?service=WMS&request=GetCapabilities");
			//System.out.println(urlWMS.toString());
			InputStream is = urlWMS.openStream();
			SAXBuilder builder = new SAXBuilder();
			m_Doc = builder.build(is);
			m_WMSCapabilities = new ReadOutCapabilities(m_Doc);
    		is.close();
			
    		//*** Formats ***
    		m_alFormats = m_WMSCapabilities.getWMSInfos().Format;
    		//*** SRSs ***
    		m_alSRSs = m_WMSCapabilities.getWMSInfos().SRS;
    		
    		//*** LayerNames and Styles ***
			ArrayList<LayerInfo> listTMP = m_WMSCapabilities.getLayerList();
			for(int i=0 ; i<listTMP.size() ; i++){
				LayerInfo layerinfoTMP = listTMP.get(i);
				m_alLayers.add(layerinfoTMP.Name);
				m_alStyles.add(layerinfoTMP.Style);
			}
		} catch (Exception e) {
			log.info("- GetCapabilites to WMS ERROR-Exception: " + e + " -");
		}
	}
	
	/**
	 * Method that returns arraylist with layernames
	 * @return arraylist with layernames
	 */
	public ArrayList<String> getLayers(){
		return m_alLayers;
	}

	/**
	 * Method that returns arraylist with stylenames
	 * @return arraylist with stylenames
	 */
	public ArrayList<String> getStyles(){
		return m_alStyles;
	}

	/**
	 * Method that returns arraylist with SRSnames
	 * @return arraylist with SRSnames
	 */
	public ArrayList<String> getSRSs(){
		return m_alSRSs;
	}

	/**
	 * Method that returns arraylist with formats
	 * @return arraylist with formats
	 */
	public ArrayList<String> getFormats(){
		return m_alFormats;
	}
}
