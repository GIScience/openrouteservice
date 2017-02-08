package org.freeopenls.routeservice.routing.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class RouteManagerConfiguration {
	public String OsmFile;
	public String ConfigPathsRoot;
	public int InitializationThreads;
	public String Mode = "Normal"; // Normal or PrepareGraphs
	public double DynamicWeightingMaxDistance = 0;
	public RouteUpdateConfiguration UpdateConfig;
	public TrafficInformationConfiguration TrafficInfoConfig;
    public RouteProfileConfiguration[] RoutePreferences;
    
    public static RouteManagerConfiguration loadFromFile(String path) throws IOException, XMLStreamException
    {
    	RouteManagerConfiguration gc = new RouteManagerConfiguration();

    	//File file = new File(path); //. RouteManagerConfiguration.class.getClassLoader().getResource(path);
    	//InputStream inputstream = new FileInputStream(file);
    	URL url = RouteManagerConfiguration.class.getClassLoader().getResource(path);
    	InputStream inputstream = url.openStream();	
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        XMLStreamReader xmlr =  xmlif.createXMLStreamReader(inputstream);
        
        ArrayList<RouteProfileConfiguration> routePrefs = new ArrayList<RouteProfileConfiguration>();
        
        String elemName = null;
        StringBuilder currentText = null;
        while(xmlr.hasNext()) {
           int event = xmlr.next();
           if (event == XMLStreamConstants.START_ELEMENT)
           {
        	   elemName = xmlr.getLocalName();
        	   
        	   if (elemName.equals("RouteProfileConfiguration"))
               {
            	   routePrefs.add(ParseRouteProfile(xmlr));
               }
        	   else if (elemName.equals("RouteUpdateConfiguration"))
               {
        		   gc.UpdateConfig = ParseRouteUpdateConfig(xmlr);
               }
        	   else if (elemName.equals("TrafficInformationConfiguration"))
               {
        		   gc.TrafficInfoConfig = ParseTrafficInformationConfig(xmlr);
               }
        		   
        	   currentText = new StringBuilder(256);
           }
           else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.SPACE)
           {
        	   if (currentText != null)
        	   currentText.append(xmlr.getText());
           }
           else if (event == XMLStreamConstants.END_ELEMENT)
           {
        	   if (elemName.equals("OsmFile"))
               {
            	   gc.OsmFile = currentText.toString(); 
               }
               else if (elemName.equals("ConfigPathsRoot"))
               {
            	   gc.ConfigPathsRoot = currentText.toString();
               }
               else if (elemName.equals("InitializationThreads"))
               {
            	   gc.InitializationThreads = Math.max(1, Integer.parseInt(currentText.toString()));
               }
               else if (elemName.equals("Mode"))
               {
            	   gc.Mode = currentText.toString(); 
               }
               else if (elemName.equals("DynamicWeightingMaxDistance"))
               {
            	   gc.DynamicWeightingMaxDistance = Double.parseDouble(currentText.toString());
               }
        	   
        	   elemName = null;
        	   currentText = null;
           }
        }
        
        xmlr.close();
        
		gc.RoutePreferences = (RouteProfileConfiguration[])routePrefs.toArray(new RouteProfileConfiguration[routePrefs.size()]);
       
        return gc;
    }
    
    private static RouteProfileConfiguration ParseRouteProfile(XMLStreamReader xmlr) throws XMLStreamException
    {
    	RouteProfileConfiguration ppc = new RouteProfileConfiguration();
    	String elemName = null;
        StringBuilder currentText = null;
        String elemNameStop = xmlr.getLocalName();
    	
    	while(xmlr.hasNext()) {
        	int event = xmlr.next();
        	
    		if (event == XMLStreamConstants.START_ELEMENT)
            {
    			elemName = xmlr.getLocalName();
    			currentText = new StringBuilder(256);
            }
    		else if (event == XMLStreamConstants.CHARACTERS)
    		{
    			if (currentText != null)
    				currentText.append(xmlr.getText());
            }
    		else if (event == XMLStreamConstants.END_ELEMENT)
            {
   			    if (xmlr.getLocalName() == elemNameStop)
   			    	break;
   			 
    			if (elemName.equals("Preferences"))
                {
    				ppc.Preferences = currentText.toString(); 
                }
                else if (elemName.equals("ConfigFileName"))
                {
                    ppc.ConfigFileName = currentText.toString(); 
                }
                else if (elemName.equals("GraphLocation"))
                {
                    ppc.GraphLocation = currentText.toString(); 
                }
                else if (elemName.equals("StoreCustomField") || elemName.equals("DynamicWeighting"))
                {
                    ppc.DynamicWeighting = Boolean.parseBoolean(currentText.toString());
                }
                else if (elemName.equals("StoreSurfaceInformation"))
                {
                    ppc.StoreSurfaceInformation = Boolean.parseBoolean(currentText.toString());
                }
                else if (elemName.equals("StoreHillIndex"))
                {
                    ppc.StoreHillIndex = Boolean.parseBoolean(currentText.toString());
                }
                else if (elemName.equals("Enabled"))
                {
    				ppc.Enabled = Boolean.parseBoolean(currentText.toString()); 
                }
                else if (elemName.equals("UseTrafficInformation"))
                {
    				ppc.UseTrafficInformation = Boolean.parseBoolean(currentText.toString()); 
                }
                else if (elemName.equals("MaximumDistance"))
                {
    				ppc.MaximumDistance = Double.parseDouble(currentText.toString()); 
                }
                else if (elemName.equals("MinimumDistance"))
                {
    				ppc.MinimumDistance = Double.parseDouble(currentText.toString()); 
                }

    			elemName = null;
    			currentText = null;
            }    	
    	}    
    	
    	return ppc;
    }
    
    private static RouteUpdateConfiguration ParseRouteUpdateConfig(XMLStreamReader xmlr) throws XMLStreamException
    {
    	RouteUpdateConfiguration ruc = new RouteUpdateConfiguration();
    	String elemName = null;
        StringBuilder currentText = null;
        String elemNameStop = xmlr.getLocalName();
    	
    	while(xmlr.hasNext()) {
        	int event = xmlr.next();
        	
    		if (event == XMLStreamConstants.START_ELEMENT)
            {
    			elemName = xmlr.getLocalName();
    			currentText = new StringBuilder(256);
            }
    		else if (event == XMLStreamConstants.CHARACTERS)
    		{
    			if (currentText != null)
    				currentText.append(xmlr.getText());
            }
    		else if (event == XMLStreamConstants.END_ELEMENT)
            {
   			    if (xmlr.getLocalName() == elemNameStop)
   			    	break;
   			 
    			if (elemName.equals("Enabled"))
                {
                	ruc.Enabled = Boolean.parseBoolean(currentText.toString()); 
                }
    			else if (elemName.equals("DataSource"))
                {
                	ruc.DataSource = currentText.toString(); 
                }
    			else if (elemName.equals("BoundingBox"))
                {
                	ruc.BoundingBox = currentText.toString(); 
                }	
    			else if (elemName.equals("Time"))
                {
                	ruc.Time = currentText.toString(); 
                }	
    			else if (elemName.equals("WorkingDirectory"))
                {
                	ruc.WorkingDirectory = currentText.toString(); 
                }	
    			
    			elemName = null;
    			currentText = null;
            }    	
    	}    
    	
    	return ruc;
    }
    
    private static TrafficInformationConfiguration ParseTrafficInformationConfig(XMLStreamReader xmlr) throws XMLStreamException
    {
    	TrafficInformationConfiguration tic = new TrafficInformationConfiguration();
    	String elemName = null;
        StringBuilder currentText = null;
        String elemNameStop = xmlr.getLocalName();
    	
    	while(xmlr.hasNext()) {
        	int event = xmlr.next();
        	
    		if (event == XMLStreamConstants.START_ELEMENT)
            {
    			elemName = xmlr.getLocalName();
    			currentText = new StringBuilder(256);
            }
    		else if (event == XMLStreamConstants.CHARACTERS)
    		{
    			if (currentText != null)
    				currentText.append(xmlr.getText());
            }
    		else if (event == XMLStreamConstants.END_ELEMENT)
            {
   			    if (xmlr.getLocalName() == elemNameStop)
   			    	break;
   			 
    			if (elemName.equals("Enabled"))
                {
    				tic.Enabled = Boolean.parseBoolean(currentText.toString()); 
                }
    			else if (elemName.equals("LocationCodesPath"))
                {
    				tic.LocationCodesPath = currentText.toString(); 
                }
    			else if (elemName.equals("OutputDirectory"))
                {
    				tic.OutputDirectory = currentText.toString(); 
                }
    			else if (elemName.equals("MessagesDatasource"))
                {
    				tic.MessagesDatasource = currentText.toString(); 
                }
    			else if (elemName.equals("ConfigFileName"))
                {
    				tic.ConfigFileName = currentText.toString(); 
                }
    			else if (elemName.equals("UpdateInterval"))
                {
    				tic.UpdateInterval = Integer.parseInt(currentText.toString()); 
                }
    			
    			elemName = null;
    			currentText = null;
            }    	
    	}    
    	
    	return tic;
    }
}


