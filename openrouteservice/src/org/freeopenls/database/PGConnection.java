package org.freeopenls.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * <p><b>Title: PGConnection</b></p>
 * <p><b>Description:</b> Class for PGConnection<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-12-10
 */
public class PGConnection {

	/** Logger, used to log errors(exceptions) and additionally information */
	private final Logger mLogger = Logger.getLogger(PGConnection.class.getName());

	/** PostGIS Parameters**/
	private Connection mConnection = null;
	private Statement  mStatement = null;
	
//	private long mTimeGetInUse = 0;
	
	private int mConnectionNumber = 0;
	
	private boolean mInUse = false;

	/**
	 * Constructor
	 * 
	 * @param connection
	 * @param inUse
	 */
	public PGConnection(Connection connection, int connectionNumber, boolean inUse){//, long startTime){
		try{
			mConnection = connection;
			mStatement = mConnection.createStatement();
			mConnectionNumber = connectionNumber;
			mInUse = inUse;
			//mStartTime = startTime;
		}catch(SQLException sqlex){
			mLogger.error(sqlex);
		}
	}

	/**
	 * @return the Connection
	 */
	public Connection getConnection() {
		return mConnection;
	}
	
	/**
	 * set the Connection
	 */
	public void setConnection(Connection connection) throws SQLException {
		this.mConnection = connection;
		this.mStatement = mConnection.createStatement();
	}

	/**
	 * @return the Statement
	 */
	public Statement getStatement() {
		return mStatement;
	}

	/**
	 * @return the connection number
	 */
	public int getConnectionNumber() {
		return mConnectionNumber;
	}
	
	/**
	 * @return the inUse
	 */
	public boolean isInUse() {
		return mInUse;
	}
	
	/**
	 * set the inUse
	 */
	public void setInUse(boolean inUse) {
//		if(inUse)
//			mTimeGetInUse = System.currentTimeMillis();
//		else
//			mTimeGetInUse = 0;

		mInUse = inUse;
	}
	
	/**
	 * close PGConnection
	 */
	public void closeConnection(){
        try { 
        	if( null != mConnection ) mConnection.close(); 
        	} catch( Exception ex ) {
        		mLogger.error(ex);
        	}
        try { 
        	if( null != mStatement ) mStatement.close(); 
        	} catch( Exception ex ) {
        		mLogger.error(ex);
        	}
 
		mInUse = false;
	}

//	/**
//	 * @return the TimeGetInUse
//	 */
//	public long getTimeGetInUse() {
//		return mTimeGetInUse;
//	}

//
//	/**
//	 * @param startTime the StartTime to set
//	 */
//	public void setStartTime(long startTime) {
//		mStartTime = startTime;
//	}
}
