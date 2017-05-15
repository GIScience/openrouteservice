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

import heigit.ors.servlet.requests.StatusCodeCaptureWrapper;

public class StatusCodeHandlerFilter implements Filter {

	public StatusCodeHandlerFilter() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		StatusCodeCaptureWrapper responseWrapper = new StatusCodeCaptureWrapper((HttpServletRequest)request, (HttpServletResponse)response);
		Throwable exception = null;

		try {
			chain.doFilter(request, responseWrapper);
		} catch (ServletException e) {
			exception = e.getRootCause();
		} catch (Throwable e) { // NOSONAR this is an UnhandledExceptionHandler - we need to catch this
			exception = e;
		}

		// 	flush to prevent servlet container to add anymore  headers or content
		response.flushBuffer();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}
}
