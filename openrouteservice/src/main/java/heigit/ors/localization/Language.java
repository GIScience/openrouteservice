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

import java.util.Locale;

public class Language {
	private final String _langCode; 
	private final String _enLangName; 
	private final String _nativeName; 
	private Locale _locale; 

	public Language(String langCode, String enLangName, String nativeName) { 
		_langCode = langCode; 
		_enLangName = enLangName; 
		_nativeName = nativeName; 
	} 

	public String getLangCode() { 
		return _langCode; 
	} 

	public String getEnLangName() { 
		return _enLangName; 
	} 

	public String getNativeName() { 
		return _nativeName; 
	} 

	public Locale getLocale() { 
		if (_locale != null) { 
			return _locale; 
		} 
		synchronized (this) { 
			if (_locale == null){ 
				_locale = new Locale(_langCode); 
			} 
		} 
		return _locale; 
	} 
	
	@Override 
	public int hashCode() { 
		return _langCode.hashCode();
	} 
}
