
package org.freeopenls.filedelete;

import java.io.File;
import java.io.FileFilter;

import org.apache.log4j.Logger;

/**
 * Class for delete Files.<br>
 * ATTENTION: This Thread delete every *.xml file in the given Path (Directory)!<br>
 * 
 * USE IT WITH CAUTION!!!
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * @version 1.0 2006-11-02
 * @version 1.1 2008-04-20
 */
/**
 * <p><b>Title: ThreadFileDelete</b></p>
 * <p><b>Description:</b>  * Class for delete Files.<br>
 * ATTENTION: This Thread delete every *.xml file in the given Path (Directory)!<br>
 * 
 * USE IT WITH CAUTION!!!
 * <br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-11-02
 * @version 1.1 2008-04-20
 */
public class ThreadFileDelete extends Thread {
	/** Logger, used to log errors(exceptions) and additionally information */
    private static Logger mLogger = Logger.getLogger(FileDelete.class.getName());
    /** int Time which Thread sleep */
	private int m_iSleepTimeHours = 0;
	/** Directory Path*/
	private File m_path;
	/** int Hours after Files deleted */
	private int m_iDeleteAfterHours = 0;
	/** FileFilter */
	private FileFilter m_filefilter;

    /**
     * Constructor - Set FileFilter and SleepTime of the Thread
     * 
     */
	public ThreadFileDelete(int iSleepTimeHours, String sPath, int iDeleteAfterHours) {
		m_iSleepTimeHours = iSleepTimeHours;
		m_path = new File(sPath);
		m_iDeleteAfterHours = iDeleteAfterHours;

		m_filefilter = new FileFilter() {
            public boolean accept(File f) {
            	boolean boolResponse = false;
            	if(f.getName().toLowerCase().endsWith(".xml"))
            		boolResponse = true;
            	if(f.getName().toLowerCase().endsWith(".kml"))
            		boolResponse = true;
            	if(f.getName().toLowerCase().endsWith(".jpeg"))
            		boolResponse = true;
            	if(f.getName().toLowerCase().endsWith(".png"))
            		boolResponse = true;
            	if(f.getName().toLowerCase().endsWith(".gif"))
            		boolResponse = true;
            	if(f.getName().toLowerCase().endsWith(".sld"))
            		boolResponse = true;
            	if(f.getName().toLowerCase().endsWith(".sld3d"))
            		boolResponse = true;
//            	if(f.getName().toLowerCase().endsWith(".shp"))
//            		boolResponse = true;
//            	if(f.getName().toLowerCase().endsWith(".dbf"))
//            		boolResponse = true;
//            	if(f.getName().toLowerCase().endsWith(".shx"))
//            		boolResponse = true;
//            	if(f.getName().toLowerCase().endsWith(".properties"))
//            		boolResponse = true;
            	return boolResponse;
            }
        };
	}

    /**
     * Main Method of FileDelete (Thread)
     * 
     */
	public void run() {
		
		//log.info("	*** Thread: FileDelete START! ***");
		
		while (!isInterrupted()) {

			long lTime = System.currentTimeMillis();
			//log.info("Thread DeleteFile look for files.");
			int iNumberofDeletes = 0;
			File[] files = m_path.listFiles(m_filefilter);
			if (files != null)
			{	
				for (File file : files) {
					long lTimeTMP = file.lastModified();
					long lTimeDiff = lTime - lTimeTMP;
					int iHours = ((int) lTimeDiff / 1000) / 60 / 60;

					if (iHours > m_iDeleteAfterHours) {
						boolean boolDelete = file.delete();
						if (!boolDelete) {
							mLogger.error("- Problem in FileDelete. File could not be deleted! "
									+ file.getPath());
						} else {
							// log.info("File deleted: " + boolDelete);
							iNumberofDeletes++;
						}
					}
				}
			}
			mLogger.info(" * Thread FileDelete *\n"
					+"		 Folder: "+ m_path +" Files deleted: "+ iNumberofDeletes);
			try {
				Thread.sleep(m_iSleepTimeHours*3600000);
			} catch (InterruptedException e) {
				interrupt();
				//log.info("Thread: FileDelete END!");
			}
		}
	}
}
