package org.heigit.ors.routing.graphhopper.extensions.util;

import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;

public class WheelchairAttributesEncodedValues {
    IntEncodedValue surfaceEncoder;
    IntEncodedValue smoothnessEncoder;
    IntEncodedValue trackTypeEncoder;
    IntEncodedValue inclineEncoder;
    IntEncodedValue widthEncoder;
    IntEncodedValue kerbEncoder;
    BooleanEncodedValue suitableEncoder;
    EnumEncodedValue<WheelchairAttributes.Side> sideEncoder;
    BooleanEncodedValue surfaceQualityKnownEncoder;

    public WheelchairAttributesEncodedValues(EncodingManager encodingManager) {
        if(encodingManager.hasEncodedValue(WheelchairSurface.KEY))
            surfaceEncoder = encodingManager.getIntEncodedValue(WheelchairSurface.KEY);

        if(encodingManager.hasEncodedValue(WheelchairSmoothness.KEY))
            smoothnessEncoder = encodingManager.getIntEncodedValue(WheelchairSmoothness.KEY);

        if(encodingManager.hasEncodedValue(WheelchairTrackType.KEY))
            trackTypeEncoder = encodingManager.getIntEncodedValue(WheelchairTrackType.KEY);

        if(encodingManager.hasEncodedValue(WheelchairIncline.KEY))
            inclineEncoder = encodingManager.getIntEncodedValue(WheelchairIncline.KEY);

        if(encodingManager.hasEncodedValue(WheelchairWidth.KEY))
            widthEncoder = encodingManager.getIntEncodedValue(WheelchairWidth.KEY);

        if(encodingManager.hasEncodedValue(WheelchairKerb.KEY))
            kerbEncoder = encodingManager.getIntEncodedValue(WheelchairKerb.KEY);

        if(encodingManager.hasEncodedValue(WheelchairSuitable.KEY))
            suitableEncoder = encodingManager.getBooleanEncodedValue(WheelchairSuitable.KEY);

        if(encodingManager.hasEncodedValue(WheelchairSide.KEY))
            sideEncoder = encodingManager.getEnumEncodedValue(WheelchairSide.KEY, WheelchairAttributes.Side.class);

        if(encodingManager.hasEncodedValue(WheelchairSurfaceQualityKnown.KEY))
            surfaceQualityKnownEncoder = encodingManager.getBooleanEncodedValue(WheelchairSurfaceQualityKnown.KEY);
    }

    public WheelchairAttributes getAttributes(IntsRef edgeFlags) {
        WheelchairAttributes attrs = new WheelchairAttributes();
        attrs.setSurfaceType(surfaceEncoder.getInt(false, edgeFlags));
        attrs.setSmoothnessType(smoothnessEncoder.getInt(false, edgeFlags));
        attrs.setTrackType(trackTypeEncoder.getInt(false, edgeFlags));
        attrs.setSuitable(suitableEncoder.getBool(false, edgeFlags));
        attrs.setSide(sideEncoder.getEnum(false, edgeFlags));
        attrs.setSurfaceQualityKnown(surfaceQualityKnownEncoder.getBool(false, edgeFlags));
        
        int incline = inclineEncoder.getInt(false, edgeFlags) - 1; // we store unsigned integers, however -1 is used to indicate unknown
        if (incline > -1) {
            attrs.setIncline(incline); // incline is converts to int in WheelchairAttributes, weird that the method takes a double...
        }

        int width = widthEncoder.getInt(false, edgeFlags);
        if (width > 0) {
            attrs.setWidth(width);
        }

        int kerb = kerbEncoder.getInt(false, edgeFlags);
        if (kerb > 0) {
            attrs.setSlopedKerbHeight(kerb);
        }

        return attrs;
    }
}
