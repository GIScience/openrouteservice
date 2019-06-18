package heigit.ors.routing.pathprocessors;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.routing.util.PathProcessorFactory;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.PMap;

import org.apache.log4j.Logger;

public class ORSPathProcessorFactory implements PathProcessorFactory {
    private static final Logger LOGGER = Logger.getLogger(ORSPathProcessorFactory.class.getName());

    public ORSPathProcessorFactory () {
    }
    
    @Override
    public PathProcessor createPathProcessor(PMap opts, FlagEncoder enc, GraphHopperStorage gs) {
        try {
            return new ExtraInfoProcessor(opts, gs, enc);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return PathProcessor.DEFAULT;
    }
}
