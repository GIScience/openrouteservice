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
package heigit.ors.routing.configuration;

import java.util.Properties;

import com.graphhopper.util.Helper;

public class TrafficInformationConfiguration {
	public String LocationCodesPath;
	public String MessagesDatasource;
	public String OutputDirectory;
	public Boolean Enabled = true;
	public Integer UpdateInterval;
	
	public Properties getDataSourceProperties()
	{
		Properties props = new Properties();
		
		if (!Helper.isEmpty(MessagesDatasource))
		{
			String[] values = MessagesDatasource.split(";");
			
			for(String kv : values)
			{
				String[] values2 = kv.split("=");
				props.put(values2[0], values2[1]);
			}
		}
		
		return props;
	}
}
