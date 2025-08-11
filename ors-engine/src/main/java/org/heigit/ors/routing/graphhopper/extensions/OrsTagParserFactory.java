package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.util.parsers.DefaultTagParserFactory;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.routing.util.parsers.TagParserFactory;
import com.graphhopper.util.PMap;

public class OrsTagParserFactory implements TagParserFactory {
    DefaultTagParserFactory defaultTagParserFactory = new DefaultTagParserFactory();

    @Override
    public TagParser create(String name, PMap configuration) {
        try {
            return defaultTagParserFactory.create(name, configuration);
        } catch (IllegalArgumentException e) {
// TODO: add a new tag parser for each new encoded value here:
//            return switch (name) {
//                case MyNewEncodedValue.KEY -> new MyNewTagParserTagParser();
//                default -> throw e;
//            };
          throw e;
        }
    }
}
