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
package heigit.ors.routing.traffic.providers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import heigit.ors.routing.traffic.providers.TrafficInfoDataSource;

import heigit.ors.util.StreamUtility;

public class FtpDataSource implements TrafficInfoDataSource {
	private String server;
	private Integer port = 21;
	private String user;
	private String password;
	private String file;

	@Override
	public void Initialize(Properties props) {
		server = props.getProperty("server");
		user = props.getProperty("user");
		password = props.getProperty("password");
		file = props.getProperty("file");
		if (props.contains("port"))
			port = Integer.parseInt(props.getProperty("port"));
	}

	@Override
	public String getMessage() throws IOException {
		String message = null;
		try 
		{
			FTPClient ftpClient = new FTPClient();
			ftpClient.connect(server, port);
			ftpClient.login(user, password);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(2);

			String remoteFile1 = "/"+file;
			InputStream inputStream = ftpClient.retrieveFileStream(remoteFile1);
			message = StreamUtility.readStream(inputStream, "iso-8859-1");
		    Boolean success = ftpClient.completePendingCommand();
		    
			if (success) {
				System.out.println("File has been downloaded successfully.");
			}
			
			inputStream.close();
		}
		catch (IOException e)
		{
			/* SendMail mail = new SendMail();
		      try
		      {
		        mail.postMail(this.properties.getProperty("mail"), "TMC-Fehler", 
		          e.getStackTrace().toString(), "TMC@Fehlermeldung.de", this.properties
		          .getProperty("smtpHost"), this.properties
		          .getProperty("smtpUser"), this.properties
		          .getProperty("smtpPort"));
		      }
		      catch (MessagingException e1)
		      {
		        e1.printStackTrace();
		      }
		      this.logger.debug("Error with FTP connection " + 
		        e.getLocalizedMessage(), e);
		      throw new DownloadException(
		        "Error while downloading file from FTP " + 
		        e.getLocalizedMessage(), e);
			 */
		}

		return message;
	}
}
