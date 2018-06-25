package com.graphhopper.util;

import com.graphhopper.storage.ExtendedStorageSequence;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.TurnCostExtension;

public class HelperOSM {

    public static class ExtAngleCalc extends AngleCalc {
        // Modification by Maxim Rylov: added new method.
        public double calcAzimuth(double orientation) {
            orientation = Math.PI / 2 - orientation;
            if (orientation < 0)
                orientation += 2 * Math.PI;

            return Math.toDegrees(Helper.round4(orientation)) % 360;
        }
    }

    public static final ExtAngleCalc ANGLE_CALCX = new ExtAngleCalc();

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
