package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.ev.DefaultEncodedValueFactory;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueFactory;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.util.Helper;
import org.heigit.ors.routing.graphhopper.extensions.ev.BorderType;
import org.heigit.ors.routing.graphhopper.extensions.ev.DynamicData;

public class OrsEncodedValueFactory implements EncodedValueFactory {
    DefaultEncodedValueFactory defaultEncodedValueFactory = new DefaultEncodedValueFactory();

    @Override
    public EncodedValue create(String encodedValueString) {
        // This code could be simpler if first calling create on defaultEncodedValueFactory
        // in a try-catch block catching the IllegalArgumentException when an
        // unknown encodedValueString is passed. Unfortunately we can only distinguish
        // the exceptions by their messages, which would be too fragile. Therefore,
        // we need to repeat the sanity checks from DefaultEncodedValueFactory.
        if (Helper.isEmpty(encodedValueString))
            throw new IllegalArgumentException("No string provided to load EncodedValue");

        final EncodedValue enc;
        String name = encodedValueString.split("\\|")[0];
        if (name.isEmpty())
            throw new IllegalArgumentException("To load EncodedValue a name is required. " + encodedValueString);

        return switch (name) {
            case DynamicData.KEY -> DynamicData.create();
            case BorderType.KEY -> new EnumEncodedValue<>(BorderType.KEY, BorderType.class);
            default -> defaultEncodedValueFactory.create(encodedValueString); // Fallback to GraphHopper's EncodedValues
        };
    }
}
