package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.graphhopper.extensions.SurfaceType;

import java.util.List;

public class SurfaceTypeTagParser implements TagParser {
    private EnumEncodedValue<SurfaceType> surfaceTypeEnc;

    public SurfaceTypeTagParser() {
        this(new EnumEncodedValue<>(SurfaceType.KEY, SurfaceType.class));
    }

    public SurfaceTypeTagParser(EnumEncodedValue<SurfaceType> surfaceTypeEnc) {
        this.surfaceTypeEnc = surfaceTypeEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup lookup, List<EncodedValue> registerNewEncodedValue) {
        registerNewEncodedValue.add(surfaceTypeEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        return edgeFlags;
    }
}
