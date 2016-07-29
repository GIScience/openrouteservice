
package org.freeopenls.connector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.XLSDocument;

import org.apache.xmlbeans.XmlException;
import org.freeopenls.error.ServiceError;

/**
 * <p><b>Title: WebServiceConnector</b></p>
 * <p><b>Description:</b> Class for send a XML/XLS Request to an OpenLS Service / or other Web Service<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-07-24
 * @version 1.1 2008-04-21
 */
public class WebServiceConnector {

	/**
	 * Method that send a Request to a URL (Web Service)
	 * 
	 * @param url
	 * @param request
	 * @return XLSDocument
	 * @throws ServiceError
	 */
	public XLSDocument connect(String url, String request)throws ServiceError{
		XLSDocument xlsResponse;
		
		try{
			// Send data
			URL u = new URL(url);
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
            xmlOut.write(request);
            xmlOut.flush();
            xmlOut.close();

			// Get the response
            InputStream is = acon.getInputStream();
	
			//Create New XLSDoc with InputStream
			xlsResponse = XLSDocument.Factory.parse(is);
			is.close();
			
		}catch(MalformedURLException mURLe){
			ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.UNKNOWN,
            		"Connection to: "+url,
            		"Problem with the Connection to the Service: "+url+" - Message: "+mURLe);
            throw se;
		}
		catch(IOException ioe){
			ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.UNKNOWN,
            		"Connection to: "+url,
                    "Problem with the Connection to the Service: "+url+" - Message: "+ioe);
            throw se;
		}
		catch(XmlException xmle){
			ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.UNKNOWN,
            		"Connection to: "+url,
                    "Problem with the Connection to the Service: "+url+" - Message: "+xmle);
            throw se;
		}
		
		return xlsResponse;
	}
}
