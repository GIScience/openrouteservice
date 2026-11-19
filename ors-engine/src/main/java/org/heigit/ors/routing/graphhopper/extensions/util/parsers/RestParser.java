package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.Rest;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;

/**
 * Registers the {@link Rest} encoded value. Rest points do not come from OSM way tags but from the rest point file
 * configured as {@code foot_rest_data}, which is applied after the import; this parser therefore leaves the edge
 * flags unchanged.
 */
public class RestParser implements TagParser {
    private final DecimalEncodedValue restEnc;

    public RestParser() {
        this(Rest.create());
    }

    public RestParser(DecimalEncodedValue restEnc) {
        this.restEnc = restEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(restEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
        return edgeFlags;
    }
}
