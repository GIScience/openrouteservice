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
package heigit.ors.routing.parameters;

public class CyclingParameters extends ProfileParameters 
{
	private int _maximumTrailDifficulty = Integer.MAX_VALUE;
	
	public CyclingParameters()
	{

	}

	public int getMaximumTrailDifficulty() {
		return _maximumTrailDifficulty;
	}

	public void setMaximumTrailDifficulty(int value) {
		_maximumTrailDifficulty = value;
	}
}
