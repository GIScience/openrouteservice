package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.ev.LogieBorders;
import com.graphhopper.routing.ev.WaySurface;
import com.graphhopper.routing.ev.WayType;
import com.graphhopper.routing.util.parsers.DefaultTagParserFactory;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.routing.util.parsers.TagParserFactory;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.util.parsers.LogieBordersParser;
import org.heigit.ors.routing.graphhopper.extensions.util.parsers.WaySurfaceParser;
import org.heigit.ors.routing.graphhopper.extensions.util.parsers.WayTypeParser;

public class OrsTagParserFactory implements TagParserFactory {
    DefaultTagParserFactory defaultTagParserFactory = new DefaultTagParserFactory();

    @Override
    public TagParser create(String name, PMap configuration) {
        try {
            return defaultTagParserFactory.create(name, configuration);
        } catch (IllegalArgumentException e) {
            return switch (name) {
                case WaySurface.KEY -> new WaySurfaceParser();
                case WayType.KEY -> new WayTypeParser();
                case LogieBorders.KEY -> new LogieBordersParser();
                default -> throw e;
            };
        }
    }
}
