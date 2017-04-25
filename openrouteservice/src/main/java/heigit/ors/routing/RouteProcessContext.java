package heigit.ors.routing;

import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.util.ArrayBuffer;

public class RouteProcessContext {
	private PathProcessor _pathProcessor;
	private ArrayBuffer _arrayBuffer;
	
   public RouteProcessContext(PathProcessor pathProcessor)
   {
	   _pathProcessor = pathProcessor;
   }
   
   public PathProcessor getPathProcessor()
   {
	   return _pathProcessor;
   }
   
   public ArrayBuffer getArrayBuffer()
   {
	   if (_arrayBuffer == null)
		   _arrayBuffer = new ArrayBuffer(4);
	   
	   return _arrayBuffer;
   }
}
