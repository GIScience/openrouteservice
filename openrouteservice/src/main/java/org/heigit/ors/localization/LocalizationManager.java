/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package heigit.ors.localization;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import heigit.ors.util.StringUtility;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class LocalizationManager {
	protected static Logger LOGGER = LoggerFactory.getLogger(LocalizationManager.class);

	private Map<String, LanguageResources> _langResources = null;
	private static volatile LocalizationManager m_instance = null;

	private LocalizationManager() throws Exception
	{
		_langResources = new HashMap<String, LanguageResources>();

		loadLocalizations();
	}

	public static LocalizationManager getInstance() throws Exception
	{
		if(null == m_instance)
		{
			synchronized(LocalizationManager.class)
			{
				m_instance = new LocalizationManager();
			}
		}

		return m_instance;
	}

	private void loadLocalizations() throws Exception
	{
		PathMatchingResourcePatternResolver resource_pattern = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());

		String filePattern = "ors_(.*?).resources";
		String resourcePattern = "/resources/**/ors_*.resources";

		Resource[] resources = resource_pattern.getResources(resourcePattern);
		Pattern pattern = Pattern.compile(filePattern);

		if (resources.length == 0)
			throw new Exception("Resources can not be found.");

		for (Resource res : resources) {
			File file = res.getFile();
			try
			{
				if (file.isFile()) {
					if (file.getName().matches(filePattern))
					{
						Matcher matcher = pattern.matcher(file.getName());
						if (matcher.find())
						{
							String langCode = matcher.group(1).toLowerCase();
							String[] langCountry = langCode.split("-");
							Locale locale = new Locale(langCountry[0], langCountry.length == 2 ? langCountry[1] : "");
							Language lang = new Language(langCode, locale.getDisplayLanguage(), locale.getDisplayName());
							LanguageResources langResources = new LanguageResources(lang);

							Config allConfig = ConfigFactory.parseFile(file);

							allConfig.entrySet().forEach(entry -> {
								langResources.addLocalString(entry.getKey(), StringUtility.trim(entry.getValue().render(), '\"'));
							});

							_langResources.put(langCode, langResources);
						}
					}
				}
			}
			catch(Exception ex)
			{
				LOGGER.error("Unable to load resources from file " + file.getAbsolutePath());				
			}
		}
	}

	public String getTranslation(String langCode, String name, boolean throwException) throws Exception
	{
		if (name == null)
			return null;

		LanguageResources langRes = _langResources.get(langCode);

		if (langRes != null)
			return langRes.getTranslation(name, throwException);

		if (throwException)
			throw new Exception("Unable to find translation for '" + name + "' in language '" + langCode + "'.");
		else
			return null;
	}

	public LanguageResources getLanguageResources(String langCode)
	{
		String lang = langCode.toLowerCase();
		LanguageResources res = _langResources.get(lang);
		if (res == null)
		{
			if (!langCode.contains("-"))
				res = _langResources.get(lang + "-" + lang);
		}

		return res;
	}

	public boolean isLanguageSupported(String langCode)
	{
		String lang = langCode.toLowerCase();
		boolean res = _langResources.containsKey(lang);

		if (!res)
		{
			if (!langCode.contains("-"))
				res = _langResources.containsKey(lang + "-" + lang);
		}

		return res;
	}

	public String[] getLanguages()
	{
		String[] langs = new String[_langResources.size()];
		int i = 0;
		for (Map.Entry<String, LanguageResources> entry : _langResources.entrySet())
		{
			langs[i] = entry.getKey();
			i++;
		}

		Arrays.sort(langs);
		
		return langs;
	}
}
