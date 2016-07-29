

package org.freeopenls.logger;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * <p><b>Title: LoggerConfig</b></p>
 * <p><b>Description:</b> Class for LoggerConfig<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-12-10
 */
public class LoggerConfig {

	private static String mLastString = "";

	/**
	 * 
	 * @param userFile
	 * @throws FileNotFoundException
	 */
	public static void initLogger(File userFile) throws FileNotFoundException {

		String filename = userFile.getAbsolutePath();
		
		if (mLastString.equals(filename)) {
			LogLog.warn("Init the logger with the same file. Not updated!");
			return; // you don't need to do something and exit
		}

		// temp helper
		String extname, useFileName;

		int idx = filename.lastIndexOf('.');
		if (idx == filename.length()) {
			// file name does not contain any period
			throw new IllegalArgumentException("The given Filename [" + filename + "] has no extension");
		} else {
			/*
			 * split file name into base name and extension - base name contains
			 * all characters before the period - extension contains all
			 * characters after the period
			 */
			// basename = filename.substr(0, idx);
			extname = filename.substring(idx + 1);
			if (extname.isEmpty()) {
				// contains period but no extension: append tmp
				throw new IllegalArgumentException("The given Filename [" + filename + "] has no extension");
			}
		}

		// try to find the Config-File with the given name
		// IF the file don't exits try to find the default files.
		useFileName = filename;

		boolean fileExists = false;

		if (extname.equals("xml")) {
			String defaultXML = "log/log4j.xml";
			File defaultXMLFile = new File(defaultXML);
			if (userFile.exists()) {
				fileExists = true;
			} else {
				LogLog.error(("Couln't find the given config file, trying to find the default files."));
			}

			if (!fileExists && defaultXMLFile.exists()) {
				LogLog.warn("Using the default file '" + defaultXML	+ "' to config the logger.");
				useFileName = defaultXML;
				fileExists = true;
			}

			// if a file exists, init the Logger with the xml file
			if (fileExists) {
				// reset the existing
				Logger.getRootLogger().removeAllAppenders();
				DOMConfigurator.configure(useFileName);
			}
		} 

		if (fileExists) {
			LogLog.debug("Using configuration file [" + useFileName	+ "] for automatic log4cxx configuration.");
			mLastString = useFileName;
		} else {
			mLastString = "";
			throw new FileNotFoundException("Could not find configuration file: [" + useFileName + "].");
		}
	}
}
