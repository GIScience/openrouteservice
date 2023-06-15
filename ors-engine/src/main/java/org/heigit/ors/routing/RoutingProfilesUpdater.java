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
package org.heigit.ors.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.Helper;
import org.apache.commons.io.FileUtils;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.routing.configuration.RouteUpdateConfiguration;
import org.heigit.ors.util.DebugUtility;
import org.heigit.ors.util.FileUtility;
import org.heigit.ors.util.StackTraceUtility;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoutingProfilesUpdater {

	public class UpdateTask extends TimerTask {

		private final RoutingProfilesUpdater updater;

		public UpdateTask(RoutingProfilesUpdater updater) {
			this.updater = updater;
		}

		public void run() {
			updater.run();
		}
	}

	private static final Logger LOGGER = Logger.getLogger(RoutingProfilesUpdater.class.getName());

	private final RouteUpdateConfiguration config;
	private final RoutingProfilesCollection routingProfilesCollection;
	private Timer timer;
	private long updatePeriod;
	private boolean isRunning;
	private String updateStatus = null;
	private Date nextUpdate;

	public RoutingProfilesUpdater(RouteUpdateConfiguration config, RoutingProfilesCollection profiles) {
		this.config = config;
		routingProfilesCollection = profiles;

		if (this.config.getDataSource() == null || this.config.getDataSource().isEmpty())
			throw new IllegalArgumentException("DataSource is null or empty.");
		if (this.config.getWorkingDirectory() == null || this.config.getWorkingDirectory().isEmpty())
			throw new IllegalArgumentException("WorkingDirectory is null or empty.");
	}

	public void start() {
		// parse time of the format: day of the week, time, period
		String strDateTime = config.getTime();
		String[] splitValues = strDateTime.split(",");
		int dayOfWeek = Integer.parseInt(splitValues[0].trim()) + 1; // Sunday is 1.
		updatePeriod = Integer.parseInt(splitValues[2].trim());
		splitValues = splitValues[1].trim().split(":");
		int hours = Integer.parseInt(splitValues[0].trim());
		int minutes = Integer.parseInt(splitValues[1].trim());
		int seconds = Integer.parseInt(splitValues[2].trim());

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, seconds);

		Date firstUpdateTime = calendar.getTime();

		TimerTask timerTask = new UpdateTask(this);
		timer = new Timer(true);
		timer.schedule(timerTask, firstUpdateTime, updatePeriod);

		nextUpdate = firstUpdateTime;
		if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info("Profile updater is started and scheduled at " + firstUpdateTime + ".");
	}

	private void downloadFile(String url, File destination) {
		try (FileOutputStream fos = new FileOutputStream(destination)){
			ReadableByteChannel rbc = Channels.newChannel(new URL(url).openStream());
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
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
		return nextUpdate;
	}

	public String getStatus()
	{
		return updateStatus;
	}

	public boolean isRunning()
	{
		return isRunning;
	}

	private void run() {
		if (isRunning)
			return;

		isRunning = true;

		try {
			long startTime = System.currentTimeMillis();

			LOGGER.info("Start updating profiles...");

			nextUpdate = new Date(startTime + updatePeriod);

			String osmFile = null;
			String datasource = config.getDataSource();

			FileUtility.makeDirectory(config.getWorkingDirectory());

			String md5Sum = null;
			String newMd5Sum = null;
			boolean skipUpdate = false;  

			File fileLastUpdate =  Paths.get(config.getWorkingDirectory(),"last-update.md5").toFile();
			if (fileLastUpdate.exists())
				md5Sum =  FileUtils.readFileToString(fileLastUpdate);

			if (datasource.contains("http")) {
				updateStatus = "donwloading data";

				try {
					newMd5Sum = downloadFileContent(datasource + ".md5");
				} catch(Exception ex2) {
					// do nothing
				}

				if (md5Sum != null && newMd5Sum != null && md5Sum.contains(newMd5Sum))
					skipUpdate = true;

				if (!skipUpdate) {
					Path path = Paths.get(config.getWorkingDirectory(), FileUtility.getFileName(new URL(datasource)));

					try {
						downloadFile(datasource, path.toFile());
					} catch(Exception ex) {
						LOGGER.warning(ex.getMessage());

						Thread.sleep(300000);

						isRunning = false;
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
					Path path = Paths.get(config.getWorkingDirectory(), file.getName());

					// make a local copy of the file
					FileUtils.copyFile(file, path.toFile());

					osmFile = path.toString();
				}
			}

			if (!skipUpdate) {
				if (Helper.isEmpty(newMd5Sum))
					newMd5Sum = FileUtility.getMd5OfFile(osmFile);

				md5Sum = newMd5Sum;
				File file = new File(osmFile);
				String newFileStamp = Long.toString(file.length());

				String tempGraphLocation = Paths.get(config.getWorkingDirectory(), "graph").toString();

				try {
					// Clear directory from a previous build
					File fileGraph = new File(tempGraphLocation);
					if (fileGraph.exists())
						FileUtils.deleteDirectory(fileGraph);	
				} catch(Exception ex) {
					// do nothing
				}

				FileUtility.makeDirectory(tempGraphLocation);

				RoutingProfileLoadContext loadCntx = new RoutingProfileLoadContext();
				int nUpdatedProfiles = 0;

				for (RoutingProfile profile : routingProfilesCollection.getUniqueProfiles()) {
					RouteProfileConfiguration rpc = profile.getConfiguration();

					Path pathTimestamp = Paths.get(rpc.getGraphPath(), "stamp.txt");
					File file2 = pathTimestamp.toFile();
					if (file2.exists()) {
						String oldFileStamp = FileUtils.readFileToString(file2);
						if (newFileStamp.equals(oldFileStamp))
							continue;
					}

					if (updatePeriod > 0) {
						StorableProperties storageProps = profile.getGraphProperties();
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

						Date importDate = df.parse(storageProps.get("osmreader.import.date"));

						long diff = startTime - importDate.getTime();

						if (!DebugUtility.isDebug() && diff < updatePeriod) {
							continue;
						}
					}

					try {
						updateStatus = "preparing profile '" + rpc.getProfiles() +"'";
						RouteProfileConfiguration rpcNew = new RouteProfileConfiguration(rpc);
						rpcNew.setGraphPath(tempGraphLocation);
						GraphHopper gh = RoutingProfile.initGraphHopper(osmFile, rpcNew, loadCntx);
						profile.updateGH(gh);
						nUpdatedProfiles++;
					} catch (Exception ex) {
						LOGGER.severe("Failed to update graph profile. Message:" + ex.getMessage() + "; StackTrace: " +	StackTraceUtility.getStackTrace(ex));
					}

					updateStatus = null;
				}

				loadCntx.releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed();
				
				FileUtils.writeStringToFile(fileLastUpdate, md5Sum);

				long seconds = (System.currentTimeMillis() - startTime) / 1000;
				if (LOGGER.isLoggable(Level.INFO))
					LOGGER.info(nUpdatedProfiles + " of " + routingProfilesCollection.size() + " profiles were updated in " + seconds + " s.");

				updateStatus = "Last update on " + new Date() + " took " + seconds + " s.";
			}
			else
			{
				LOGGER.info("No new data is available.");
			}
			
		} catch (Exception ex) {
			LOGGER.warning(ex.getMessage());
		}

		if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info("Next route profiles update is scheduled on " + nextUpdate.toString());

		isRunning = false;
	}

	public void stop() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public void destroy() {
		stop();
	}
}
