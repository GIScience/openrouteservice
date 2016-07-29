
package org.freeopenls.database;

import java.sql.DriverManager;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * <p><b>Title: ConnectionManager</b></p>
 * <p><b>Description:</b> Class for ConnectionManager<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-12-10
 */
public class ConnectionManager {
	/** Logger, used to log errors(exceptions) and additionally information */
	private final Logger mLogger = Logger.getLogger(ConnectionManager.class.getName());
	/** PostGIS Parameters**/
	private ConnectionParameter mConnParamterDB;
	/** ArrayList of PG Connections */
	private ArrayList<PGConnection> mConnections = new ArrayList<PGConnection>();
    
	/**
	 * Constructor
	 * 
	 * @param connParamterDB
	 * @param numberOfConnections
	 */
	public ConnectionManager(ConnectionParameter connParamterDB, int numberOfConnections){
		mConnParamterDB = connParamterDB;
		
    	try {
    		Class.forName("org.postgresql.Driver");
    		
    		for(int index=0 ; index<numberOfConnections ; index++){
        		// Connection to DB
        		PGConnection pgConnectionTMP = new PGConnection(
        				DriverManager.getConnection("jdbc:postgresql://"+mConnParamterDB.getHost()+":"+mConnParamterDB.getPort()+"/"+mConnParamterDB.getDBName(), 
        						mConnParamterDB.getUser(), mConnParamterDB.getPasswd()), index, false);
        		mConnections.add(pgConnectionTMP);
    		}
	      } catch( ClassNotFoundException cnfex ) {
				mLogger.error(cnfex);
	      }	catch( Exception ex ) {
				mLogger.error(ex);
	      }
	}

	/**
	 * return a free connection
	 * 
	 * @return PG Connection
	 */
	public synchronized PGConnection getFreeConnection(){
		boolean free = false;
		int index = 0;

		try{
			do{
				if(!mConnections.get(index).isInUse()){
					free = true;
					if(mConnections.get(index).getConnection().isClosed()){
						mConnections.get(index).closeConnection();
						mLogger.error("Connection is Closed! Index: "+index+" Try to open the connection ...");
						mConnections.get(index).setConnection(
								//DriverManager.getConnection("jdbc:postgresql://"+mConnParamterDB.getHost()
								//	+":"+mConnParamterDB.getPort()+"/"+mConnParamterDB.getDBName()));
								DriverManager.getConnection("jdbc:postgresql://"+mConnParamterDB.getHost()
        						    +":"+mConnParamterDB.getPort()+"/"+mConnParamterDB.getDBName(), 
        						    mConnParamterDB.getUser(), mConnParamterDB.getPasswd()));
						mLogger.error(".. connection is opend again! Index: "+index);
					}
					mConnections.get(index).setInUse(true);
				}
				
//				if(mConnections.get(index).getTimeGetInUse() > 0)
//					if((System.currentTimeMillis()-mConnections.get(index).getTimeGetInUse()) > 10000){
//						mLogger.error("more than 10.sec. ...: @ "+index);
//					}
				
				if(!(index<mConnections.size()-1) && !free){
					//mLogger.error("No Connection FREE from "+mConnections.size()+" possibilities!");
					index=0;
				}
				else if(!free){
					index++;
				}

			}while(!free);
		}catch(Exception ex){
			ex.printStackTrace();
			mLogger.error(ex);
		}
		
		PGConnection conn =  mConnections.get(index);
		
		return conn;
	}

	/**
	 * enable used connection
	 * @param index
	 */
	public void enableConnection(int index){
		mConnections.get(index).setInUse(false);
	}
	
	/**
	 * Close all open connections
	 */
	public void closeAllConnections(){
        for(int i=0 ; i<mConnections.size() ; i++)
        	mConnections.get(i).closeConnection();
	}

	/**
	 * @return the NumberOfConnetions
	 */
	public int getNumberOfConnetions() {
		return mConnections.size();
	}
	
	/**
	 * @return toString()
	 */
	public String toString() {
		return "Number of Connections: "+mConnections.size()+"\n"+mConnParamterDB.toString();
	}

	/**
	 * @return the ConnParamterDB
	 */
	public ConnectionParameter getConnParamterDB() {
		return mConnParamterDB;
	}
}
