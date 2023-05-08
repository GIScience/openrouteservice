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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.log4j.Logger;
import org.heigit.ors.util.StringUtility;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalizationManager {
	protected static final Logger LOGGER = Logger.getLogger(LocalizationManager.class);

	private final Map<String, LanguageResources> langResources;
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

		String filePattern = "ors_(.*?)(\\.default)?.resources";
		String resourcePattern = "/resources/**/ors_*.resources";

		Resource[] resources = resourcePatternResolver.getResources(resourcePattern);
		Pattern pattern = Pattern.compile(filePattern);

		if (resources.length == 0)
			throw new Exception("Resources can not be found.");

		for (Resource res : resources) {
			File file = res.getFile();
			if (file.isFile()) {
				Matcher matcher = pattern.matcher(file.getName());
				if (matcher.find()) {
					loadLocalization(matcher.group(1).toLowerCase(), file, matcher.group(2) != null);
				}
			}
		}
	}

	private void loadLocalization(String langTag, File file, boolean isDefault) {
		String langCode = langTag.substring(0,2);
		LanguageResources localLangResources = new LanguageResources(langTag);
		try {
			Config allConfig = ConfigFactory.parseFile(file);
			allConfig.entrySet().forEach(entry -> localLangResources.addLocalString(entry.getKey(), StringUtility.trim(entry.getValue().render(), '\"')));
			this.langResources.put(langTag, localLangResources);
			if (isDefault || !this.langResources.containsKey(langCode)) {
				this.langResources.put(langCode, localLangResources);
			}
		} catch(Exception ex) {
			LOGGER.error(String.format("Unable to load resources from file %s", file.getAbsolutePath()));
		}
	}

	public LanguageResources getLanguageResources(String langCode) {
		return langResources.get(langCode.toLowerCase());
	}

	public boolean isLanguageSupported(String langCode) {
		return langResources.containsKey(langCode.toLowerCase());
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
