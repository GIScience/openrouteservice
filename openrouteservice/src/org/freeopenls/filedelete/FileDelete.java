package org.freeopenls.filedelete;

import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * <p><b>Title: FileDelete</b></p>
 * <p><b>Description:</b> Class for FileDelete<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-12-10
 */
public class FileDelete {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static Logger mLogger = Logger.getLogger(FileDelete.class.getName());

    /** FileDelete Thread for delete files */
    private static ThreadFileDelete mThread = null;
    
	public static void initFileDelete(){
		
		if(mThread == null){
			try{
				//*** Open ConfigFile ***
				String fileNameProperties = "../FileDelete.properties.xml";
		    	URL url = FileDelete.class.getClassLoader().getResource(fileNameProperties);
		    	Properties properties = new Properties();
		    	properties.loadFromXML(url.openStream());
				
		    	String fileDeletePath = properties.getProperty("FILEDELETE_PATH");
				int fileDeleteInvoking = Integer.parseInt(properties.getProperty("FILEDELETE_INVOKING"));
				int fileDeleteAfter = Integer.parseInt(properties.getProperty("FILEDELETE_AFTER"));
				
				//Create Thread FileDelete
				mThread = new ThreadFileDelete(fileDeleteInvoking, fileDeletePath, fileDeleteAfter);
				mThread.start();
				
				mLogger.info("***********************************************************************************");
				mLogger.info("* * FileDelete  s t a r t *");
				mLogger.info("* Path: "+fileDeletePath);
				mLogger.info("* Delete Invoking: "+fileDeleteInvoking+" h");
				mLogger.info("* Delete After: "+fileDeleteAfter+" h");
				mLogger.info("***********************************************************************************");
	
			}catch (Exception e) {
				mLogger.error(e);
			}
		}
		else
			mLogger.info("FileDelete already starts ...");
	}

	/**
	 * Method that stop the FileDelete Thread
	 */
	public static synchronized void stop(){
		if(mThread!=null){
			mThread.interrupt();
			mLogger.info("FileDelete  s t o p !");
			mThread = null;
		}
		else
			mLogger.info("FileDelete already stops ...");
	}
}
