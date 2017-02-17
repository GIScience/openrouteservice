package heigit.ors.servlet.util;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import heigit.ors.routing.RoutingProfileManager;

public class ORSServletContextListener implements ServletContextListener{

	ServletContext context;

	public void contextInitialized(ServletContextEvent contextEvent) {
			try {
				 RoutingProfileManager.getInstance().toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void contextDestroyed(ServletContextEvent contextEvent) {
		try {
			RoutingProfileManager.getInstance().destroy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
} 