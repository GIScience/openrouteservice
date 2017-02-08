package org.freeopenls.servlet.filters;

import java.io.IOException;
 
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.freeopenls.servlet.requests.ByteStreamResponseWrapper;
import org.freeopenls.servlet.requests.MultiReadHttpServletRequest;
import org.freeopenls.servlet.utils.ServletUtility;
import org.freeopenls.tools.StreamUtility;
import org.freeopenls.tools.StringUtility;
 
@WebFilter("/MultipleRequestFilter")
public class MultipleRequestFilter implements Filter {
 
	private class ClientRequestContainer{
		private String m_ip;
		private ArrayList<HttpServletRequest> m_requests;
		
		public ClientRequestContainer(String ip){
		  m_ip = ip;
		  m_requests = new ArrayList<HttpServletRequest>();
		}
		
		public boolean isEmpty()
		{
			return m_requests.isEmpty();
		}
		
		public void tryAddRequest(HttpServletRequest req)
		{
			m_requests.add(req);
			
			if (m_requests.size() > 1)
			{
				 try
				    {
				      req.wait(600000);
				    }
				    catch( InterruptedException ie )
				    {
				      
				    }
			}
		}
		
		public void releaseRequest(HttpServletRequest req)
		{
			boolean res = m_requests.remove(req);
			
			if (res)
			{
				ArrayList<HttpServletRequest> toRemove = new ArrayList<HttpServletRequest>();
				for (int i = 0 ;i< m_requests.size();i++)
				{
					HttpServletRequest req2 = m_requests.get(i);
					req2.notify();
					toRemove.add(req2);
				}
				
				m_requests.removeAll(toRemove);
			}
		}
		
		public int hashCode()
		{
			return m_ip.hashCode();
		}
	}
	
    private ServletContext m_context;
    private final HashMap<String, ClientRequestContainer> m_clients = new HashMap<String, ClientRequestContainer>();
    
    public void init(FilterConfig fConfig) throws ServletException {
    	m_context = fConfig.getServletContext();
    	m_context.log("MultipleRequestFilter initialized");
    }
     
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 
    	HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
       
    	chain.doFilter(req, res);
    	// TODO Not finished yet. Runge
    	return;
     /*
        String remoteAddr = req.getRemoteAddr();
  
        MultiReadHttpServletRequest mreq = new MultiReadHttpServletRequest(req);
      //  String strRequest = StreamUtility.readStream(mreq.getInputStream());
        
        ByteStreamResponseWrapper responseWrapper = new ByteStreamResponseWrapper((HttpServletResponse)response);
        
        ClientRequestContainer cont = null;

		synchronized (m_clients) {
			cont = m_clients.get(remoteAddr);
			if (cont == null) {
				cont = new ClientRequestContainer(remoteAddr);
				cont.tryAddRequest(mreq);
				m_clients.put(remoteAddr, cont);
			} else {
				cont.tryAddRequest(mreq);
			}
		}
        
        chain.doFilter(mreq, responseWrapper);
        
        synchronized (m_clients) {
        	cont.releaseRequest(mreq);
        
        	if (cont.isEmpty())
        		m_clients.remove(remoteAddr);
        }
                
        String processedResponse = responseWrapper.toString();
        
        byte[] responseAsBytes = null;
        if (null != processedResponse)
        {
         // processedResponse = processedResponse.toUpperCase();
          responseAsBytes = processedResponse.getBytes();
        }
    
        // Writing the response (as bytes) to the servlet output stream
        ServletUtility.write(response, responseAsBytes);*/
    }
 
    public void destroy() {
        //close any resources here
    }
 }