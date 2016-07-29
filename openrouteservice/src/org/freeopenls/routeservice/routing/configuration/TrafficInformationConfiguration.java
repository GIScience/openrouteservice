package org.freeopenls.routeservice.routing.configuration;

import java.util.Properties;

import com.graphhopper.util.Helper;

public class TrafficInformationConfiguration {
	public String LocationCodesPath;
	public String MessagesDatasource;
	public String OutputDirectory;
	public Boolean Enabled = true;
	public String ConfigFileName;
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
