package heigit.ors.services.locations;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import heigit.ors.servlet.http.BaseHttpServlet;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.services.locations.requestprocessors.LocationsServiceRequestProcessorFactory;

public class LocationsServiceServlet extends BaseHttpServlet
{
	private static final long serialVersionUID = 1L;

	public void init() {
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try
		{
			AbstractHttpRequestProcessor reqProcessor = LocationsServiceRequestProcessorFactory.createProcessor(request);
			reqProcessor.process(response);
			reqProcessor.destroy();
		}
		catch(Exception ex)
		{
			writeError(response, ex);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try
		{
			AbstractHttpRequestProcessor reqProcessor = LocationsServiceRequestProcessorFactory.createProcessor(request);
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
