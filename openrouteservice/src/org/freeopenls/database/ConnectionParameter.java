
package org.freeopenls.database;

/**
 * <p><b>Title: ConnectionParameter</b></p>
 * <p><b>Description:</b> Class for ConnectionParameter<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-12-10
 */
public class ConnectionParameter {

	private String mDBType = null;
	private String mHost = null;
	private String mPort = null;
	private String mDBName = null;
	private String mTableName = null;
	private String mUser = null;
	private String mPasswd = null;

	/**
	 * Constructor
	 * 
	 * @param dbtype
	 * @param host
	 * @param port
	 * @param dbname
	 * @param tablename
	 * @param user
	 * @param passwd
	 */
	public ConnectionParameter(String dbtype, String host, String port, String dbname, 
			String tablename, String user, String passwd){
		
		mDBType = dbtype;
		mHost = host;
		mPort = port;
		mDBName = dbname;
		mTableName = tablename;
		mUser = user;
		mPasswd = passwd;
	}
	
	/**
	 * @return the mDBType
	 */
	public String getDBType() {
		return mDBType;
	}

	/**
	 * @return the mHost
	 */
	public String getHost() {
		return mHost;
	}

	/**
	 * @return the mPort
	 */
	public String getPort() {
		return mPort;
	}

	/**
	 * @return the mDBName
	 */
	public String getDBName() {
		return mDBName;
	}

	/**
	 * @return the mTableName
	 */
	public String getTableName() {
		return mTableName;
	}

	/**
	 * @return the mUser
	 */
	public String getUser() {
		return mUser;
	}

	/**
	 * @return the mPasswd
	 */
	public String getPasswd() {
		return mPasswd;
	}

	/**
	 * @return toString()
	 */
	public String toString(){
		return "DBType: "+mDBType+" Host: "+mHost+" Port: "+mPort+" DBName: "+mDBName+" TabelName: "+mTableName+" User: "+mUser+" Passwd: "+mPasswd;
	}
}
