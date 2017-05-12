package heigit.ors.servlet.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CompressionFilter implements Filter 
{
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException 
	{
		if (req instanceof HttpServletRequest)
		{
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;
			String acceptEncoding = request.getHeader("accept-encoding");
			
			if (acceptEncoding != null) {
				if (acceptEncoding.indexOf("gzip") != -1) {
					GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(response);
					chain.doFilter(req, wrappedResponse);
					wrappedResponse.finishResponse();
					return;
				}
				else if (acceptEncoding.indexOf("deflate") != -1) {
                   // todo
				}
			}

			chain.doFilter(req, res);
		}
	}

	public void init(FilterConfig filterConfig) {

	}

	public void destroy() {

	}
}
