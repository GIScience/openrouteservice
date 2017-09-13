package heigit.ors.services.isochrones;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import heigit.ors.services.isochrones.requestprocessors.IsochronesServiceRequestProcessorFactory;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.http.BaseHttpServlet;

public class IsochronesServiceServlet extends BaseHttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init() {
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException 
	{
		try
		{
			AbstractHttpRequestProcessor reqProcessor = IsochronesServiceRequestProcessorFactory.createProcessor(request);
			reqProcessor.process(response);
			reqProcessor.destroy();
		}
		catch(Exception ex)
		{
			writeError(response, ex);
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException{
		try
		{
			AbstractHttpRequestProcessor reqProcessor = IsochronesServiceRequestProcessorFactory.createProcessor(request);
			reqProcessor.process(response);
			reqProcessor.destroy();
		}
		catch(Exception ex)
		{
			writeError(response, ex);
		}
	}

    public void destroy() {
    	
    }
}
