package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.parsers.DefaultTagParserFactory;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.routing.util.parsers.TagParserFactory;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.util.parsers.*;

public class OrsTagParserFactory implements TagParserFactory {
    DefaultTagParserFactory defaultTagParserFactory = new DefaultTagParserFactory();

    @Override
    public TagParser create(String name, PMap configuration) {
        try {
            return defaultTagParserFactory.create(name, configuration);
        } catch (IllegalArgumentException e) {
            return switch (name) {
                case Ford.KEY -> new FordParser();
                case Highway.KEY -> new HighwayParser();
                case OsmWayId.KEY -> new OsmWayIdParser();
                case WaySurface.KEY -> new WaySurfaceParser();
                case WayType.KEY -> new WayTypeParser();
                case AgriculturalAccess.KEY -> new VehicleAccessParser(AgriculturalAccess.create(), HeavyVehicleAttributes.AGRICULTURE);
                case BusAccess.KEY -> new VehicleAccessParser(BusAccess.create(), HeavyVehicleAttributes.BUS);
                case DeliveryAccess.KEY -> new VehicleAccessParser(DeliveryAccess.create(), HeavyVehicleAttributes.DELIVERY);
                case ForestryAccess.KEY -> new VehicleAccessParser(ForestryAccess.create(), HeavyVehicleAttributes.FORESTRY);
                case GoodsAccess.KEY -> new VehicleAccessParser(GoodsAccess.create(), HeavyVehicleAttributes.GOODS);
                case HgvAccess.KEY -> new VehicleAccessParser(HgvAccess.create(), HeavyVehicleAttributes.HGV);
                default -> throw e;
            };
        }
    }
}
