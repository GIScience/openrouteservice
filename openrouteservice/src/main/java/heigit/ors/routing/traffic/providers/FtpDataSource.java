/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
