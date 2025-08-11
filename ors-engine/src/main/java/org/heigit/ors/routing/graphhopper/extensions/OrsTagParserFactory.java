package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.util.parsers.DefaultTagParserFactory;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.routing.util.parsers.TagParserFactory;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.ev.BorderType;
import org.heigit.ors.routing.graphhopper.extensions.util.parsers.BorderTypeTagParser;

public class OrsTagParserFactory implements TagParserFactory {
    DefaultTagParserFactory defaultTagParserFactory = new DefaultTagParserFactory();

    @Override
    public TagParser create(String name, PMap configuration) {
        try {
            return defaultTagParserFactory.create(name, configuration);
        } catch (IllegalArgumentException e) {
            return switch (name) {
                case BorderType.KEY -> new BorderTypeTagParser();
                default -> throw e;
            };
        }
    }
}
