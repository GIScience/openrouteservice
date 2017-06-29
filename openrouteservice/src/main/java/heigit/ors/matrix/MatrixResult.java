/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.matrix;

import com.vividsolutions.jts.geom.Coordinate;

public class MatrixResult {
  private float[][] _tables;
  private Coordinate[] _destinations;
  private String[] _destinationNames;
  private Coordinate[] _sources;
  private String[] _sourceNames;
    
  public MatrixResult()
  {
	  _tables = new float[6][];
  }
  
  public void setTable(int metric, float[] values)
  {
	  _tables[metric] = values;
  }
  
  public float[] getTable(int metric)
  {
	  return _tables[metric];
  }

  public Coordinate[] getDestinations()
  {
	  return _destinations;
  }
  
  public void setDestinations(Coordinate[] coords)
  {
	  _destinations = coords;
  }
  
  public String[] getDestinationNames()
  {
	  return _destinationNames;
  }
  
  public void setDestinationNames(String[] names)
  {
	  _destinationNames = names;
  } 
  
  public Coordinate[] getSources()
  {
	  return _sources;
  }
  
  public void setSources(Coordinate[] coords)
  {
	  _sources = coords;
  }
  
  public String[] getSourceNames()
  {
	  return _sourceNames;
  }
  
  public void setSourceNames(String[] names)
  {
	  _sourceNames = names;
  } 
}
