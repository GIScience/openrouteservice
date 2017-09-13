/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.localization;

public class LocalString {
	private Language _language; 
	private String _string; 

	public LocalString(Language language, String string) 
	{ 
		this._language = language; 
		this._string = string; 
	} 

	public Language getLanguage() { 
		return _language; 
	} 

	public String getString() { 
		return _string; 
	} 

	@Override 
	public boolean equals(Object o) { 
		if (this == o) return true; 
		if (o == null || getClass() != o.getClass()) return false; 

		LocalString that = (LocalString) o; 

		return _language.equals(that._language) && _string.equals(that._string); 

	} 

	@Override 
	public int hashCode() { 
		int result = _language.hashCode(); 
		result = 31 * result + _string.hashCode(); 
		return result; 
	} 

	@Override 
	public String toString() { 
		return "LocalString{" + 
				"language=" + _language + 
				", string='" + _string + '\'' + 
				'}'; 
	} 
}
