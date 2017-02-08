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
package heigit.ors.servlet.filters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.graphhopper.util.Helper;

public class UserAuthenticationContext
{
	public class SessionListener implements HttpSessionListener {

		private UserAuthenticationContext m_context;
		public SessionListener(UserAuthenticationContext cntx)
		{
			m_context = cntx;
		}
		
	    @Override
	    public void sessionCreated(HttpSessionEvent event) {
	        
	    }
	 
	    @Override
	    public void sessionDestroyed(HttpSessionEvent event) {
	    	HttpSession session = event.getSession();
	    	m_context.endSession(session);
	    }
	}
	
	private class UserInfo
	{
		private int userId;
		private String apiKey;
		private ArrayList<String> ipAddresses;
		private String referer;
		private int sessions;
		private int requests = 0;
		private int requestsLimit = 0;
		private Date expirationDate;
		private Date sessionBegin;
		
		public UserInfo(int userId, String apiKey, Collection<String> ipAddressses, String referer, Date expirationDate, int requestsLimit)
		{
			this.userId = userId;
			this.apiKey = apiKey;
			if (ipAddressses != null)
				this.ipAddresses =  new ArrayList<>(ipAddresses);
			this.referer = referer;
			this.expirationDate = expirationDate;
			this.sessionBegin = new Date(System.currentTimeMillis());
			this.requestsLimit = requestsLimit;
		}
		
		public void resetSessionBeginTime()
		{
			this.sessionBegin = new Date(System.currentTimeMillis());
		}
		
		public Boolean accessIsExpired()
		{
			if (this.expirationDate != null)
				return this.expirationDate.before(new Date(System.currentTimeMillis()));
			else
				return false;
		}
		
		public Boolean isIpAddressAllowed(String ip)
		{
			if (ipAddresses != null)
			{
				return ipAddresses.contains(ip);
			}
			
			return false;
		}
		
		public void setIpAddresses(Collection<String> ipAddresses)
		{
			if (this.ipAddresses == null)
				this.ipAddresses = new ArrayList<>(ipAddresses);
			else
				this.ipAddresses.addAll(ipAddresses);
		}
		
		public void setReferer(String referer)
		{
			this.referer = referer;
		}
		
		public String getReferer()
		{
			return this.referer;
		}
	}
	
	private Connection m_connection;
	private HashMap<String, UserInfo> m_activeUsers;
	private List<String> m_invalidApiKeys;
	private boolean m_logStatistics;
	private ServletContext m_servletContext;
	
	public UserAuthenticationContext(String dbName, Boolean logStatisits, ServletContext servletContext) throws ClassNotFoundException, SQLException
	{
		m_connection = null;
		Class.forName("org.sqlite.JDBC");

		m_connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);

		createDatabase(m_connection);

		m_logStatistics = logStatisits;
		m_activeUsers = new HashMap<String, UserAuthenticationContext.UserInfo>();
		m_invalidApiKeys = new ArrayList<String>();
		
		m_servletContext = servletContext;
		m_servletContext.addListener(new SessionListener(this));
	}
	
	public void destroy() {
		if (m_connection != null)
		{
			try {
				m_connection.close();
				m_connection = null;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	private void updateUserInfo()
	{
		//TODO
	}
	
	private void createDatabase(Connection connection) throws SQLException
	{
		Statement stmt = connection.createStatement();
		
		String sql = "CREATE TABLE IF NOT EXISTS users " + 
		                      "(user_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + 
				               "user_name           TEXT    NOT NULL, " + 
		                       "user_email          TEXT    NOT NULL, " + 
		                       "user_api_key        TEXT    NOT NULL, " +
				               "user_registration_date      DATETIME default current_timestamp," +
				               "user_expiration_date        DATETIME default current_timestamp)";
		stmt.execute(sql);
		
		sql = "CREATE TABLE IF NOT EXISTS users_settings " + 
                "(user_id INT PRIMARY KEY     NOT NULL," + 
	             "user_available_features     INT, " + 
                 "user_ip_addresses           TEXT    , " + 
                 "user_referer                TEXT    , " +
                 "user_requests_limitations   INT    NOT NULL)";
        stmt.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS users_session_stats " + 
                "(session_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                 "user_id INT  NOT NULL," + 
	             "session_begin_time     DATETIME, " + 
                 "session_end_time       DATETIME, " + 
                 "session_num_requests   INT NOT NULL)";
        stmt.execute(sql);
        
		stmt.close();
	}
	
	public boolean isValidUser(HttpServletRequest req, String apiKey, String userIp, Boolean createSession) 
	{
		synchronized (m_invalidApiKeys) {
			if (m_invalidApiKeys.contains(apiKey))
				return false;
		}

		boolean res = false;

		UserInfo userInfo = null;

		if (req.getSession(false) != null) {
			userInfo = (UserInfo) req.getSession().getAttribute("user_info");
		}
		if (userInfo == null) {
			synchronized (m_activeUsers) {
				userInfo = m_activeUsers.get(apiKey);
			}
		}

		if (userInfo != null) {
			if (userInfo.ipAddresses != null && !userInfo.isIpAddressAllowed(userIp)) {
				return false;
			}

			if (userInfo.accessIsExpired()) {
				return false;
			}

			if (createSession) {
				createSession(req, userInfo);

				return true;
			}
		}
		
		try {
			Statement stmt = m_connection.createStatement();

			int userId = -1;
		    ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE user_api_key = '" +  apiKey +"';");
		    while ( rs.next() ) {
		        userId = rs.getInt("user_id");
		        
		        // check expiration date
		        java.sql.Date date = getDate(rs, "user_expiration_date");
		        if (date != null && date.before(new java.sql.Date(System.currentTimeMillis())))
		        {
		        	userId = -1;
		        	String userName = rs.getString("user_name");
		        	m_servletContext.log("Access is no longer possible due to expiration of an api_key. User '" + userName + "' (api_key '"+ apiKey+"') .");
		        }
		        break;
		    }
		    
		    rs.close();		    
		    
		    if (userId >= 0)
		    {
		    	res = true;
				rs = stmt.executeQuery("SELECT * FROM users_settings WHERE user_id = " + String.valueOf(userId) + ";");

				while (rs.next()) {
					String IPs = rs.getString("user_ip_addresses");

					if (!Helper.isEmpty(IPs)) {
						Collection<String> ipAddresses = parseIpAddresses(IPs);
						res = ipAddresses.contains(userIp);
					}
					
					if (res)
					{
						String referer = rs.getString("user_referer");
						if (!Helper.isEmpty(referer))
						{
							String reqReferer = req.getHeader("HTTP_REFERER");
							if (reqReferer != null && !reqReferer.contains(reqReferer))
								res = false;
						}
					}
					break;
				}
				
				rs.close();	
		    }
		    
		    stmt.close();
		    
		    if (res && createSession)
		    {
		    	createSession(req, apiKey, userId);
		    }		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res = false;
		}
		
		if (!res)
		{
			synchronized (m_invalidApiKeys) {
				m_invalidApiKeys.add(apiKey);
			}
		}
	      
		return res;
	}
	
	private java.sql.Date getDate(ResultSet rs, String columnName) throws ParseException, SQLException
	{
		String value = rs.getString(columnName);
		if (Helper.isEmpty(value))
			return null;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		java.util.Date date1 = df.parse(value);
		java.sql.Date date = new java.sql.Date(date1.getTime());

		return date;
	}
	
	private HttpSession createSessionInternal(HttpServletRequest req, UserInfo userInfo)
	{
		HttpSession session = req.getSession(true);
		session.setAttribute("api_key", userInfo.apiKey);
		session.setMaxInactiveInterval(10 * 60);
		session.setAttribute("user_info", userInfo);
		session.setAttribute("user_requests_limitation", userInfo.requestsLimit);
		
		return session;
	}
	
	private void createSession(HttpServletRequest req, UserInfo userInfo)
	{
		HttpSession session = req.getSession(false);
		
		if (session == null)
		{
			session = createSessionInternal(req, userInfo);
		}
	}
	
	private void createSession(HttpServletRequest req, String apiKey, int userId)
	{
		HttpSession session = req.getSession(false);

		if (session == null) {
			UserInfo userInfo = null;
			synchronized (m_activeUsers) {
				userInfo = m_activeUsers.get(apiKey);

				if (userInfo == null) {
   				    userInfo = getUserInfo(apiKey, userId);
					m_activeUsers.put(apiKey,  userInfo);
				} else {
						userInfo.sessions++;
				}

				session = createSessionInternal(req, userInfo);
			}
		}
	}
	
	private UserInfo getUserInfo(String apiKey, int userId)
	{
		UserInfo userInfo = null;
		
		try {
			Statement stmt = m_connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE user_id = " + String.valueOf(userId) + ";");

			while (rs.next()) {
				java.sql.Date date = getDate(rs, "user_expiration_date");
				userInfo = new UserInfo(userId, apiKey, null, null, date == null ? null : new Date(date.getTime()), 0);
				break;
			}

			rs.close();	
			
			rs = stmt.executeQuery("SELECT * FROM users_settings WHERE user_id = " + String.valueOf(userId) + ";");
			while (rs.next()) {
				String ipAddresses = rs.getString("user_ip_addresses");
				if (!Helper.isEmpty(ipAddresses))
				{
					userInfo.setIpAddresses(parseIpAddresses(ipAddresses));
				}
				
				userInfo.setReferer(rs.getString("user_referer"));

				userInfo.requestsLimit = rs.getInt("user_requests_limitations");
				break;
			}

			rs.close();	
		    
		    stmt.close();
		    
		    return userInfo;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public void endSession(HttpSession session)
	{
		String apiKey = (String)session.getAttribute("api_key");
		
		synchronized (m_activeUsers) {
			UserInfo userInfo = m_activeUsers.get(apiKey);

			if (userInfo != null) {
				if (userInfo.sessions == 1) {
					if (m_logStatistics)
					{
						writeUserStatistics(userInfo);
					}
					
					m_activeUsers.remove(apiKey);
				} else {
					userInfo.sessions--;
				}
			}
		}
	}
	
	public void logRequest(HttpServletRequest req)
	{
		if (m_logStatistics)
		{
			UserInfo userInfo = (UserInfo)req.getSession().getAttribute("user_info");
			userInfo.requests++;
			
			if (userInfo.requests >= 100)
			{
				writeUserStatistics(userInfo);
			}
		}
	}
	
	private void writeUserStatistics(UserInfo userInfo)
	{
		try
		{
			synchronized(userInfo)
			{
				if (userInfo.requests <= 0)
					return;

				int numRequests = userInfo.requests;
				userInfo.requests = 0;
			
				PreparedStatement stmt = m_connection.prepareStatement("INSERT INTO users_session_stats (user_id, session_begin_time, session_end_time, session_num_requests) " + "VALUES ("
						+ userInfo.userId + ", ? , ?, " + numRequests + ")");

				java.sql.Timestamp timestampBegin = new java.sql.Timestamp(userInfo.sessionBegin.getTime());
				java.sql.Timestamp timestampEnd = new java.sql.Timestamp(System.currentTimeMillis());
												
				stmt.setTimestamp(1, timestampBegin);
				stmt.setTimestamp(2, timestampEnd);
				
				int res = stmt.executeUpdate();

				userInfo.resetSessionBeginTime();

				stmt.close();
			}
		}
		catch(Exception ex)
		{
			
		}
	}
	
	private Collection<String> parseIpAddresses(String ipAddresses)
	{
		String[] array = ipAddresses.split(",");
		if (array != null)
		{
			ArrayList<String> result = new ArrayList<String>(); 
		    for(String e : array)
		    	result.add(e.trim());
		    
		    return result;
		}
		
		return null;
	}
}