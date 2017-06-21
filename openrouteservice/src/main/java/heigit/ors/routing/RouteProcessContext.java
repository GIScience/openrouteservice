package heigit.ors.routing;

import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.util.ByteArrayBuffer;

public class RouteProcessContext {
	private PathProcessor _pathProcessor;
	private ByteArrayBuffer _arrayBuffer;
	
   public RouteProcessContext(PathProcessor pathProcessor)
   {
	   _pathProcessor = pathProcessor;
   }
   
   public PathProcessor getPathProcessor()
   {
	   return _pathProcessor;
   }
   
   public ByteArrayBuffer getArrayBuffer()
   {
	   if (_arrayBuffer == null)
		   _arrayBuffer = new ByteArrayBuffer(4);
	   
	   return _arrayBuffer;
   }
}
