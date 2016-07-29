package org.freeopenls.routeservice.routing.configuration;

public class RouteUpdateConfiguration {
	public String BoundingBox;
	public Boolean Enabled = true;
	/// Either a web or a file link
	public String DataSource; 
	public String Time = "7, 12:00:00, 60000";
	public String WorkingDirectory;
}
