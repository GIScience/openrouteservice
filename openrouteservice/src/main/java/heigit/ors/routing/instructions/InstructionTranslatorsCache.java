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
package heigit.ors.routing.instructions;

import java.util.HashMap;
import java.util.Map;

import heigit.ors.localization.LocalizationManager;

public class InstructionTranslatorsCache 
{
	private Map<Integer, InstructionTranslator> _translators = null;
	private static volatile InstructionTranslatorsCache m_instance = null;

	private InstructionTranslatorsCache()
	{
		_translators = new HashMap<Integer, InstructionTranslator>();
	}

	public static InstructionTranslatorsCache getInstance()
	{
		if(null == m_instance)
		{
			synchronized(InstructionTranslatorsCache.class)
			{
				m_instance = new InstructionTranslatorsCache();
			}
		}

		return m_instance;
	}

	public InstructionTranslator getTranslator(String langCode) throws Exception
	{
		int hashCode = langCode.hashCode();

		InstructionTranslator res = _translators.get(hashCode);

		if (res == null)
		{
			synchronized(InstructionTranslatorsCache.class)
			{
				res = new InstructionTranslator(LocalizationManager.getInstance().getLanguageResources(langCode));
				_translators.put(hashCode, res);
			}
		}

		return res;
	}
}
