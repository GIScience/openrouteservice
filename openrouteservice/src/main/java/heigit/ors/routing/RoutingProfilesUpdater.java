/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package heigit.ors.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.Helper;
import heigit.ors.routing.configuration.RouteProfileConfiguration;
import heigit.ors.routing.configuration.RouteUpdateConfiguration;
import heigit.ors.routing.traffic.RealTrafficDataProvider;
import heigit.ors.util.DebugUtility;
import heigit.ors.util.FileUtility;
import heigit.ors.util.StackTraceUtility;
import org.apache.commons.io.FileUtils;

import java.io.*;
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
import java.util.logging.Logger;

public class RoutingProfilesUpdater {

	public class UpdateTask extends TimerTask {

		private RoutingProfilesUpdater m_updater;

		public UpdateTask(RoutingProfilesUpdater updater) {
			m_updater = updater;
		}

		public void run() {
			m_updater.run();
		}
	}

	private static Logger LOGGER = Logger.getLogger(RoutingProfilesUpdater.class.getName());

	private RouteUpdateConfiguration m_config;
	private RoutingProfilesCollection m_routeProfiles;
	private Timer m_timer;
	private long m_updatePeriod;
	private boolean m_isRunning;
	private String m_updateStatus = null;
	private Date m_nextUpdate;

	public RoutingProfilesUpdater(RouteUpdateConfiguration config, RoutingProfilesCollection profiles) {
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
		LOGGER.info("Profile updater is started and scheduled at " + firstUpdateTime.toString() + ".");
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

			LOGGER.info("Start updating profiles...");

			m_nextUpdate = new Date(startTime + m_updatePeriod);

			String osmFile = null;
			String datasource = m_config.DataSource;

			FileUtility.makeDirectory(m_config.WorkingDirectory);

			String md5Sum = null;
			String newMd5Sum = null;
			boolean skipUpdate = false;  

			File fileLastUpdate =  Paths.get(m_config.WorkingDirectory,"last-update.md5").toFile();
			if (fileLastUpdate.exists())
				md5Sum =  FileUtils.readFileToString(fileLastUpdate);

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
					skipUpdate = true;

				if (!skipUpdate)
				{
					Path path = Paths.get(m_config.WorkingDirectory, FileUtility.getFileName(new URL(datasource)));

					try
					{
						downloadFile(datasource, path.toFile());
					}
					catch(Exception ex)
					{
						LOGGER.warning(ex.getMessage());

						Thread.sleep(300000);

						m_isRunning = false;
						run();

						return;
					}

					osmFile = path.toString();
				}
			} else {
				File file = new File(datasource + ".md5");
				newMd5Sum = file.exists() ? FileUtils.readFileToString(file) : FileUtility.getMd5OfFile(datasource);

				if (md5Sum != null && newMd5Sum != null && md5Sum.contains(newMd5Sum))
					skipUpdate = true;

				if (!skipUpdate)
				{
					file = new File(datasource);
					Path path = Paths.get(m_config.WorkingDirectory, file.getName());

					// make a local copy of the file
					FileUtils.copyFile(file, path.toFile());

					osmFile = path.toString();
				}
			}

			if (!skipUpdate)
			{
				if (Helper.isEmpty(newMd5Sum))
					newMd5Sum = FileUtility.getMd5OfFile(osmFile);

				md5Sum = newMd5Sum;
				File file = new File(osmFile);
				String newFileStamp = Long.toString(file.length());

				String tempGraphLocation = Paths.get(m_config.WorkingDirectory, "graph").toString();

				try
				{
					// Clear directory from a previous build
					File fileGraph = new File(tempGraphLocation);
					if (fileGraph.exists())
						FileUtils.deleteDirectory(fileGraph);	
				}
				catch(Exception ex)
				{}

				FileUtility.makeDirectory(tempGraphLocation);

				RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext();
				int nUpdatedProfiles = 0;

				for (RoutingProfile profile : m_routeProfiles.getUniqueProfiles()) {
					RouteProfileConfiguration rpc = profile.getConfiguration();

					Path pathTimestamp = Paths.get(rpc.getGraphPath(), "stamp.txt");
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
						m_updateStatus = "preparing profile '" + rpc.getProfiles() +"'";

						RouteProfileConfiguration rpcNew = rpc.clone();
						rpcNew.setGraphPath(tempGraphLocation);
						GraphHopper gh = RoutingProfile.initGraphHopper(osmFile, rpcNew, RoutingProfileManager.getInstance().getProfiles(), loadCntx);

						if (gh != null) {
							profile.updateGH(gh);

							if (RealTrafficDataProvider.getInstance().isInitialized())
							{
								m_updateStatus += ". Performing map matching...";
								RealTrafficDataProvider.getInstance().updateGraphMatching(profile, profile.getGraphLocation());
							}

							nUpdatedProfiles++;
						}
					} catch (Exception ex) {
						LOGGER.severe("Failed to update graph profile. Message:" + ex.getMessage() + "; StackTrace: " +	StackTraceUtility.getStackTrace(ex));
					}

					m_updateStatus = null;
				}

				loadCntx.releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed();
				
				FileUtils.writeStringToFile(fileLastUpdate, md5Sum);

				long seconds = (System.currentTimeMillis() - startTime) / 1000;
				LOGGER.info(nUpdatedProfiles + " of " + m_routeProfiles.size() + " profiles were updated in " + seconds + " s.");

				m_updateStatus = "Last update on " + new Date() + " took " + seconds + " s.";
			}
			else
			{
				LOGGER.info("No new data is available.");
			}
			
		} catch (Exception ex) {
			LOGGER.warning(ex.getMessage());
		}

		LOGGER.info("Next route profiles update is scheduled on " + m_nextUpdate.toString());

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
