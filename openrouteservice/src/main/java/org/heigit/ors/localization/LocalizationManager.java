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
package org.heigit.ors.localization;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.log4j.Logger;
import org.heigit.ors.util.StringUtility;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class LocalizationManager {
	protected static final Logger LOGGER = Logger.getLogger(LocalizationManager.class);

	private Map<String, LanguageResources> langResources;
	private static LocalizationManager mInstance = null;

	private LocalizationManager() throws Exception {
		langResources = new HashMap<>();
		loadLocalizations();
	}

	public static LocalizationManager getInstance() throws Exception {
		if(null == mInstance) {
			synchronized(LocalizationManager.class) {
				mInstance = new LocalizationManager();
			}
		}
		return mInstance;
	}

	private void loadLocalizations() throws Exception {
		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());

		String filePattern = "ors_(.*?).resources";
		String resourcePattern = "/resources/**/ors_*.resources";

		Resource[] resources = resourcePatternResolver.getResources(resourcePattern);
		Pattern pattern = Pattern.compile(filePattern);

		if (resources.length == 0)
			throw new Exception("Resources can not be found.");

		for (Resource res : resources) {
			File file = res.getFile();
			try {
				if (file.isFile() && file.getName().matches(filePattern)) {
					Matcher matcher = pattern.matcher(file.getName());
					if (matcher.find()) {
						String langCode = matcher.group(1).toLowerCase();
						String[] langCountry = langCode.split("-");
						Locale locale = new Locale(langCountry[0], langCountry.length == 2 ? langCountry[1] : "");
						Language lang = new Language(langCode, locale.getDisplayLanguage(), locale.getDisplayName());
						LanguageResources localLangResources = new LanguageResources(lang);

						Config allConfig = ConfigFactory.parseFile(file);
						allConfig.entrySet().forEach(entry -> localLangResources.addLocalString(entry.getKey(), StringUtility.trim(entry.getValue().render(), '\"')));
						this.langResources.put(langCode, localLangResources);
					}
				}
			} catch(Exception ex) {
				LOGGER.error(String.format("Unable to load resources from file %s", file.getAbsolutePath()));
			}
		}
	}

	public LanguageResources getLanguageResources(String langCode) {
		String lang = langCode.toLowerCase();
		LanguageResources res = langResources.get(lang);
		if (res == null && !langCode.contains("-"))
			res = langResources.get(lang + "-" + lang);
		return res;
	}

	public boolean isLanguageSupported(String langCode) {
		String lang = langCode.toLowerCase();
		boolean res = langResources.containsKey(lang);

		if (!res && !langCode.contains("-"))
			res = langResources.containsKey(lang + "-" + lang);

		return res;
	}

	public String[] getLanguages() {
		String[] langs = new String[langResources.size()];
		int i = 0;
		for (Map.Entry<String, LanguageResources> entry : langResources.entrySet()) {
			langs[i] = entry.getKey();
			i++;
		}

		Arrays.sort(langs);
		
		return langs;
	}
}
