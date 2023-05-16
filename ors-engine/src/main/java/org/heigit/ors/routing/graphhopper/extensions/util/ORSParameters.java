/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions.util;

/**
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 */
public class ORSParameters {
    private ORSParameters() {}

    /* Parameters with an 'INIT' prefix are used as defaults and/or are configured at start.*/
    static final String ROUTING_INIT_PREFIX = "routing.";

    /**
     * Parameters that can be passed as hints and influence routing per request.
     */
    public static final class Weighting {
        private Weighting() {}

        public static final String TIME_DEPENDENT_SPEED_OR_ACCESS = "time_dependent_speed_or_access";
    }


    /**
     * Properties for routing with contraction hierarchies speedup
     */
    public static final class Core {
        private Core() {}

        public static final String PREPARE = "prepare.core.";
        /**
         * This property name in HintsMap configures at runtime if CH routing should be ignored.
         */
        public static final String DISABLE = "core.disable";
        /**
         * This property name configures at start if the DISABLE parameter can have an effect.
         */
        public static final String INIT_DISABLING_ALLOWED = ROUTING_INIT_PREFIX + "core.disabling_allowed";
        /**
         * The property name in HintsMap if heading should be used for CH regardless of the possible
         * routing errors.
         */
        public static final String FORCE_HEADING = "core.force_heading";
    }

    /**
     * Properties for routing with landmark speedup
     */
    public static final class CoreLandmark {
        private CoreLandmark() {}

        public static final String PREPARE = "prepare.corelm.";
        /**
         * Specifies landmark sets.
         */
        public static final String LMSETS = PREPARE + "lmsets";
        /**
         * This property name in HintsMap configures at runtime if CH routing should be ignored.
         */
        public static final String DISABLE = "corelm.disable";
        /**
         * Specifies how many active landmarks should be used when routing
         */
        public static final String ACTIVE_COUNT = "corelm.active_landmarks";
        /**
         * Default for active count
         */
        public static final String ACTIVE_COUNT_DEFAULT = ROUTING_INIT_PREFIX + ACTIVE_COUNT;
        /**
         * Specifies how many landmarks should be created
         */
        public static final String COUNT = PREPARE + "landmarks";
        /**
         * This property name configures at start if the DISABLE parameter can have an effect.
         */
        public static final String INIT_DISABLING_ALLOWED = ROUTING_INIT_PREFIX + "corelm.disabling_allowed";
    }

    /**
     * Properties for partition preparation
     */
    public static final class FastIsochrone {
        private FastIsochrone(){}

        public static final String PREPARE = "prepare.fastisochrone.";
        /**
         * This property name in HintsMap configures at runtime if CH routing should be ignored.
         */
        public static final String PROFILE = "fastisochrone.profile";
        /**
         * This property name configures at start if the DISABLE parameter can have an effect.
         */
        public static final String INIT_DISABLING_ALLOWED = ROUTING_INIT_PREFIX + "fastisochrone.disabling_allowed";
        /**
         * The property name in HintsMap if heading should be used for CH regardless of the possible
         * routing errors.
         */
        public static final String FORCE_HEADING = "fastisochrone.force_heading";
    }


}
