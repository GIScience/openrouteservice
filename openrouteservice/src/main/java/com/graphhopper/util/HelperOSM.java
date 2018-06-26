package com.graphhopper.util;

import com.graphhopper.storage.ExtendedStorageSequence;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.TurnCostExtension;

public class HelperOSM {

    // Modification by Maxim Rylov: Added getTurnCostExtensions method to extract TurnCostExtension
    public static TurnCostExtension getTurnCostExtensions(GraphExtension extendedStorage) {
        if (extendedStorage instanceof TurnCostExtension) {
            return (TurnCostExtension) extendedStorage;
        } else if (extendedStorage instanceof ExtendedStorageSequence) {
            ExtendedStorageSequence ess = (ExtendedStorageSequence) extendedStorage;
            GraphExtension[] exts = ess.getExtensions();
            for (int i = 0; i < exts.length; i++) {
                if (exts[i] instanceof TurnCostExtension) {
                    return (TurnCostExtension) exts[i];
                }
            }
        }

        return null;
    }
}
