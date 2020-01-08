package org.heigit.ors.routing.pathprocessors;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.routing.util.PathProcessorFactory;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.PMap;

import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.apache.log4j.Logger;

public class ORSPathProcessorFactory implements PathProcessorFactory {
    private static final Logger LOGGER = Logger.getLogger(ORSPathProcessorFactory.class.getName());
    private CountryBordersReader countryBordersReader;

    @Override
    public PathProcessor createPathProcessor(PMap opts, FlagEncoder enc, GraphHopperStorage gs) {
        try {
            if (countryBordersReader != null) {
                return new ExtraInfoProcessor(opts, gs, enc, countryBordersReader);
            }
            return new ExtraInfoProcessor(opts, gs, enc);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return PathProcessor.DEFAULT;
    }

    public void setCountryBordersReader(CountryBordersReader cbr) {
        this.countryBordersReader = cbr;
    }
}
