package heigit.ors.routing.instructions;

import java.util.HashMap;
import java.util.Map;

import heigit.ors.localization.Language;
import heigit.ors.localization.LocalizationManager;

public class InstructionTranslatorsCache {
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
