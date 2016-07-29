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

// Authors: M. Rylov
package org.freeopenls.servlet.filters;

import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.freeopenls.servlet.requests.MultiReadHttpServletRequest;
import org.freeopenls.tools.HTTPUtility;

import com.graphhopper.util.Helper;

import java.util.HashSet;
import java.util.Set;

public class UserAuthenticationFilter implements Filter {
	private ServletContext m_servletContext;
	private UserAuthenticationContext m_userMangementCntx;
	private Set<String> localAddresses = new HashSet<String>();
	
    public UserAuthenticationFilter()
	{
		try {
			boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
				    getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
            if (!isDebug)
            {
            	localAddresses.add(InetAddress.getLocalHost().getHostAddress());
            	for (InetAddress inetAddress : InetAddress.getAllByName("localhost")) {
            		localAddresses.add(inetAddress.getHostAddress());
            	}
            }
		} catch (IOException e) {
			
		}
	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		m_servletContext = fConfig.getServletContext();
		
	    try {
	      String pathDatabase = fConfig.getInitParameter("DATABASE_PATH");
	      if (Helper.isEmpty(pathDatabase))
	    	  pathDatabase = "ORS_Users.db";

	        Boolean logStats = Helper.isEmpty(fConfig.getInitParameter("LOG_STATS")) ?  true : fConfig.getInitParameter("LOG_STATS") == "true";
	    	m_userMangementCntx = new UserAuthenticationContext(pathDatabase, logStats,  m_servletContext);
	    } catch ( Exception e ) {
	    	m_servletContext.log("Unable to connect the database.");
	    }
	    
	    m_servletContext.log("AuthenticationFilter initialized");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, 
			ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		
		if (m_userMangementCntx != null) {
			String userIp = HTTPUtility.getRemoteAddr(req);

			if (!localAddresses.contains(userIp)) 
			{
				if (!(req instanceof MultiReadHttpServletRequest))
					req = new MultiReadHttpServletRequest(req);
				
				String apiKey = req.getParameter("apikey");
				if (!Helper.isEmpty(apiKey)) {
					boolean isValidUser = m_userMangementCntx.isValidUser(req, apiKey, userIp, true);

					if (!isValidUser) {
						response.getWriter().println("Your request was declined. The APIKey is either incorrect or expired.");
						return;
					} else {
						m_userMangementCntx.logRequest(req);
					}
				}
			}
		}

		chain.doFilter(req, response);
	}
		
	@Override
	public void destroy() {
		if (m_userMangementCntx != null)
		{
			try {
				m_userMangementCntx.destroy();
				m_userMangementCntx = null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
}
