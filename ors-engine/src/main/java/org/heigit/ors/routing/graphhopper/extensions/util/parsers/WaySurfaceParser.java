package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;

import static org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMAttachedSidewalkProcessor.KEY_ORS_SIDEWALK_SIDE;


public class WaySurfaceParser implements TagParser {
    private EnumEncodedValue<WaySurface> surfaceEnc;

    public WaySurfaceParser() {
        this(new EnumEncodedValue<>(WaySurface.KEY, WaySurface.class));
    }

    public WaySurfaceParser(EnumEncodedValue<WaySurface> surfaceEnc) {
        this.surfaceEnc = surfaceEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(surfaceEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay readerWay, boolean b, IntsRef relationFlags) {
        String surfaceTag = getSurfaceTag(readerWay);
        WaySurface surface = getFromString(surfaceTag);
        surfaceEnc.setEnum(false, edgeFlags, surface);
        return edgeFlags;
    }

    private String getSurfaceTag(ReaderWay readerWay) {
        if (readerWay.hasTag(KEY_ORS_SIDEWALK_SIDE)) {
            String side = readerWay.getTag(KEY_ORS_SIDEWALK_SIDE);
            return readerWay.getTag("sidewalk:" + side + ":surface",
                    readerWay.getTag("sidewalk:both:surface",
                            readerWay.getTag("sidewalk:surface")
                    )
            );
        }
        return readerWay.getTag("surface");
    }

    private static WaySurface getFromString(String surface) {
        if (surface == null)
            return WaySurface.UNKNOWN;

        if (surface.contains(";"))
            surface = surface.split(";")[0];
        if (surface.contains(":"))
            surface = surface.split(":")[0];

        return switch (surface.toLowerCase()) {
            case "paved" -> WaySurface.PAVED;
            case "unpaved", "woodchips", "rock", "rocks", "stone", "shells", "salt" -> WaySurface.UNPAVED;
            case "asphalt", "chipseal", "bitmac", "tarmac" -> WaySurface.ASPHALT;
            case "concrete", "cement" -> WaySurface.CONCRETE;
            case "paving_stones", "paved_stones", "sett", "cobblestone", "unhewn_cobblestone", "bricks", "brick" -> WaySurface.PAVING_STONES;
            case "metal" -> WaySurface.METAL;
            case "wood" -> WaySurface.WOOD;
            case "compacted", "pebblestone" -> WaySurface.COMPACTED_GRAVEL;
            case "gravel", "fine_gravel" -> WaySurface.GRAVEL;
            case "dirt", "earth", "soil" -> WaySurface.DIRT;
            case "ground", "mud" -> WaySurface.GROUND;
            case "ice", "snow" -> WaySurface.ICE;
            case "sand" -> WaySurface.SAND;
            case "grass" -> WaySurface.GRASS;
            case "grass_paver" -> WaySurface.GRASS_PAVER;
            default -> WaySurface.UNKNOWN;
        };
    }
}
