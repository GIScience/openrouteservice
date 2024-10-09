package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.bike.CargoBikeFlagEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CargoBikeFlagEncoderTest {

    protected final Set<String> preferHighwayTags = new HashSet<>();
    private final CargoBikeFlagEncoder flagEncoder;
    private ReaderWay way;



    public CargoBikeFlagEncoderTest() {
        EncodingManager encodingManager = EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.BIKE_CARGO);
        flagEncoder = (CargoBikeFlagEncoder) encodingManager.getEncoder(FlagEncoderNames.BIKE_CARGO);
    }

    @BeforeEach
    void initWay() {
        way = new ReaderWay(1);
    }

    @Test
    void acceptBridlewayOnlyWithBicycleTag() {
        way.setTag("highway", "bridleway");
        assertTrue(flagEncoder.getAccess(way).canSkip());

        way.setTag("bicycle", "yes");
        assertTrue(flagEncoder.getAccess(way).isWay());

        way.setTag("bicycle", "no");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    void checkTagAvailability() {
        way.setTag("cycleway", "lane");
        assertTrue(flagEncoder.getAccess(way).isWay());

        preferHighwayTags.add("cycleway");
        preferHighwayTags.contains("cycleway");
        assertTrue(preferHighwayTags.contains("cycleway"));
    }
}
