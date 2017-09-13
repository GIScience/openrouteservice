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

public class MatrixResult {
  private float[][] _tables;
  private ResolvedLocation[] _destinations;
  private ResolvedLocation[] _sources;
  
  public MatrixResult(ResolvedLocation[] sources, ResolvedLocation[] destinations)
  {
	  _tables = new float[6][];
	  _sources = sources;
	  _destinations = destinations;
  }
  
  public void setTable(int metric, float[] values)
  {
	  _tables[metric] = values;
  }
  
  public float[] getTable(int metric)
  {
	  return _tables[metric];
  }

  public ResolvedLocation[] getDestinations()
  {
	  return _destinations;
  }
  
  public void setDestinations(ResolvedLocation[] locations)
  {
	  _destinations = locations;
  }
  
  public ResolvedLocation[] getSources()
  {
	  return _sources;
  }
  
  public void setSources(ResolvedLocation[] locations)
  {
	  _sources = locations;
  }
}
