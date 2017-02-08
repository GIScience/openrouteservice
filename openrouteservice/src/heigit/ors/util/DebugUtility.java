package org.freeopenls.tools;

public class DebugUtility {

	private static boolean isDebug;
	
	static 
	{
		isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
				.indexOf("-agentlib:jdwp") > 0;
	}
	
	public static boolean isDebug()
	{
		return isDebug;	
	}
}
