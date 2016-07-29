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

// Authors: M. Rylov

package org.freeopenls.routeservice.traffic.providers;

import java.io.IOException;
import java.util.Properties;

import org.freeopenls.tools.FileUtility;

public class FileDataSource implements TrafficInfoDataSource {
	private String m_path;
	
	public FileDataSource()
	{}
	
	public void Initialize(Properties props)
	{
		m_path = props.getProperty("path");	
	}

	@Override
	public String getMessage() throws IOException {
		return FileUtility.readFile(m_path, "ISO-8859-1");
	}
}
