package org.freeopenls.servlet.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.freeopenls.tools.HTTPUtility;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple servlet filter that limits the request rate to a certain threshold
 * of requests per second. The default rate is 5 hits in 10 seconds. This can be
 * overridden in the web.xml file by adding parameters named "hits" and "period"
 * with the desired values. When the rate is exceeded, a short string is written
 * to the response output stream and the chain method is not invoked. Otherwise,
 * processing proceeds as normal.
 */
public class RequestRateThrottleFilter implements Filter {
	public class RefreshClientStatsTask extends TimerTask {

		private RequestRateThrottleFilter filter;

		public RefreshClientStatsTask(RequestRateThrottleFilter filter) {
			this.filter = filter;
		}

		public void run() {
			filter.refreshClientStats();
		}
	}

	private final Logger mLogger = Logger.getLogger(RequestRateThrottleFilter.class.getName());

	private int hits = 5;
	private int period = 10;
	private int mode = 0; // 0-ip mode; 1-session mode;
	private HashMap<String, Stack<Date>> clientStats;
	private Timer timer;
	private Set<String> localAddresses = new HashSet<String>();

	private static final String HITS = "hits";
	private static final String PERIOD = "period";
	private static final String MODE = "mode";

	/**
	 * Called by the web container to indicate to a filter that it is being
	 * placed into service. The servlet container calls the init method exactly
	 * once after instantiating the filter. The init method must complete
	 * successfully before the filter is asked to do any filtering work.
	 * 
	 * @param filterConfig
	 *            configuration object
	 * @throws ServletException
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		hits = Integer.parseInt(filterConfig.getInitParameter(HITS));
		period = Integer.parseInt(filterConfig.getInitParameter(PERIOD));
		if (filterConfig.getInitParameter(MODE) != null)
			mode = Integer.parseInt(filterConfig.getInitParameter(MODE));

		if (mode == 0) {
			clientStats = new HashMap<String, Stack<Date>>();
			TimerTask timerTask = new RefreshClientStatsTask(this);
			timer = new Timer(true);
			int interval = 30 * 60 * 1000/* 30 minutes */;
			timer.schedule(timerTask, new Date(System.currentTimeMillis() + interval), interval);
		}

		try {
			boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
					.indexOf("-agentlib:jdwp") > 0;
			if (!isDebug) {
				localAddresses.add(InetAddress.getLocalHost().getHostAddress());
				for (InetAddress inetAddress : InetAddress.getAllByName("localhost")) {
					localAddresses.add(inetAddress.getHostAddress());
				}
		}
		} catch (IOException e) {
			throw new ServletException("Unable to lookup local addresses");
		}
	}

	/**
	 * Checks to see if the current session has exceeded the allowed number of
	 * requests in the specified time period. If the threshold has been
	 * exceeded, then a short error message is written to the output stream and
	 * no further processing is done on the request. Otherwise the request is
	 * processed as normal.
	 * 
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		if (hits > 0) {
			int hitsCount = hits;
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String ip = HTTPUtility.getRemoteAddr(httpRequest);
			
			// get hits limitation from session, see UserAuthenticationContext class.
			HttpSession session = httpRequest.getSession(false);
			if (session != null)
			{
				Object objHits = session.getAttribute("user_requests_limitation");
				if (objHits != null)
				{
					hitsCount = (int)objHits;
					if (hitsCount <= 0)
					{
						chain.doFilter(request, response);
						return;
					}
				}
			}
			
			if (!localAddresses.contains(ip)) {
				if (mode == 0) {
					synchronized (clientStats) {
						Stack<Date> times = null;
						if (clientStats.containsKey(ip))
							times = clientStats.get(ip);

						if (times == null) {
							times = new Stack<Date>();
							times.push(new Date(0));
							clientStats.put(ip, times);
						}

						if (!processRequest(times, response, ip, hitsCount))
							return;
					}
				} else if (mode == 1) {
					session = httpRequest.getSession(true);

					synchronized (session.getId().intern()) {
						@SuppressWarnings("unchecked")
						Stack<Date> times = (Stack<Date>) session.getAttribute("user_requests_times");
						if (times == null) {
							times = new Stack<Date>();
							times.push(new Date(0));
							session.setAttribute("user_requests_times", times);
						}

						if (!processRequest(times, response, ip, hitsCount))
							return;
					}
				}
			}
		}

		chain.doFilter(request, response);
	}

	private void refreshClientStats() {
		synchronized (clientStats) {
			ArrayList<String> listToRemove = new ArrayList<String>();
			for (Map.Entry<String, Stack<Date>> entry : clientStats.entrySet()) {
				String key = entry.getKey();
				Stack<Date> times = entry.getValue();
				if (times.size() > 0) {
					Date latest = times.get(times.size() - 1);
					long elapsed = System.currentTimeMillis() - latest.getTime();
					if (elapsed > 60 * 60 * 1000) {
						listToRemove.add(key);
					}
				}
			}

			if (listToRemove.size() > 0) {
				for (String ip : listToRemove)
					clientStats.remove(ip);
			}
		}
	}

	private boolean processRequest(Stack<Date> times, ServletResponse response, String ip, int hitsCount) throws IOException {
		times.push(new Date());
		if (times.size() >= hitsCount) {
			times.removeElementAt(0);
		}

		Date newest = times.get(times.size() - 1);
		Date oldest = times.get(0);
		long elapsed = newest.getTime() - oldest.getTime();
		if (elapsed < period * 1000) // seconds
		{
			response.getWriter().println("Request rate is too high.");
			mLogger.info("IP: " + ip);
			return false;
		}

		return true;
	}

	/**
	 * Called by the web container to indicate to a filter that it is being
	 * taken out of service. This method is only called once all threads within
	 * the filter's doFilter method have exited or after a timeout period has
	 * passed. After the web container calls this method, it will not call the
	 * doFilter method again on this instance of the filter.
	 */
	public void destroy() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
}