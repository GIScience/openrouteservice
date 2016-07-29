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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import javax.servlet.*;
import javax.servlet.http.*;

import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.XLSDocument;

import org.apache.log4j.Logger;
import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.HTTPUtility;


/**
 * <p><b>Title: RESTfulServlet</b></p>
 * <p><b>Description:</b> Class for RESTfulServlet for OLS<br>
 * The servlet of the RESTful which receives the incoming HttpPost requests <br>
 * and sends the operation result documents to the client. </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-08-01
 */
public class RESTfulServlet extends HttpServlet {
	/** Serial Version UID */
	private static final long serialVersionUID = 1L;
    private static Logger mLogger = Logger.getLogger(RESTfulServlet.class.getName());
    private static Logger mLoggerCounter = Logger.getLogger(RESTfulServlet.class.getName()+".Counter");
    /** RSConfigurator **/
    private RESTfulConfigurator mRESTfulConfigurator;
    
    /**
     * Method that initialize the RESTfulServlet
     * 
     * @throws ServletException
     */
    public void init(){
		//Initialize Configurator
		mRESTfulConfigurator = RESTfulConfigurator.getInstance();
	}

    /**
     * Method that removes the RESTfulServlet from the server.
     * 
     */
    public void destroy() {
    	Logger.getRootLogger().removeAllAppenders();
    }

    /**
     * Method to service requests (GET) -<br>
     * the request will be passed to ...
     * 
     * @param request
     * 				HttpServletRequest - incoming Request
     * @param response 
     * 				HttpServletResponse - response of the incoming request
     * @throws ServletException
     * @throws IOException
     */
	public void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
        String rest = "";
        
        mLoggerCounter.info("Post ; "+HTTPUtility.getRemoteAddr(request)+" ; ; "+mRESTfulConfigurator.getServiceName());
		long requestTime = System.currentTimeMillis();

        if(request.getPathInfo() != null && request.getPathInfo().contains("route/")){
            //Decode the application/x-www-form-url encoded query string
            rest = java.net.URLDecoder.decode(request.getPathInfo(), "ISO-8859-1");//"UTF-8");
//System.out.println("REST Request: "+rest);
           
            //Log REST request
    		mLoggerCounter.info(" REST ; ; ; ; "+rest+" ; ");
    		
            Pattern pattern = Pattern.compile( "[/]" );
	    	String[] restArray = pattern.split(rest);
            String routePreference = "Car";

            if(restArray.length < 3){
				ServiceError se = new ServiceError(SeverityType.ERROR);
				se.addError(ErrorCodeType.NOT_SUPPORTED, "Request","Not enough parameters ...");
            	doResponse(response, new ResponseXLSDocument(se.getErrorListXLSDocument("")));
            	return;
            }
            else if(restArray.length > 5){
				ServiceError se = new ServiceError(SeverityType.ERROR);
				se.addError(ErrorCodeType.NOT_SUPPORTED, "Request","To much paraameters ...");
            	doResponse(response, new ResponseXLSDocument(se.getErrorListXLSDocument("")));
            	return;
            }
            else if(restArray.length >= 4){
				routePreference = restArray[4];
            }

  
			//*** Request to WFS ***
			String routeRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
										"<xls:XLS xmlns:xls=\"http://www.opengis.net/xls\" xmlns:sch=\"http://www.ascc.net/xml/schematron\"\n" +
										" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
										" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
										" xsi:schemaLocation=\"http://www.opengis.net/xls http://schemas.opengis.net/ols/1.1.0/RouteService.xsd\" version=\"1.1\" xls:lang=\"en\">\n" +
										" <xls:RequestHeader/>\n" +
										"<xls:Request methodName=\"RouteRequest\" requestID=\"123456789\" version=\"1.1\">\n" +
										"	<xls:DetermineRouteRequest distanceUnit=\"KM\">\n" +
										"		<xls:RoutePlan>\n" +
										"			<xls:RoutePreference>"+routePreference+"</xls:RoutePreference>\n" +
										"				<xls:WayPointList>\n" +
										"					<xls:StartPoint>\n" +
										"						<xls:Position>\n" +
										"							<gml:Point srsName=\"EPSG:4326\">\n" +
										"								<gml:pos>"+restArray[2]+"</gml:pos>\n" +
										"							</gml:Point>\n" +
										"						</xls:Position>\n" +
										"					</xls:StartPoint>\n" +
										"					<xls:EndPoint>\n" +
										"						<xls:Position>\n" +
										"							<gml:Point srsName=\"EPSG:4326\">\n" +
										"								<gml:pos>"+restArray[3]+"</gml:pos>\n" +
										"							</gml:Point>\n" +
										"						</xls:Position>\n" +
										"					</xls:EndPoint>\n" +
										"				</xls:WayPointList>\n" +
										"			</xls:RoutePlan>\n" +
										"			<xls:RouteInstructionsRequest/>\n" +
										"			<xls:RouteGeometryRequest/>\n" +
										"			<xls:RouteMapRequest>\n" +
										"				<xls:Output format=\"kml\" height=\"400\" width=\"400\" BGcolor=\"#ffffff\"/>\n" +
										"			</xls:RouteMapRequest>\n" +
										"		</xls:DetermineRouteRequest>\n" +
										"	</xls:Request>\n" +
										"</xls:XLS>";

			// Send data
			URL u = new URL(mRESTfulConfigurator.getOpenLSRouteServicePath());
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
//System.out.println("Request: "+routeRequest);
            xmlOut.write(routeRequest);
            xmlOut.flush();
            xmlOut.close();

			// Get the response
            InputStream is = acon.getInputStream();

			//Create New XLSDoc with InputStream
			XLSDocument xlsNEW = null;
			try {
				xlsNEW = XLSDocument.Factory.parse(is);
			} catch (Exception e) {
				mLogger.error(e);
			}
			is.close();
			
			doResponse(response, new ResponseXLSDocument(xlsNEW));
        }
        else if(request.getPathInfo() != null && request.getPathInfo().contains("geocode/")){
            //Decode the application/x-www-form-url encoded query string
            rest = java.net.URLDecoder.decode(request.getPathInfo(), "ISO-8859-1");//"UTF-8");
//System.out.println("REST Request: "+rest);
           
            //Log REST request
    		mLoggerCounter.info(" REST ; ; ; ; "+rest+" ; ");
    		
        	Pattern pattern = Pattern.compile( "[/]" );
	    	String[] restArray = pattern.split(rest);
	    	            
            //localhost:8080/RESTfulOpenLS/geocode/reverse/7.099 50.7793
  
			//*** Request to WFS ***
			String lusRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "+
									"<xls:XLS xmlns:xls=\"http://www.opengis.net/xls\"" +
									" xmlns:sch=\"http://www.ascc.net/xml/schematron\" xmlns:gml=\"http://www.opengis.net/gml\"" +
									" xmlns:xlink=\"http://www.w3.org/1999/xlink\"" +
									" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
									" xsi:schemaLocation=\"http://www.opengis.net/xls LocationUtilityService.xsd\" version=\"1.1\">" +
									"<xls:RequestHeader/>";
			//decide on normal or reverse geocoding using URL
			if (restArray[2].equals("reverse") ){				
				   lusRequest += "<xls:Request methodName=\"ReverseGeocodeRequest\" requestID=\"123456789\" version=\"1.1\">" +
									"<xls:ReverseGeocodeRequest>"+
										"<xls:Position>"+
											"<gml:Point srsName=\"EPSG:4326\">"+
												"<gml:pos>"+ restArray[3] +"</gml:pos>"+
											"</gml:Point>"+
										"</xls:Position>"+
									"</xls:ReverseGeocodeRequest>";							
			}else{
				 lusRequest +="<xls:Request methodName=\"GeocodeRequest\" requestID=\"123456789\" version=\"1.1\">" +
								"<xls:GeocodeRequest>" +
									"<xls:Address countryCode=\"DE\">" +
										"<xls:freeFormAddress>"+ restArray[2] +"</xls:freeFormAddress>" +
									"</xls:Address>" +
								"</xls:GeocodeRequest>" ;
			}
			
			lusRequest += 	"</xls:Request>" +
								  "</xls:XLS>";
			// Send data
			URL u = new URL(mRESTfulConfigurator.getOpenLSLocationUtilityServicePath());
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
//System.out.println("Request: "+lusRequest);
            xmlOut.write(lusRequest);
            xmlOut.flush();
            xmlOut.close();

			// Get the response
            InputStream is = acon.getInputStream();

			//Create New XLSDoc with InputStream
			XLSDocument xlsNEW = null;
			try {
				xlsNEW = XLSDocument.Factory.parse(is);
			} catch (Exception e) {
				mLogger.error(e);
			}
			is.close();
			
			doResponse(response, new ResponseXLSDocument(xlsNEW));
        	
        }
        else{
        	//TODO
        }

		long responseTime = System.currentTimeMillis();
		long handlingTime = responseTime-requestTime;
		double handlingTimeSeconds = (double)handlingTime/1000;

		mLoggerCounter.info("Finish ; "+request.getRemoteAddr()+" ; "+Double.toString(handlingTimeSeconds).replace(".",",")+" ; "+mRESTfulConfigurator.getServiceName());
	}
    
    /**
     * Method to service requests. (POST) - <br>
     * the request will be passed to ...
     * 
     * @param request
     * 				HttpServletRequest - incoming Request
     * @param response 
     * 				HttpServletResponse - response of the incoming request
     * @throws ServletException
     * @throws IOException
     */
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
			//TODO ???
	}

    /**
     * Method to service response. - <br>
     * writes the content of the Response to the OutputStream 
     * of the HttpServletResponse
     * 
     * @param servletresponse
     * @param routeServiceResponse
     */
    public void doResponse(HttpServletResponse servletresponse, ResponseXLSDocument routeServiceResponse) {
        try {
            String contentType = routeServiceResponse.getContentType();
            int contentLength = routeServiceResponse.getContentLength();
            byte[] bytes = routeServiceResponse.getByteArray();
            servletresponse.setContentLength(contentLength);
            OutputStream out = servletresponse.getOutputStream();
            servletresponse.setContentType(contentType);
            //log.info("doResponse");
            out.write(bytes);
            out.close();
        } catch (IOException ioe) {
        	mLogger.error(ioe);
        }
    }
}
