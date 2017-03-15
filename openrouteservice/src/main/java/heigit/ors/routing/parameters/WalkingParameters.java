/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing.parameters;

/**
 * Created by lliu on 15/03/2017.
 */
public class WalkingParameters extends ProfileParameters {
    private int _greenLevel = -1;
    private int _difficultyLevel = -1;
    private int _maximumGradient = -1;

    public int getGreenLevel() {
        return _greenLevel;
    }

    public void setGreenLevel(int level) {
        this._greenLevel = level;
    }

    public int getDifficultyLevel() {
        return _difficultyLevel;
    }

    public void setDifficultyLevel(int level) {
        this._difficultyLevel = level;
    }

    public int getMaximumGradient() {
        return _maximumGradient;
    }

    public void setMaximumGradient(int maximumGradient) {
        this._maximumGradient = maximumGradient;
    }
}
