package heigit.ors.services.routing;

public enum RouteInstructionsFormat {
	UNKNOWN,
	TEXT,
	HTML;

	public static RouteInstructionsFormat fromString(String text) 
	{
		if ("TEXT".equalsIgnoreCase(text))
			return RouteInstructionsFormat.TEXT;
		else if ("HTML".equalsIgnoreCase(text))
			return RouteInstructionsFormat.HTML;

		return RouteInstructionsFormat.UNKNOWN;
	}
}
