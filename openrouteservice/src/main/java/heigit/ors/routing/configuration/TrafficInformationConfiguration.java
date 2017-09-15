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
