package heigit.ors.servlet.filters;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.piwik.PiwikException;
import org.piwik.SimplePiwikTracker;

import heigit.ors.servlet.requests.MultiReadHttpServletRequest;
import heigit.ors.util.HTTPUtility;
import heigit.ors.util.StreamUtility;

import com.graphhopper.util.Helper;

@WebFilter("/PiwikRequestFilter")
public class PiwikRequestFilter implements Filter {

	private ServletContext m_context;
	private int m_siteID = -1;
	private String m_apiURL;
	private Set<String> localAddresses = new HashSet<String>();
	
	private static final Pattern TAG_REGEX = Pattern.compile("<xls:RoutePreference>(.+?)</xls:RoutePreference>");

	public void init(FilterConfig fConfig) throws ServletException {
		String siteID = fConfig.getInitParameter("SiteID");
		if (siteID != null) {
			m_apiURL = fConfig.getInitParameter("APIURL");
			m_siteID = Integer.parseInt(siteID);
		}
		
		m_context = fConfig.getServletContext();
		m_context.log("PiwikRequestFilter initialized");

		try {
			localAddresses.add(InetAddress.getLocalHost().getHostAddress());
			for (InetAddress inetAddress : InetAddress.getAllByName("localhost")) {
				localAddresses.add(inetAddress.getHostAddress());
			}
		} catch (IOException e) {
			//throw new ServletException("Unable to lookup local addresses");
		}
	}

	private String getRoutePreferenceType(final String str) {
		final Matcher matcher = TAG_REGEX.matcher(str);
		while (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		String ip = HTTPUtility.getRemoteAddr(req);

		if (m_siteID >= 0 && !localAddresses.contains(ip)) {
			HttpServletResponse resp = (HttpServletResponse) response;
			
			if (!(req instanceof MultiReadHttpServletRequest))
				req = new MultiReadHttpServletRequest(req);

			String strRequest = StreamUtility.readStream(req.getInputStream());

			chain.doFilter(req, resp);

			try {
				SimplePiwikTracker piwikTracker = new SimplePiwikTracker(m_siteID, m_apiURL, req);

				if (strRequest != null) {
					String routePref = getRoutePreferenceType(strRequest);
					if (routePref != null)
						piwikTracker.setPageCustomVariable("RouteType", routePref);
					String apiKey = req.getParameter("api_key");
					if (apiKey != null)
						piwikTracker.setPageCustomVariable("APIKey", apiKey);
				}

				// proxy.cgi has to include at least the following line
				// headers = {"Content-Type": os.environ["CONTENT_TYPE"],
				// "HTTP_CLIENT_IP":os.environ["REMOTE_ADDR"]}
				piwikTracker.setIp(ip);

				String urlReferer = req.getHeader("Referer");
				if (!Helper.isEmpty(urlReferer))
					piwikTracker.setUrlReferrer(urlReferer);
				if (Helper.isEmpty(req.getHeader("User-Agent")))
					piwikTracker
							.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0");

				if (Helper.isEmpty(req.getHeader("Accept-Language")))
					piwikTracker.setAcceptLanguage("en-US,en;q=0.5");

				URL url = piwikTracker.getLinkTrackURL(getFullURL(req));
				piwikTracker.sendRequest(url);

			} catch (PiwikException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			chain.doFilter(request, response);
		}
		
		return;
	}

	public static String getFullURL(HttpServletRequest request) {
		String uri = request.getScheme()
				+ "://"
				+ request.getServerName()
				+ ("http".equals(request.getScheme()) && request.getServerPort() == 80
						|| "https".equals(request.getScheme()) && request.getServerPort() == 443 ? "" : ":"
						+ request.getServerPort()) + request.getRequestURI()
				+ (request.getQueryString() != null ? "?" + request.getQueryString() : "");

		return uri;
	}

	public void destroy() {
		// close any resources here
	}
}