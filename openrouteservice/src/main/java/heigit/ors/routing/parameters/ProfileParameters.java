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

import heigit.ors.routing.ProfileWeighting;
import heigit.ors.routing.ProfileWeightingCollection;

public class ProfileParameters {
    protected ProfileWeightingCollection _weightings;
    protected int _maximumGradient = -1;
    
    public ProfileParameters()
    {
    	
    }

    public int getMaximumGradient() {
        return _maximumGradient;
    }

    public void setMaximumGradient(int maximumGradient) {
        this._maximumGradient = maximumGradient;
    }
    
    public void add(ProfileWeighting weighting)
    {
    	if (_weightings == null)
    		_weightings = new ProfileWeightingCollection();
    	
    	_weightings.add(weighting);
    }
    
    public ProfileWeightingCollection getWeightings()
    {
    	return _weightings;
    }
    
    public boolean hasWeightings()
    {
    	return _weightings != null && _weightings.size() > 0;
    }
}
