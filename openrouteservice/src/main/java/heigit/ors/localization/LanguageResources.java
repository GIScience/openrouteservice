package heigit.ors.localization;

import java.util.HashMap;
import java.util.Map;

public class LanguageResources 
{
	private Map<Integer,LocalString> _localStrings = null;
	private Language _lang;
	
	public LanguageResources(Language lang)
	{
		_lang = lang;
		_localStrings = new HashMap<Integer, LocalString>();
	}
	
	public void addLocalString(String resourceName, String resourceText)
	{
		int hashCode = resourceName.hashCode();

		if (!_localStrings.containsKey(hashCode))
		{
			LocalString localString = new LocalString(_lang, resourceText);
			_localStrings.put(hashCode, localString);
		}
	}
	
	public Language getLangCode()
	{
		return _lang;
	}
	
	public String getTranslation(String name) throws Exception
	{
		return getTranslation(name, false);
	}
	
	public String getTranslation(String name, boolean throwException) throws Exception
	{
		if (name == null)
			return null;

		LocalString ls = _localStrings.get(name.hashCode());
		if (ls != null)
			return ls.getString();
		else
		{
			if (throwException)
				throw new Exception("Unable to find translation for '" + name + "' in language '" + _lang.getLangCode() + "'.");
			else
				return null;
		}
	}
}
