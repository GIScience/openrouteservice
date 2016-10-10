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

package org.freeopenls.routeservice.routing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.lf5.LogLevel;
import org.freeopenls.routeservice.routing.configuration.RouteProfileConfiguration;
import org.freeopenls.routeservice.routing.configuration.RouteUpdateConfiguration;
import org.freeopenls.routeservice.traffic.RealTrafficDataProvider;
import org.freeopenls.tools.DebugUtility;
import org.freeopenls.tools.FileUtility;
import org.freeopenls.tools.StackTraceUtility;

import com.graphhopper.GraphHopper;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.Helper;

public class RouteProfilesUpdater {

	public class UpdateTask extends TimerTask {

		private RouteProfilesUpdater m_updater;

		public UpdateTask(RouteProfilesUpdater updater) {
			m_updater = updater;
		}

		public void run() {
			m_updater.run();
		}
	}

	private Logger logger = Logger.getLogger("org.freeopenls.routeservice.routing.RouteProfilesUpdater");

	private RouteUpdateConfiguration m_config;
	private RouteProfilesCollection m_routeProfiles;
	private Timer m_timer;
	private long m_updatePeriod;
	private boolean m_isRunning;
	private String m_updateStatus = null;
	private Date m_nextUpdate;

	public RouteProfilesUpdater(RouteUpdateConfiguration config, RouteProfilesCollection profiles) {
		m_config = config;
		m_routeProfiles = profiles;

		if (m_config.DataSource == null || m_config.DataSource.isEmpty())
			throw new IllegalArgumentException("DataSource is null or empty.");
		if (m_config.WorkingDirectory == null || m_config.WorkingDirectory.isEmpty())
			throw new IllegalArgumentException("WorkingDirectory is null or empty.");
	}

	public void start() {
		// parse time of the format: day of the week, time, period
		String strDateTime = m_config.Time;
		String[] splitValues = strDateTime.split(",");
		int dayOfWeek = Integer.valueOf(splitValues[0].trim()) + 1; // Sunday is 1.
		m_updatePeriod = Integer.valueOf(splitValues[2].trim());
		splitValues = splitValues[1].trim().split(":");
		int hours = Integer.valueOf(splitValues[0].trim());
		int minutes = Integer.valueOf(splitValues[1].trim());
		int seconds = Integer.valueOf(splitValues[2].trim());

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, seconds);

		Date firstUpdateTime = calendar.getTime();

		TimerTask timerTask = new UpdateTask(this);
		m_timer = new Timer(true);
		m_timer.schedule(timerTask, firstUpdateTime, m_updatePeriod);

		m_nextUpdate = firstUpdateTime;
		logger.info("Next route profiles update is scheduled at " + firstUpdateTime.toString());
	}

	private void downloadFile(String url, File destination) {
		try {
			URL website = new URL(url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(destination);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String downloadFileContent(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
       
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) 
            response.append(inputLine);

        in.close();

        return response.toString();
    }
	
	public Date getNextUpdate()
	{
		return m_nextUpdate;		
	}
	
	public String getStatus()
	{
		return m_updateStatus;
	}
	
	public boolean isRunning()
	{
		return m_isRunning;
	}

	private void run() {
		if (m_isRunning)
			return;

		m_isRunning = true;
		
		try {
			long startTime = System.currentTimeMillis();

			logger.info("Start updating route profiles...");

			m_nextUpdate = new Date(startTime + m_updatePeriod);

			String osmFile = null;
			String datasource = m_config.DataSource;

			FileUtility.makeDirectory(m_config.WorkingDirectory);
			
			String md5Sum = null;
			String newMd5Sum = null;
			
			File fileMd5 = Paths.get(m_config.WorkingDirectory,"last-update.md5").toFile();
			if (fileMd5.exists())
				md5Sum =  FileUtils.readFileToString(fileMd5);
			
			if (datasource.contains("http")) {
				m_updateStatus = "donwloading data";
				
				try
				{
					newMd5Sum = downloadFileContent(datasource + ".md5");
				}
				catch(Exception ex2)
				{
					
				}

				if (md5Sum != null && newMd5Sum != null && md5Sum.contains(newMd5Sum))
					return;

				Path path = Paths.get(m_config.WorkingDirectory, FileUtility.getFileName(new URL(datasource)));
				
				try
				{
					downloadFile(datasource, path.toFile());
				}
				catch(Exception ex)
				{
					logger.warning(ex.getMessage());
					
					Thread.sleep(300000);
					
					m_isRunning = false;
					run();
					
					return;
				}
				
				osmFile = path.toString();
			} else {
				File fMd5 = new File(datasource + ".md5");
				if (fMd5.exists())
				{
					newMd5Sum = FileUtils.readFileToString(fMd5);
					
					if (md5Sum != null && md5Sum.contains(newMd5Sum))
						return;
				}
				
				File file = new File(datasource);
				Path path = Paths.get(m_config.WorkingDirectory, file.getName());
				
				// make a local copy of the file
				FileUtils.copyFile(file, path.toFile());
				
				osmFile = path.toString();
			}
			
			if (Helper.isEmpty(newMd5Sum))
			  newMd5Sum = FileUtility.getMd5OfFile(osmFile);

			md5Sum = newMd5Sum;
			File file = new File(osmFile);
			String newFileStamp = Long.toString(file.length());
			int i = 0;

			String tempLocation = Paths.get(m_config.WorkingDirectory, "graph").toString();
			
			try
			{
				// Clear directory from a previous build
				File fileGraph = new File(tempLocation);
				if (fileGraph.exists())
					FileUtils.deleteDirectory(fileGraph);	
			}
			catch(Exception ex)
			{}

			FileUtility.makeDirectory(tempLocation);

			for (RouteProfile profile : m_routeProfiles.getUniqueProfiles()) {
				i++;
				RouteProfileConfiguration rpc = profile.getConfiguration();
			
				Path pathTimestamp = Paths.get(rpc.GraphLocation, "stamp.txt");
				File file2 = pathTimestamp.toFile();
				if (file2.exists()) {
					String oldFileStamp = FileUtils.readFileToString(file2);
					if (newFileStamp.equals(oldFileStamp))
						continue;
				}
				
				if (m_updatePeriod > 0)
				{
					StorableProperties storageProps = profile.getGraphProperties();
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

					Date importDate = df.parse(storageProps.get("osmreader.import.date"));
					
					long diff = startTime - importDate.getTime();
					
					if (!DebugUtility.isDebug())
					{
						if (diff < m_updatePeriod)
							continue;
					}
				}

				try {
					m_updateStatus = "preparing profile " + Integer.toString(i);

					GraphHopper gh = RouteProfile.initGraphHopper(osmFile, profile.getConfigRootPath(),
							rpc.ConfigFileName, tempLocation, rpc.DynamicWeighting, rpc.StoreSurfaceInformation, rpc.StoreHillIndex, rpc.UseTrafficInformation, rpc.BBox, RouteProfileManager.getInstance().getProfiles());

					if (gh != null) {
						profile.updateGH(gh);

						if (RealTrafficDataProvider.getInstance().isInitialized())
						{
							m_updateStatus += ". Performing map matching...";
							RealTrafficDataProvider.getInstance().updateGraphMatching(profile, profile.getGraphLocation());
						}
					}
				} catch (Exception ex) {
					logger.severe("Failed to update graph profile '" + rpc.ConfigFileName +"'. Message:" + ex.getMessage() + "; StackTrace: " +	StackTraceUtility.getStackTrace(ex));
				}
				
				m_updateStatus = null;
			}
			
		    FileUtils.writeStringToFile(fileMd5, md5Sum);
		    
			long seconds = (System.currentTimeMillis() - startTime) / 1000;
			logger.info(i + " of " + m_routeProfiles.size() + " profiles were updated in " + seconds + " s.");
			
			m_updateStatus = "last update on " + new Date() + " took " + seconds + " s.";
		} catch (Exception ex) {
			logger.warning(ex.getMessage());
		}

		logger.info("Next route profiles update is scheduled on " + m_nextUpdate.toString());

		m_isRunning = false;
	}

	public void stop() {
		if (m_timer != null) {
			m_timer.cancel();
			m_timer = null;
		}
	}

	public void destroy() {
		stop();
	}
}
