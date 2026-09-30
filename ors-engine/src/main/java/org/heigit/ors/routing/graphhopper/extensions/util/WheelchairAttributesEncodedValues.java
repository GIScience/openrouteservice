package org.heigit.ors.routing.graphhopper.extensions.util;

import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;

import java.util.function.IntConsumer;

public class WheelchairAttributesEncodedValues {
    IntEncodedValue surfaceEncoder;
    IntEncodedValue smoothnessEncoder;
    IntEncodedValue trackTypeEncoder;
    IntEncodedValue inclineEncoder;
    DecimalEncodedValue widthEncoder;
    IntEncodedValue kerbEncoder;
    BooleanEncodedValue suitableEncoder;
    EnumEncodedValue<WheelchairAttributes.Side> sideEncoder;
    BooleanEncodedValue surfaceQualityKnownEncoder;

    public WheelchairAttributesEncodedValues(EncodingManager encodingManager) {
        if (encodingManager.hasEncodedValue(WheelchairSurface.KEY))
            surfaceEncoder = encodingManager.getIntEncodedValue(WheelchairSurface.KEY);

        if (encodingManager.hasEncodedValue(WheelchairSmoothness.KEY))
            smoothnessEncoder = encodingManager.getIntEncodedValue(WheelchairSmoothness.KEY);

        if (encodingManager.hasEncodedValue(WheelchairTrackType.KEY))
            trackTypeEncoder = encodingManager.getIntEncodedValue(WheelchairTrackType.KEY);

        if (encodingManager.hasEncodedValue(WheelchairIncline.KEY))
            inclineEncoder = encodingManager.getIntEncodedValue(WheelchairIncline.KEY);

        if (encodingManager.hasEncodedValue(WheelchairWidth.KEY))
            widthEncoder = encodingManager.getDecimalEncodedValue(WheelchairWidth.KEY);

        if (encodingManager.hasEncodedValue(WheelchairKerb.KEY))
            kerbEncoder = encodingManager.getIntEncodedValue(WheelchairKerb.KEY);

        if (encodingManager.hasEncodedValue(WheelchairSuitable.KEY))
            suitableEncoder = encodingManager.getBooleanEncodedValue(WheelchairSuitable.KEY);

        if (encodingManager.hasEncodedValue(WheelchairSide.KEY))
            sideEncoder = encodingManager.getEnumEncodedValue(WheelchairSide.KEY, WheelchairAttributes.Side.class);

        if (encodingManager.hasEncodedValue(WheelchairSurfaceQualityKnown.KEY))
            surfaceQualityKnownEncoder = encodingManager.getBooleanEncodedValue(WheelchairSurfaceQualityKnown.KEY);
    }

    private void readAttributeIfEvExists(IntsRef edgeFlags, IntEncodedValue ev, IntConsumer setter) {
        if (ev != null) {
            int value = ev.getInt(false, edgeFlags);
            if (value > 0)
                setter.accept(value);
        }
    }

    public WheelchairAttributes getAttributes(IntsRef edgeFlags) {
        WheelchairAttributes attrs = new WheelchairAttributes();

        readAttributeIfEvExists(edgeFlags, surfaceEncoder, attrs::setSurfaceType);
        readAttributeIfEvExists(edgeFlags, smoothnessEncoder, attrs::setSmoothnessType);
        readAttributeIfEvExists(edgeFlags, trackTypeEncoder, attrs::setTrackType);
        readAttributeIfEvExists(edgeFlags, inclineEncoder, attrs::setIncline);
        readAttributeIfEvExists(edgeFlags, kerbEncoder, attrs::setSlopedKerbHeight);

        if (suitableEncoder != null)
            attrs.setSuitable(suitableEncoder.getBool(false, edgeFlags));

        if (sideEncoder != null)
            attrs.setSide(sideEncoder.getEnum(false, edgeFlags));

        if (surfaceQualityKnownEncoder != null)
            attrs.setSurfaceQualityKnown(surfaceQualityKnownEncoder.getBool(false, edgeFlags));


        if (widthEncoder != null) {
            int width = (int) widthEncoder.getDecimal(false, edgeFlags);
            if (width > 0)
                attrs.setWidth(width);
        }

        return attrs;
    }
}
