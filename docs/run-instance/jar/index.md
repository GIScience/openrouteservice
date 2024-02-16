# openrouteservice as JAR

Since version 8, openrouteservice can be built as a fat JAR, that contains all its dependencies. 
The JAR can be started stand-alone, without an installed tomcat. 
The trick is, that an embedded servlet container (tomcat) is used. 