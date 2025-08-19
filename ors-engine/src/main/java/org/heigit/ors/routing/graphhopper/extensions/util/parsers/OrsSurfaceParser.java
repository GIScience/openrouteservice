package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;

import static org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMAttachedSidewalkProcessor.KEY_ORS_SIDEWALK_SIDE;


public class OrsSurfaceParser implements TagParser {
    private EnumEncodedValue<OrsSurface> surfaceEnc;

    public OrsSurfaceParser() {
        this(new EnumEncodedValue<>(OrsSurface.KEY, OrsSurface.class));
    }

    public OrsSurfaceParser(EnumEncodedValue<OrsSurface> surfaceEnc) {
        this.surfaceEnc = surfaceEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(surfaceEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay readerWay, boolean b, IntsRef relationFlags) {
        String surfaceTag = getSurfaceTag(readerWay);
        OrsSurface surface = getFromString(surfaceTag);
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

    private static OrsSurface getFromString(String surface) {
        if (surface == null)
            return OrsSurface.UNKNOWN;

        if (surface.contains(";"))
            surface = surface.split(";")[0];
        if (surface.contains(":"))
            surface = surface.split(":")[0];

        return switch (surface.toLowerCase()) {
            case "paved" -> OrsSurface.PAVED;
            case "unpaved", "woodchips", "rock", "rocks", "stone", "shells", "salt" -> OrsSurface.UNPAVED;
            case "asphalt", "chipseal", "bitmac", "tarmac" -> OrsSurface.ASPHALT;
            case "concrete", "cement" -> OrsSurface.CONCRETE;
            case "paving_stones", "paved_stones", "sett", "cobblestone", "unhewn_cobblestone", "bricks", "brick" -> OrsSurface.PAVING_STONES;
            case "metal" -> OrsSurface.METAL;
            case "wood" -> OrsSurface.WOOD;
            case "compacted", "pebblestone" -> OrsSurface.COMPACTED_GRAVEL;
            case "gravel", "fine_gravel" -> OrsSurface.GRAVEL;
            case "dirt", "earth", "soil" -> OrsSurface.DIRT;
            case "ground", "mud" -> OrsSurface.GROUND;
            case "ice", "snow" -> OrsSurface.ICE;
            case "sand" -> OrsSurface.SAND;
            case "grass" -> OrsSurface.GRASS;
            case "grass_paver" -> OrsSurface.GRASS_PAVER;
            default -> OrsSurface.UNKNOWN;
        };
    }
}
