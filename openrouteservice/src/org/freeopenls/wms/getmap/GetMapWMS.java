

package org.freeopenls.wms.getmap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.SeverityType;

import org.apache.log4j.Logger;
import org.freeopenls.error.ServiceError;
import org.freeopenls.presentationservice.PSConfigurator;
import org.freeopenls.presentationservice.documents.RequestXLSDocument.LayerAndStyle;
import org.freeopenls.presentationservice.documents.RequestXLSDocument.OverlayADT;
import org.freeopenls.sld.SLD;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


/**
 * <p><b>Title: GetMapWMS</b></p>
 * <p><b>Description:</b>Class for GetMap WMS.<br>
 * Create the GetMap-Request(WMS) and saves the Maps.<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-05-10
 */
public class GetMapWMS {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger log = Logger.getLogger(GetMapWMS.class.getName());
	/** RSwithLandmarksConfigurator */
	private PSConfigurator mPSConfigurator;
	/** MapURL */
	private String m_sMapURL = "";
	/** Map as base64 decoded */
	private String m_sMapbase64decoded = "";

	/**
	 * Constructor - PresentationConfigurator and Sets LogLevel/Appender ; <br>
	 *  create GetMap Request and get map from WMS
	 *  
	 * @param config
	 * @param sRequestID
	 * @param sSRS
	 * @param dXLower
	 * @param dYLower
	 * @param dXUpper
	 * @param dYUpper
	 * @param boolMapTransparent
	 * @param sMapBGcolor
	 * @param sMapHeight
	 * @param sMapWidth
	 * @param sMapFormat
	 * @param alLayerNameAndStyle
	 * @param alOverlayADT
	 */
	public GetMapWMS(PSConfigurator config, String sRequestID, String sSRS, 
			double dXLower, double dYLower, double dXUpper, double dYUpper,
			boolean boolMapTransparent, String sMapBGcolor,
			String sMapHeight, String sMapWidth, String sMapFormat, ArrayList<LayerAndStyle> arraylistLayerNameAndStyle,
			ArrayList<OverlayADT> alOverlayADT)throws ServiceError{

		mPSConfigurator = config;

		///////////////////////////////////////
		//*** Create GetMap Document ***
		try{
			
			String sldRequest = SLD.createSLD(arraylistLayerNameAndStyle, alOverlayADT, 
					boolMapTransparent, sMapBGcolor, 
					sSRS, dXLower, dYLower, dXUpper, dYUpper, 
					sMapHeight, sMapWidth,
					null, null);
			
			///////////////////////////////////////
			//*** Send Request to WMS ***
			//Send data
			URL u = new URL(this.mPSConfigurator.getWMSPath());//"http://webmap:8080/geoserver/wms/GetMap");
            HttpURLConnection acon = (HttpURLConnection) u.openConnection();
            acon.setAllowUserInteraction(false);
            acon.setRequestMethod("POST");
            acon.setRequestProperty("Content-Type", "application/xml");
            acon.setDoOutput(true);
            acon.setDoInput(true);
            acon.setUseCaches(false);
            PrintWriter xmlOut = null;
            xmlOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(acon.getOutputStream())));
            xmlOut = new java.io.PrintWriter(acon.getOutputStream());
//System.out.println("Request to WMS: "+sldRequest);
            xmlOut.write(sldRequest);
            xmlOut.flush();
            xmlOut.close();

			//Get the response
            InputStream is = acon.getInputStream();

			//Save Map
            String sFileName = sRequestID+"."+sMapFormat;
            m_sMapURL = this.mPSConfigurator.getMapsPathWWW()+"/"+sFileName;
			File RouteMapFile = new File(this.mPSConfigurator.getMapsPath()+"/"+sFileName);
			FileOutputStream fos = new FileOutputStream(RouteMapFile);
			StringWriter sw = new StringWriter();
			//read every byte of the request and put it into a FileOutputStream

			byte[] buffer = new byte[8192];
			int bytesRead;
			while ( (bytesRead = is.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
				sw.write(new String(buffer, 0, bytesRead));
			}
			m_sMapbase64decoded = new String(Base64.encode(sw.toString().getBytes()));
			
            is.close();
            fos.close();

		}catch (MalformedURLException mURLe) {
			log.error(mURLe);
			ServiceError se = new ServiceError(SeverityType.ERROR);
	        se.addError(ErrorCodeType.UNKNOWN, "GetMapWMS", "URL-Problem! Message: "+mURLe);
	        throw se;
		}catch (IOException ioe) {
			log.info("GetMapWMS IOException - Message: "+ioe);
			ServiceError se = new ServiceError(SeverityType.ERROR);
	        se.addError(ErrorCodeType.UNKNOWN,
	        		"GetMapWMS", "IO-Problem! Message: "+ioe);
	        throw se;
		}
	}

	/**
	 * Method that return MapURL
	 * 
	 * @return String MapURL
	 */
	public String getMapURL(){
		return m_sMapURL;
	}

	/**
	 * Method that return Map as base64 encoded
	 * 
	 * @return String Map base64 encoded
	 */
	public String getMapbase64decoded(){
		return m_sMapbase64decoded;
	}
}
